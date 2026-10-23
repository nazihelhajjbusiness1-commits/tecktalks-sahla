package com.farmmanagement.backend.auth.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** Comma-separated browser origins allowed to call the API (CORS). */
    @Value("${APP_CORS_ALLOWED_ORIGINS:http://localhost:5173}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .toList()
        );
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                writeError(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized",
                        "Authentication is required to access this resource",
                        request.getRequestURI()
                );
    }


    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                writeError(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "Forbidden",
                        "You do not have permission to perform this action",
                        request.getRequestURI()
                );
    }

    private void writeError(
            HttpServletResponse response,
            int status,
            String error,
            String message,
            String path
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        String json = "{"
                + "\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"status\":" + status + ","
                + "\"error\":\"" + escape(error) + "\","
                + "\"message\":\"" + escape(message) + "\","
                + "\"path\":\"" + escape(path) + "\""
                + "}";

        response.getWriter().write(json);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .headers(headers -> headers
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // User creation is admin-only; login stays public above.
                        .requestMatchers(HttpMethod.POST, "/api/auth/register")
                        .hasRole("ADMIN")

                        // Payments (DT-72): accounting roles record; same roles view.
                        .requestMatchers(HttpMethod.POST, "/api/payments")
                        .hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT")

                        .requestMatchers(HttpMethod.GET, "/api/payments")
                        .hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT")

                        // Farmers
                        .requestMatchers(HttpMethod.POST, "/api/farmers")
                        .hasAnyRole("ADMIN", "MANAGER", "RECEIVING_EMPLOYEE")

                        // ACCOUNTANT included so farmer financial reads (balances,
                        // statement, payments, outstanding) pass the URL layer; the
                        // controllers' @PreAuthorize refines per endpoint.
                        .requestMatchers(HttpMethod.GET, "/api/farmers", "/api/farmers/**")
                        .hasAnyRole("ADMIN", "MANAGER", "RECEIVING_EMPLOYEE", "ACCOUNTANT")

                        .requestMatchers(HttpMethod.PUT, "/api/farmers/**")
                        .hasAnyRole("ADMIN", "MANAGER")

                        //products
                        .requestMatchers(HttpMethod.POST, "/api/products")
                        .hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**")
                        .hasAnyRole("ADMIN", "MANAGER", "RECEIVING_EMPLOYEE", "ACCOUNTANT", "WAREHOUSE_EMPLOYEE", "INSPECTOR")

                        .requestMatchers(HttpMethod.PUT, "/api/products/**")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // deliveries
                        .requestMatchers(HttpMethod.POST, "/api/deliveries")
                        .hasAnyRole("ADMIN", "MANAGER", "RECEIVING_EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/api/deliveries", "/api/deliveries/**")
                        .hasAnyRole("ADMIN", "MANAGER", "RECEIVING_EMPLOYEE", "ACCOUNTANT", "WAREHOUSE_EMPLOYEE", "INSPECTOR")

                        .requestMatchers(HttpMethod.PUT, "/api/deliveries/**")
                        .hasAnyRole("ADMIN", "MANAGER", "RECEIVING_EMPLOYEE")

                        .requestMatchers(HttpMethod.POST, "/api/deliveries/*/weight")
                        .hasAnyRole("ADMIN", "MANAGER", "RECEIVING_EMPLOYEE")

                        .requestMatchers(HttpMethod.POST, "/api/deliveries/*/grade")
                        .hasAnyRole("ADMIN", "MANAGER", "INSPECTOR")

                        .requestMatchers(HttpMethod.POST, "/api/deliveries/*/settlement/preview")
                        .hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT")

                        .requestMatchers(HttpMethod.POST, "/api/deliveries/*/settlement/calculate")
                        .hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT")

                        .requestMatchers(HttpMethod.POST, "/api/deliveries/*/settlement/confirm")
                        .hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT")

                        // grades (read is covered by the products GET matcher above)
                        .requestMatchers(HttpMethod.POST, "/api/products/*/grades")
                        .hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers(HttpMethod.PUT, "/api/grades/**")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // pricing (read is covered by the products GET matcher above)
                        .requestMatchers(HttpMethod.POST, "/api/products/*/prices")
                        .hasAnyRole("ADMIN", "MANAGER")

                        .requestMatchers(HttpMethod.PUT, "/api/prices/**")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // DT-82/83/87/88 (Sprint 5): management reporting surface.
                        // URL layer gates coarsely; each controller's @PreAuthorize
                        // refines per endpoint.
                        .requestMatchers(HttpMethod.GET, "/api/dashboard")
                        .hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT")

                        .requestMatchers(HttpMethod.GET, "/api/reports/**")
                        .hasAnyRole("ADMIN", "MANAGER", "ACCOUNTANT", "WAREHOUSE_EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/api/audit/**")
                        .hasAnyRole("ADMIN", "MANAGER")

                        // Admin-only user listing (login stays public above).
                        .requestMatchers(HttpMethod.GET, "/api/auth/users")
                        .hasRole("ADMIN")

                        // Inventory reads: warehouse + management/accounting roles.
                        .requestMatchers(HttpMethod.GET, "/api/inventory/**")
                        .hasAnyRole("ADMIN", "MANAGER", "WAREHOUSE_EMPLOYEE", "ACCOUNTANT")

                        // Quality dashboard: the inspector's surface + management.
                        .requestMatchers(HttpMethod.GET, "/api/quality/**")
                        .hasAnyRole("ADMIN", "MANAGER", "INSPECTOR")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
