package com.netdatel.adminserviceapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);


    @Value("${app.security.jwt.secret-key}")
    private String secretKey;

    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.security.jwt.issuer}")
    private String issuer;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);

        // ✅ AGREGAR ESTOS LOGS
        logger.info("🔑 JWT Secret Key (first 10 chars): {}", secretKey.substring(0, 10) + "...");
        logger.info("🏢 JWT Issuer: {}", issuer);
        logger.info("⏱️ JWT Expiration: {} ms", jwtExpiration);
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        try {
            logger.debug("🔍 DEBUG - Parsing JWT token with key...");

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            logger.debug("✅ DEBUG - JWT parsed successfully");
            return claims;

        } catch (Exception e) {
            logger.error("❌ DEBUG - Exception extracting claims: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenIssuer = claims.getIssuer();
            Date expiration = claims.getExpiration();
            Date now = new Date();

            System.out.println("🔍 DEBUG - Token issuer: " + tokenIssuer);
            System.out.println("🔍 DEBUG - Expected issuer: admin-service");
            System.out.println("🔍 DEBUG - Token expiration: " + expiration);
            System.out.println("🔍 DEBUG - Current time: " + now);
            System.out.println("🔍 DEBUG - Is expired: " + expiration.before(now));

            boolean isValidIssuer = "admin-service".equals(tokenIssuer);
            boolean isNotExpired = !expiration.before(now);

            System.out.println("🔍 DEBUG - Valid issuer: " + isValidIssuer);
            System.out.println("🔍 DEBUG - Not expired: " + isNotExpired);

            return isValidIssuer && isNotExpired;
        } catch (Exception e) {
            System.out.println("❌ DEBUG - Token validation exception: " + e.getMessage());
            return false;
        }
    }

    public Collection<? extends GrantedAuthority> extractAuthorities(Claims claims) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extraer roles
        List<String> roles = claims.get("roles", List.class);
        if (roles != null) {
            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }

        // Extraer permisos
        List<String> permissions = claims.get("permissions", List.class);
        if (permissions != null) {
            for (String permission : permissions) {
                authorities.add(new SimpleGrantedAuthority(permission));
            }
        }

        return authorities;
    }

    // Método para extraer el ID del usuario del token (subject)
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        throw new RuntimeException("Invalid or missing userId in JWT token");
    }
}