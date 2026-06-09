package org.example.apigateway.filter;


import lombok.extern.slf4j.Slf4j;
import org.example.apigateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Gateway filter that enforces JWT authentication on protected routes.
 *
 * Flow:
 * 1. Check for Authorization header with Bearer token
 * 2. Validate the token using JwtUtil
 * 3. If valid → extract username + role and forward as headers to downstream service
 * 4. If invalid → return 401 Unauthorized immediately
 *
 * Extends AbstractGatewayFilterFactory so it can be referenced by name
 * in application.yml route filters.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            log.debug("JwtAuthenticationFilter triggered for path: {}", path);

            // ── 1. Check Authorization header exists ────────────────────────
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header for path: {}", path);
                return onAuthenticationFailure(exchange, "Missing Authorization header");
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // ── 2. Validate Bearer format ────────────────────────────────────
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format for path: {}", path);
                return onAuthenticationFailure(exchange, "Invalid Authorization header format");
            }

            String token = authHeader.substring(7); // Strip "Bearer " prefix

            // ── 3. Validate the token ────────────────────────────────────────
            if (!jwtUtil.isTokenValid(token)) {
                log.warn("Invalid or expired JWT token for path: {}", path);
                return onAuthenticationFailure(exchange, "Invalid or expired JWT token");
            }

            // ── 4. Extract claims and mutate the request ─────────────────────
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            log.debug("JWT validated for user: {}, role: {}", username, role);

            // Forward user identity to downstream services via custom headers
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-Authenticated-User", username)
                    .header("X-Authenticated-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    /**
     * Writes a 401 Unauthorized response and terminates the filter chain.
     */
    private Mono<Void> onAuthenticationFailure(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "%s"
                }
                """.formatted(message);

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(bytes))
        );
    }

    /**
     * Config class required by AbstractGatewayFilterFactory.
     * Can be extended with per-route config properties if needed.
     */
    public static class Config {
        // Intentionally empty — no per-route config needed currently
    }
}