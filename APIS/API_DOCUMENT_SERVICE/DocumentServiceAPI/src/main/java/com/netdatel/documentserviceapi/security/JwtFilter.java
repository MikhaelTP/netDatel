package com.netdatel.documentserviceapi.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtTokenValidator jwtTokenValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Crear una autenticación simulada con un ID de usuario fijo para pruebas
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("document:read"));
            authorities.add(new SimpleGrantedAuthority("document:write"));
            authorities.add(new SimpleGrantedAuthority("document:admin"));

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    1, // ID de usuario fijo para pruebas (usuario administrador)
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Continuar con la cadena de filtros
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error en autenticación", e);
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error de autenticación: " + e.getMessage());
        }
    }
    /*
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();

            // Skip JWT validation for health check and OpenAPI endpoints
            if (path.equals("/api/health") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
                filterChain.doFilter(request, response);
                return;
            }

            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no proporcionado");
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = jwtTokenValidator.validateTokenAndGetClaims(token);

            if (claims == null) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
                return;
            }

            // Extract user ID from token
            Integer userId = claims.get("sub", Integer.class);

            // Check if user has permissions for this service
            List<String> permissions = claims.get("permissions", List.class);
            boolean hasDocumentPermission = permissions != null && permissions.stream()
                    .anyMatch(p -> p.startsWith("document:"));

            if (!hasDocumentPermission) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permisos para acceder a este servicio");
                return;
            }

            // Create authentication
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            // Add roles
            List<String> roles = claims.get("roles", List.class);
            if (roles != null) {
                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            }

            // Add permissions
            if (permissions != null) {
                permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error validating JWT token", e);
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error de autenticación: " + e.getMessage());
        }
    }

     */
}