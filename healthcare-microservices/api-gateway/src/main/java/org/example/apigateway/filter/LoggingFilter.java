package org.example.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Global logging filter — runs for EVERY request through the gateway.
 * <p>
 * Logs:
 * - Incoming request method + path + remote address
 * - Response status code
 * - Total request duration in ms
 * <p>
 * Implements Ordered with HIGHEST_PRECEDENCE to run first in the filter chain.
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = Instant.now().toEpochMilli();

        log.info("Incoming Request: method={} path={} remoteAddr={}",
                request.getMethod(),
                request.getPath(),
                request.getRemoteAddress());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = Instant.now().toEpochMilli() - startTime;
            log.info("Outgoing Response: status={} path={} duration={}ms",
                    exchange.getResponse().getStatusCode(),
                    request.getPath(),
                    duration);
        }));
    }

    @Override
    public int getOrder() {
        // Run before all other filters
        return Ordered.HIGHEST_PRECEDENCE;
    }
}