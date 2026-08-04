package com.knowledge.base.foundation.websocket;

import com.knowledge.base.common.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("WebSocket Authorization header is required");
        }
        String token = authorization.substring(7).trim();
        if (!jwtTokenUtil.validateToken(token) || !jwtTokenUtil.isAccessToken(token)) {
            throw new IllegalArgumentException("WebSocket token is invalid or expired");
        }
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new IllegalArgumentException("WebSocket token has no user ID");
        }

        Principal principal = () -> String.valueOf(userId);
        accessor.setUser(principal);
        log.debug("WebSocket authenticated: userId={}", userId);
        return message;
    }
}
