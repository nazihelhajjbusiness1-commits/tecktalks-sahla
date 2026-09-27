package com.farmmanagement.backend.auth.config;

import jakarta.servlet.http.HttpServletResponse;
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
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
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

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Farmers
                        .requestMatchers(HttpMethod.POST, "/api/farmers")
                        .hasAnyRole("ADMIN", "MANAGER", "RECEIVING_EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/api/farmers", "/api/farmers/**")
                        .hasAnyRole("ADMIN", "MANAGER", "RECEIVING_EMPLOYEE")

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

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
