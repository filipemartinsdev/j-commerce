package com.products.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configure(http))

                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/admin/api/v1/stock/**").hasAnyAuthority("SCOPE_STOCK_MANAGER", "SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/admin/api/v1/products/**").hasAnyAuthority("SCOPE_STOCK_MANAGER", "SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/admin/api/v1/prices/**").hasAnyAuthority("SCOPE_STOCK_MANAGER", "SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/admin/api/v1/skus/**").hasAnyAuthority("SCOPE_STOCK_MANAGER", "SCOPE_ADMIN")
                        .requestMatchers("/admin/**").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/index.html", "/h2-console/**").permitAll()
                        .requestMatchers("/api/v1/register", "/api/v1/login", "/api/v1/refresh").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth -> oauth
                        .jwt(Customizer.withDefaults())
                )

                .build();
    }
}
