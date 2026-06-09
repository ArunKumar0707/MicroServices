package org.example.eurekaserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Eureka Server dashboard.
 *
 * Enables HTTP Basic Auth on the dashboard while disabling CSRF
 * (required for Eureka clients to register via POST).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        // Eureka clients send POST requests to register — CSRF must be disabled
                        .ignoringRequestMatchers("/eureka/**")
                )
                .authorizeHttpRequests(auth -> auth
                        // Actuator endpoints are open for internal monitoring
                        .requestMatchers("/actuator/**").permitAll()
                        // All other requests (dashboard + API) require authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {}); // Enable HTTP Basic Auth for the dashboard

        return http.build();
    }
}
