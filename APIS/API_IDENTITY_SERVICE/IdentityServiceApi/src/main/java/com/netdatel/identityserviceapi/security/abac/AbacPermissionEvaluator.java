package com.netdatel.identityserviceapi.security.abac;

import com.netdatel.identityserviceapi.domain.entity.User;
import com.netdatel.identityserviceapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación de ABAC (Attribute-Based Access Control) usando Spring Security PermissionEvaluator.
 * Esta clase permite evaluar permisos basados en:
 * 1. Permisos directos (como RBAC tradicional)
 * 2. Atributos del usuario (como su tipo, organización, etc.)
 * 3. Atributos del objeto al que intenta acceder
 * 4. Contexto de la petición (tiempo, ubicación, etc.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AbacPermissionEvaluator implements PermissionEvaluator {

    private final UserRepository userRepository;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || !(permission instanceof String)) {
            return false;
        }

        String permissionString = (String) permission;

        // Verificación básica RBAC: comprobar si el usuario tiene el permiso directamente
        boolean hasDirectPermission = hasDirectPermission(authentication, permissionString);
        if (hasDirectPermission) {
            return true;
        }

        // Si no tiene permiso directo, aplicar reglas ABAC
        return evaluateAbacRules(authentication, targetDomainObject, permissionString);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetType == null || !(permission instanceof String)) {
            return false;
        }

        String permissionString = (String) permission;

        // Verificación básica RBAC: comprobar si el usuario tiene el permiso directamente
        boolean hasDirectPermission = hasDirectPermission(authentication, permissionString);
        if (hasDirectPermission) {
            return true;
        }

        // Para evaluación ABAC, necesitamos cargar el objeto completo
        // Este método podría ser extendido para cargar diferentes tipos de objetos según 'targetType'
        return false;
    }

    private boolean hasDirectPermission(Authentication authentication, String permission) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(permission));
    }

    private boolean evaluateAbacRules(Authentication authentication, Object targetObject, String permission) {
        try {
            // Obtener información del usuario actual
            String username = authentication.getName();
            Optional<User> currentUser = userRepository.findByUsername(username);
            if (currentUser.isEmpty()) {
                return false;
            }

            // Preparar contexto de evaluación
            EvaluationContext context = prepareEvaluationContext(authentication, currentUser.get(), targetObject);

            // Obtener o compilar la expresión para este permiso
            Expression expression = getOrCompileExpression(permission, targetObject);
            if (expression == null) {
                return false; // No hay regla ABAC para este permiso
            }

            // Evaluar la expresión
            return Boolean.TRUE.equals(expression.getValue(context, Boolean.class));
        } catch (Exception e) {
            log.error("Error evaluating ABAC rules for permission: {}", permission, e);
            return false;
        }
    }

    private EvaluationContext prepareEvaluationContext(Authentication authentication, User currentUser, Object targetObject) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        // Agregar variables al contexto
        context.setVariable("authentication", authentication);
        context.setVariable("user", currentUser);
        context.setVariable("target", targetObject);

        // Preparar helpers y datos adicionales para reglas complejas
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userType", currentUser.getUserType().name());

        if (currentUser.getAttributes() != null && !currentUser.getAttributes().isEmpty()) {
            attributes.put("userAttributes", currentUser.getAttributes());
        }

        context.setVariable("attributes", attributes);

        return context;
    }

    private Expression getOrCompileExpression(String permission, Object targetObject) {
        // Clave para cachear la expresión
        String targetType = targetObject != null ? targetObject.getClass().getSimpleName() : "Unknown";
        String cacheKey = permission + "_" + targetType;

        // Intentar obtener desde caché
        Expression expression = expressionCache.get(cacheKey);
        if (expression != null) {
            return expression;
        }

        // Si no está en caché, compilar la expresión según el permiso
        String expressionString = getAbacRuleForPermission(permission, targetType);
        if (expressionString == null) {
            return null; // No hay regla para este permiso
        }

        expression = expressionParser.parseExpression(expressionString);
        expressionCache.put(cacheKey, expression);
        return expression;
    }

    private String getAbacRuleForPermission(String permission, String targetType) {
        // Aquí implementaríamos reglas específicas según el permiso y el tipo de objeto
        // Esto podría venir de una base de datos, un archivo de configuración, etc.

        // Ejemplos de reglas:
        if ("admin:client:view".equals(permission) && "Client".equals(targetType)) {
            // Un usuario puede ver un cliente si es admin o si pertenece a la misma organización
            return "#user.userType == 'SUPER_ADMIN' or (#user.userType == 'CLIENT_ADMIN' and #target.id == #attributes['userAttributes']['clientId'])";
        }

        if ("document:file:download".equals(permission) && "File".equals(targetType)) {
            // Un usuario puede descargar un archivo si tiene permiso explícito o si es el propietario
            return "#target.uploadedBy == #user.id or (#attributes['userAttributes']['allowedFiles'] != null and #attributes['userAttributes']['allowedFiles'].contains(#target.id))";
        }

        if ("provider:audit:approve".equals(permission) && "Audit".equals(targetType)) {
            // Solo auditores pueden aprobar auditorías
            return "#user.userType == 'AUDITOR'";
        }

        // Regla por defecto: no hay ABAC disponible para este permiso
        return null;
    }
}