package com.hourai.prts.security;

import com.hourai.prts.entity.User;
import com.hourai.prts.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 兼容旧版 Token 格式: "user-{id}"
 * 在 JWT filter 之前运行，仅当 JWT 未通过时作为降级方案。
 * 优先级低于 JwtAuthenticationFilter。
 */
@Component
@Order(1)
public class LegacyTokenFilter extends OncePerRequestFilter {

    private final UserService userService;

    public LegacyTokenFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Only activate if no JWT auth is present
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer user-")) {
            String legacyToken = bearerToken.substring(7); // "user-{id}"
            try {
                Long userId = Long.parseLong(legacyToken.substring(5)); // Remove "user-"
                Optional<User> userOpt = userService.getById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    List<SimpleGrantedAuthority> authorities = user.getIsAdmin()
                            ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"))
                            : List.of(new SimpleGrantedAuthority("ROLE_USER"));

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (NumberFormatException ignored) {
                // Invalid legacy token format - fall through
            }
        }

        filterChain.doFilter(request, response);
    }
}
