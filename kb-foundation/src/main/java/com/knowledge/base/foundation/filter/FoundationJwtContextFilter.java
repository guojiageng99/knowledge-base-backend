package com.knowledge.base.foundation.filter;

import com.knowledge.base.common.utils.JwtTokenUtil;
import com.knowledge.base.common.utils.UserContextUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Populates the shared request context for foundation endpoints that need the
 * authenticated user's identifier, such as notification operations.
 */
@Component
@RequiredArgsConstructor
public class FoundationJwtContextFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7) : authorization;
        try {
            if (token != null && jwtTokenUtil.isAccessToken(token)) {
                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                if (userId != null) {
                    UserContextUtil.setUserId(userId);
                    UserContextUtil.setToken(token);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContextUtil.clear();
        }
    }
}
