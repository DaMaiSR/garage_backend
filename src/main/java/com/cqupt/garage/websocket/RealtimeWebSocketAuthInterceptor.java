package com.cqupt.garage.websocket;

import com.cqupt.garage.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class RealtimeWebSocketAuthInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        String token = servletRequest.getHeader(jwtUtils.getHeader());
        if (isBlank(token)) {
            token = servletRequest.getParameter("token");
        }
        if (isBlank(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        try {
            Claims claims = jwtUtils.getTokenClaim(token);
            if (claims == null || jwtUtils.isTokenExpired(claims.getExpiration())) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            String role = claims.get("role", String.class);
            if (!"admin".equals(role)) {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return false;
            }
            attributes.put("userId", claims.get("userId"));
            attributes.put("username", claims.getSubject());
            return true;
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
