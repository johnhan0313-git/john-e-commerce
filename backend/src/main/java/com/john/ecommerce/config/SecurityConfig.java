package com.john.ecommerce.config;

import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.context.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.jwt.secret:change-me-in-production}")
    private String jwtSecret;

    @Value("${app.cors.origins:http://localhost:3020,http://localhost:3021}")
    private String corsOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/public/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(corsOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        return new UrlBasedCorsConfigurationSource() {{
            registerCorsConfiguration("/**", config);
        }};
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public class JwtAuthFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    String token = header.substring(7);
                    Claims claims = Jwts.parser()
                            .verifyWith(getSigningKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

                    Long userId = claims.get("userId", Long.class);
                    Long tenantId = claims.get("tenantId", Long.class);
                    String userType = claims.get("userType", String.class);

                    TenantContext.setTenantId(tenantId);
                    UserContext.setUserId(userId);
                    UserContext.setUserType(userType);

                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + (userType != null ? userType.toUpperCase() : "USER"))
                    );
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception ignored) {
                    // Invalid token - continue without auth
                }
            }
            try {
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
                UserContext.clear();
            }
        }
    }
}
