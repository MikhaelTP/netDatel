package com.netdatel.identityserviceapi.api;

import com.netdatel.identityserviceapi.domain.dto.AuthRequest;
import com.netdatel.identityserviceapi.domain.dto.AuthResponse;
import com.netdatel.identityserviceapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        log.debug("REST request to login user: {}", request.getUsername());
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh authentication token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String refreshToken, HttpServletRequest request) {
        log.debug("REST request to refresh token");
        return ResponseEntity.ok(authService.refreshToken(refreshToken, request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate token")
    public ResponseEntity<Void> logout(@RequestParam String refreshToken) {
        log.debug("REST request to logout user");
        authService.logout(refreshToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate-token")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        log.debug("REST request to validate token");
        return ResponseEntity.ok(authService.validateToken(token));
    }
}