package org.example.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for JWT operations at the Gateway level.
 * <p>
 * Responsibilities:
 * - Validate the JWT signature and expiry
 * - Extract claims (username, role) for downstream header forwarding
 * <p>
 * NOTE: This class only VALIDATES tokens. Token GENERATION happens in Auth Service.
 */
@Slf4j
@Component
public class JwtUtil {

    private final SecretKey signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // Derive a SecretKey from the configured hex string
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates the JWT token — checks signature and expiry.
     *
     * @param token raw JWT string (without "Bearer " prefix)
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extracts the subject (username) from the JWT.
     *
     * @param token raw JWT string
     * @return username stored as subject
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extracts the user role claim from the JWT.
     *
     * @param token raw JWT string
     * @return role string (e.g. "ROLE_PATIENT", "ROLE_ADMIN")
     */
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Parses and returns all claims from the JWT.
     *
     * @param token raw JWT string
     * @return parsed Claims object
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}