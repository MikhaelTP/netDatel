package com.netdatel.identityserviceapi.service;

import com.netdatel.identityserviceapi.domain.dto.AuthRequest;
import com.netdatel.identityserviceapi.domain.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse login(AuthRequest authRequest, HttpServletRequest request);

    AuthResponse refreshToken(String refreshToken, HttpServletRequest request);

    void logout(String refreshToken);

    void logoutAllUserSessions(String username);

    boolean validateToken(String token);
}
