package com.knowledge.base.ai.filter;

import com.knowledge.base.common.utils.JwtTokenUtil;
import com.knowledge.base.common.utils.UserContextUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component @RequiredArgsConstructor
public class AiJwtContextFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        try {
            if (token != null && jwtTokenUtil.isAccessToken(token)) {
                UserContextUtil.setUserId(jwtTokenUtil.getUserIdFromToken(token));
                UserContextUtil.setToken(token);
            }
            chain.doFilter(request, response);
        } finally { UserContextUtil.clear(); }
    }
}
