package com.netdatel.adminserviceapi.config;

import com.netdatel.adminserviceapi.security.CurrentUserId;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resuelve el parámetro de ID de usuario en los métodos de controlador
 * a partir del token JWT o del encabezado X-User-Id.
 *
 * Permite inyectar automáticamente el ID del usuario autenticado usando la anotación
 * @CurrentUserId en los parámetros de los métodos controladores.
 */
@Component
public class UserIdResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        Object userId = request.getAttribute("userId");

        if (userId == null) {
            // Si no existe, intentar obtenerlo del header X-User-Id (para compatibilidad)
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                try {
                    return Long.parseLong(userIdHeader);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid user ID in header");
                }
            }
            throw new IllegalStateException("User ID not available");
        }

        return userId;
    }
}