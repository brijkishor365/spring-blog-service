package com.qburst.blog_application.config;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ActuatorSecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // This MUST run before your main API security
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(EndpointRequest.toAnyEndpoint()) // Only applies to port 8084
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(EndpointRequest.to("health", "info")).permitAll() // Publicly available
                        .anyRequest().authenticated() // Everything else (metrics, env) needs login
                )
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults()); // Allows simple tools to login to metrics

        return http.build();
    }
}
