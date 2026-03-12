package com.cqupt.garage.security;

import com.cqupt.garage.utils.JwtUtils;
import com.cqupt.garage.utils.ResultVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> WHITE_LIST = java.util.Arrays.asList(
            "/user/login",
            "/user/register",
            "/error",
            "/favicon.ico",
            "/ws/realtime/**"
    );

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isWhitePath(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(jwtUtils.getHeader());
        if (isBlank(token)) {
            token = request.getParameter("token");
        }
        if (isBlank(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtUtils.getTokenClaim(token);
            if (claims == null || jwtUtils.isTokenExpired(claims.getExpiration())) {
                writeTokenError(response, "登录已过期，请重新登录");
                return;
            }
            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);
            String username = claims.getSubject();
            request.setAttribute("currentUserId", userId);
            request.setAttribute("currentRole", role);
            request.setAttribute("currentUsername", username);

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + String.valueOf(role).toUpperCase());
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException e) {
            writeTokenError(response, "登录状态无效，请重新登录");
        } catch (Exception e) {
            writeTokenError(response, "登录状态无效，请重新登录");
        }
    }

    private boolean isWhitePath(String path) {
        if (path == null) {
            return false;
        }
        for (String white : WHITE_LIST) {
            if (pathMatcher.match(white, path)) {
                return true;
            }
        }
        return false;
    }

    private void writeTokenError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ResultVo.fail(message, "token_error"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
