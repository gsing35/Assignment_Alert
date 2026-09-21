package com.assignment_alert.Assignment_Alert.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

// Updated config now requires everything except connecting to canvas to use a bearer token
// Endpoints no longer user a userId query bc it can be easily changed by just editiing URL so a person could access anyones data
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenAuthFilter tokenAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    // CHANGE THIS VALUE TO ACCEPT THE YOUR EXTENSION ID
    // DEFAULT: @Value("${}") Accept none
    // EXAMPLE: extenion id is qwertyuiopasdfghjklzxcvbnm 
    // @Value("${chrome-extension://qwertyuiopasdfghjklzxcvbnm}")
    @Value("${app.cors.allowed-origins:}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            /*
             * CSRF protection defends against a browser silently attaching AMBIENT
             * credentials (cookies) to a forged request. This API authenticates with an
             * explicit Authorization header that no other site can cause to be sent, so
             * there is nothing for CSRF to protect. Disabling it here is correct - please
             * do not "fix" this without first switching to cookie-based auth.
             */
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // No server-side sessions: every request carries its own bearer token.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Connecting is how a user gets a token, so it cannot itself require one.
                // It is protected by rate limiting instead - see RateLimitFilter.
                .requestMatchers("/api/v1/canvas/connect").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            )

            // Answer unauthenticated calls with a plain 401 rather than a login redirect,
            // which is meaningless to an extension calling fetch().
            .exceptionHandling(ex -> ex.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

            .addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(rateLimitFilter, TokenAuthFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
