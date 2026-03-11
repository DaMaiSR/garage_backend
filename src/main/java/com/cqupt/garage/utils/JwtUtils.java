package com.cqupt.garage.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${config.jwt.secret}")
    private String secret;

    @Value("${config.jwt.expire}")
    private long expire;

    @Value("${config.jwt.header}")
    private String header;

    public String createToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire * 1000);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(username)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Claims getTokenClaim(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    public String getHeader() {
        return header;
    }
}
