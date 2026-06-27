package com.jjc.mqtt.admin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Component
@Order(1)
public class JwtAuthFilter implements Filter {

    private static final Set<String> WHITE_LIST = Set.of(
            "/",
            "/v1/login",
            "/v1/check",
            "/v1/health",
            "/actuator/health",
            "/actuator/info"
    );

    private static final Set<String> WHITE_LIST_PREFIXES = Set.of(
            "/doc.html",
            "/webjars/",
            "/v3/api-docs",
            "/favicon",
            "/ui/",
            "/index.html",
            "/assets/"
    );

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();

        if (isWhiteListed(uri)) {
            chain.doFilter(request, response);
            return;
        }

        // 优先从 Authorization 头读取，其次从 URL 参数读取（SSE/EventSource 不支持自定义头）
        String token = extractToken(httpRequest);
        if (token != null && jwtUtils.isTokenValid(token)) {
            String username = jwtUtils.getUsernameFromToken(token);
            httpRequest.setAttribute("jwt_username", username);
            chain.doFilter(request, response);
            return;
        }

        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.getWriter().write(objectMapper.writeValueAsString(
                Map.of("code", 401, "msg", "认证失败或token已过期")));
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return request.getParameter("token");
    }

    private boolean isWhiteListed(String uri) {
        if (WHITE_LIST.contains(uri)) {
            return true;
        }
        for (String prefix : WHITE_LIST_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
