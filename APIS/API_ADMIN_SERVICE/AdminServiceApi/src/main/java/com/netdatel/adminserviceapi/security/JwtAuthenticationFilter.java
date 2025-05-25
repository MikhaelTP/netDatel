package com.netdatel.adminserviceapi.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        // 🔍 DEBUG 1: Verificar si llega el header
        System.out.println("🔍 DEBUG - Authorization Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ DEBUG - No Bearer token found");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        System.out.println("🔍 DEBUG - JWT Token extracted: " + jwt.substring(0, 20) + "...");

        try {
            Claims claims = jwtService.extractAllClaims(jwt);
            System.out.println("🔍 DEBUG - Claims extracted successfully");
            System.out.println("🔍 DEBUG - Subject: " + claims.getSubject());
            System.out.println("🔍 DEBUG - Issuer: " + claims.getIssuer());
            System.out.println("🔍 DEBUG - Roles: " + claims.get("roles"));
            System.out.println("🔍 DEBUG - Permissions: " + claims.get("permissions"));

            if (jwtService.isTokenValid(jwt)) {

                System.out.println("✅ DEBUG - Token is valid");

                // ✅ NUEVO: Extraer información del JWT para crear UserPrincipal
                String username = claims.getSubject();
                Integer userId = claims.get("userId", Integer.class);
                String email = claims.get("email", String.class);
                Collection<? extends GrantedAuthority> authorities = extractAuthorities(claims);

                System.out.println("🔍 DEBUG - Creating UserPrincipal with userId: " + userId + ", email: " + email);

                // ✅ NUEVO: Crear UserPrincipal en lugar de usar solo el username
                // En tu JwtAuthenticationFilter, después de crear UserPrincipal
                UserPrincipal userPrincipal = new UserPrincipal(userId, email, username, authorities);

                // ✅ AGREGAR ESTE DEBUG
                System.out.println("🔍 JWT FILTER DEBUG - UserPrincipal created:");
                System.out.println("  - userId: " + userPrincipal.getUserId());
                System.out.println("  - email: " + userPrincipal.getEmail());
                System.out.println("  - username: " + userPrincipal.getUsername());

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        authorities
                );

                System.out.println("🔍 DEBUG - Authorities: " + authentication.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("✅ DEBUG - Authentication set successfully");
            } else {
                System.out.println("❌ DEBUG - Token is NOT valid");
            }
        } catch (Exception e) {
            System.out.println("❌ DEBUG - Exception extracting claims: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private Collection<? extends GrantedAuthority> extractAuthorities(Claims claims) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extraer roles
        List<String> roles = claims.get("roles", List.class);
        System.out.println("🔍 DEBUG - Extracted roles from claims: " + roles);

        if (roles != null) {
            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                System.out.println("🔍 DEBUG - Added role authority: ROLE_" + role);
            }
        }

        // Extraer permisos
        List<String> permissions = claims.get("permissions", List.class);
        System.out.println("🔍 DEBUG - Extracted permissions from claims: " + permissions);

        if (permissions != null) {
            for (String permission : permissions) {
                authorities.add(new SimpleGrantedAuthority(permission));
                System.out.println("🔍 DEBUG - Added permission authority: " + permission);
            }
        }

        System.out.println("🔍 DEBUG - Final authorities list: " + authorities);
        return authorities;
    }
}