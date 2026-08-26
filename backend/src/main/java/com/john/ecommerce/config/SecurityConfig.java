package com.john.ecommerce.config;

import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.context.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    @Value("${app.jwt.secret:john-ecommerce-dev-jwt-secret-change-me-32b+}")
    private String jwtSecret;

    @Value("${app.cors.origins:http://localhost:3022,http://localhost:3021}")
    private String corsOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/auth/**",
                        "/public/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/logistics/webhook/**"
                        ,"/payment/callback", "/refund/callback"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                        writeJsonError(response, objectMapper, 401, "未登录或登录已失效"))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                        writeJsonError(response, objectMapper, 403, "无权限访问"))
            )
            .addFilterBefore(new JwtAuthFilter(objectMapper), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void writeJsonError(HttpServletResponse response, ObjectMapper objectMapper,
                                       int code, String message) throws IOException {
        response.setStatus(code);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), java.util.Map.of(
                "code", code,
                "message", message
        ));
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

    @SuppressWarnings("unchecked")
    private static Set<String> parseIdentities(Object raw) {
        Set<String> out = new LinkedHashSet<>();
        if (raw instanceof Collection<?> col) {
            for (Object item : col) {
                if (item != null && !item.toString().isBlank()) {
                    out.add(item.toString().trim());
                }
            }
        }
        return out;
    }

    public class JwtAuthFilter extends OncePerRequestFilter {
        private final ObjectMapper objectMapper;

        JwtAuthFilter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            Long headerTenantId;
            try {
                headerTenantId = parseTenantHeader(request.getHeader(TENANT_HEADER));
            } catch (IllegalArgumentException e) {
                writeError(response, 400, e.getMessage());
                return;
            }
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
                    Set<String> identities = parseIdentities(claims.get("identities"));

                    if (headerTenantId != null && !headerTenantId.equals(tenantId)) {
                        writeError(response, 403, "请求租户与登录租户不一致");
                        return;
                    }
                    TenantContext.setTenantId(tenantId);
                    UserContext.setUserId(userId);
                    UserContext.setUserType(userType);
                    UserContext.setIdentities(identities);

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    for (String identity : identities) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + identity.toUpperCase()));
                    }
                    if (authorities.isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority(
                                "ROLE_" + (userType != null ? userType.toUpperCase() : "USER")));
                    }
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception ignored) {
                    // 带了无效/过期 token：直接 401，避免前端误当成「有登录态」继续打业务接口
                    writeError(response, 401, "未登录或登录已失效");
                    return;
                }
            } else if (headerTenantId != null) {
                TenantContext.setTenantId(headerTenantId);
            }
            try {
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
                UserContext.clear();
            }
        }

        private Long parseTenantHeader(String raw) {
            if (raw == null || raw.isBlank()) return null;
            try {
                long value = Long.parseLong(raw.trim());
                if (value <= 0) throw new NumberFormatException();
                return value;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("X-Tenant-Id 必须是正整数");
            }
        }

        private void writeError(HttpServletResponse response, int code, String message) throws IOException {
            response.setStatus(code);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), java.util.Map.of(
                    "code", code,
                    "message", message
            ));
        }
    }
}
