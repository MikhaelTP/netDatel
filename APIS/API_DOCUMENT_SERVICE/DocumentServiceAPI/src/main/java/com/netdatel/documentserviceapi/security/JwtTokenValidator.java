package com.netdatel.documentserviceapi.security;

import com.netdatel.documentserviceapi.config.JwtClientProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenValidator {
    private final JwtClientProperties jwtProperties;

    public Claims validateTokenAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtProperties.getPublicKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Invalid JWT token", e);
            return null;
        }
    }
}