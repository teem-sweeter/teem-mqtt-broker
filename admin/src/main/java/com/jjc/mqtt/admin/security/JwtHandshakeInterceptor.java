package com.jjc.mqtt.admin.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    public JwtHandshakeInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, java.util.Map<String, Object> attributes) {
        List<String> tokens = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .get("token");

        if (tokens != null && !tokens.isEmpty()) {
            String token = tokens.get(0);
            if (jwtUtils.isTokenValid(token)) {
                attributes.put("jwt_username", jwtUtils.getUsernameFromToken(token));
                return true;
            }
        }

        if (response instanceof HttpServletResponse httpResponse) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
