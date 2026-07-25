package com.hourai.prts.config;

import com.hourai.prts.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 安全过滤器：XSS 清洗 -> 频率限制 -> JWT 认证
        XssFilter xssFilter = new XssFilter();
        RateLimitFilter rateLimitFilter = new RateLimitFilter();

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})  // uses corsConfigurationSource bean from CorsConfig
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // CORS 预检与无需登录的公开接口
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/auth/register",
                        "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/ping",
                        "/api/v1/ping",
                        "/images/**",
                        "/api/v1/announcements",
                        "/api/v1/questions",
                        "/api/v1/questions/*",
                        "/api/v1/training/questions",
                        "/api/v1/training/questions/*",
                        "/api/v1/keywords",
                        "/api/v1/stats/question/*",
                        "/api/v1/stats/system").permitAll()

                // 管理端及题库写操作
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/questions",
                        "/api/v1/training/questions").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,
                        "/api/v1/questions/*",
                        "/api/v1/training/questions/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,
                        "/api/v1/questions/*",
                        "/api/v1/training/questions/*").hasRole("ADMIN")

                // 用户私有数据与正式考试
                .requestMatchers(
                        "/api/v1/auth/profile",
                        "/api/v1/auth/logout",
                        "/api/v1/exam/**",
                        "/api/v1/answers/**",
                        "/api/v1/user/**",
                        "/api/v1/notifications/**",
                        "/api/v1/stats/user").authenticated()

                // 未显式列入权限矩阵的接口默认拒绝
                .anyRequest().denyAll()
            )
            .addFilterBefore(xssFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter, XssFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
