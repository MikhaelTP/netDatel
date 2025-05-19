package com.netdatel.identityserviceapi.service.impl;

import com.netdatel.identityserviceapi.domain.dto.AuthRequest;
import com.netdatel.identityserviceapi.domain.dto.AuthResponse;
import com.netdatel.identityserviceapi.domain.entity.Session;
import com.netdatel.identityserviceapi.domain.entity.User;
import com.netdatel.identityserviceapi.exception.CustomAuthenticationException;
import com.netdatel.identityserviceapi.repository.SessionRepository;
import com.netdatel.identityserviceapi.security.jwt.JwtService;
import com.netdatel.identityserviceapi.service.AuditService;
import com.netdatel.identityserviceapi.service.AuthService;
import com.netdatel.identityserviceapi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final SessionRepository sessionRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public AuthResponse login(AuthRequest authRequest, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userService.findUserByUsername(authRequest.getUsername())
                    .orElseThrow(() -> new CustomAuthenticationException("User not found"));

            String jwtToken = jwtService.generateToken(authentication, user);
            String refreshToken = jwtService.generateRefreshToken(authentication, user);

            // Update last login time
            userService.updateLastLogin(user.getId());

            // Save session
            Session session = Session.builder()
                    .user(user)
                    .token(refreshToken)
                    .ipAddress(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(1)) // 24 hours for refresh token
                    .isActive(true)
                    .build();
            sessionRepository.save(session);

            auditService.logEvent("USER_LOGIN", "user", user.getId().toString(), null, null);

            // Get roles and permissions for response
            List<String> roles = user.getRoles().stream()
                    .map(role -> "ROLE_" + role.getName())
                    .collect(Collectors.toList());

            List<String> permissions = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> !authority.startsWith("ROLE_"))
                    .collect(Collectors.toList());

            return AuthResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hour in seconds
                    .userId(user.getId())
                    .username(user.getUsername())
                    .userType(user.getUserType().name())
                    .roles(roles)
                    .permissions(permissions)
                    .build();
        } catch (org.springframework.security.core.AuthenticationException e) {
            auditService.logEvent("LOGIN_FAILED", "auth", authRequest.getUsername(), null, null);
            throw new CustomAuthenticationException("Authentication failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken, HttpServletRequest request) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new CustomAuthenticationException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        String type = jwtService.getTokenType(refreshToken);

        if (!"REFRESH".equals(type)) {
            throw new CustomAuthenticationException("Invalid token type");
        }

        User user = userService.findUserByUsername(username)
                .orElseThrow(() -> new CustomAuthenticationException("User not found"));

        // Validate session
        Optional<Session> sessionOpt = sessionRepository.findByToken(refreshToken);
        if (sessionOpt.isEmpty() || !sessionOpt.get().isActive()) {
            throw new CustomAuthenticationException("Session not found or inactive");
        }

        // Create new tokens
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, null, userService.loadUserByUsername(username).getAuthorities());

        String newJwtToken = jwtService.generateToken(authentication, user);
        String newRefreshToken = jwtService.generateRefreshToken(authentication, user);

        // Update session
        Session session = sessionOpt.get();
        session.setToken(newRefreshToken);
        session.setIpAddress(getClientIp(request));
        session.setUserAgent(request.getHeader("User-Agent"));
        session.setExpiresAt(LocalDateTime.now().plusDays(1));
        sessionRepository.save(session);

        auditService.logEvent("TOKEN_REFRESH", "user", user.getId().toString(), null, null);

        // Get roles and permissions for response
        List<String> roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.toList());

        List<String> permissions = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .token(newJwtToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 hour in seconds
                .userId(user.getId())
                .username(user.getUsername())
                .userType(user.getUserType().name())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        Session session = sessionRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomAuthenticationException("Session not found"));

        session.setActive(false);
        sessionRepository.save(session);

        auditService.logEvent("USER_LOGOUT", "user", session.getUser().getId().toString(), null, null);
    }

    @Override
    @Transactional
    public void logoutAllUserSessions(String username) {
        User user = userService.findUserByUsername(username)
                .orElseThrow(() -> new CustomAuthenticationException("User not found"));

        sessionRepository.deactivateAllUserSessions(user);

        auditService.logEvent("USER_LOGOUT_ALL_SESSIONS", "user", user.getId().toString(), null, null);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void cleanupExpiredSessions() {
        sessionRepository.deactivateExpiredSessions(LocalDateTime.now());
        log.info("Cleaned up expired sessions");
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}