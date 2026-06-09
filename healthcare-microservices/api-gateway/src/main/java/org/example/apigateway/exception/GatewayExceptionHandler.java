package org.example.apigateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Global exception handler for the reactive API Gateway.
 * <p>
 * Catches:
 * - NotFoundException        → 503 Service Unavailable (downstream service down)
 * - ResponseStatusException  → passes through the embedded status code
 * - All other exceptions     → 500 Internal Server Error
 * <p>
 * Must be @Order(-1) to take priority over Spring Boot's DefaultErrorWebExceptionHandler.
 */
@Slf4j
@Component
@Order(-1)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus status;
        String message;

        if (ex instanceof NotFoundException) {
            // Eureka couldn't find a registered instance for the target service
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service is currently unavailable. Please try again later.";
            log.error("Service not found in registry: {}", ex.getMessage());

        } else if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : "Request error";
            log.warn("Response status exception: {}", rse.getMessage());

        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred in the gateway.";
            log.error("Unexpected gateway error: {}", ex.getMessage(), ex);
        }

        response.setStatusCode(status);

        String body = """
                {
                  "status": %d,
                  "error": "%s",
                  "message": "%s"
                }
                """.formatted(status.value(), status.getReasonPhrase(), message);

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(bytes))
        );
    }
}