package com.digitaltwin.system.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 使用固定的密钥确保JWT在应用重启后仍然有效
    private static final String SECRET = "digital_twin_secret_key_for_jwt_signing_32_chars_min";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    
    // Token有效期7天
    public static final long JWT_TOKEN_VALIDITY = 7 * 24 * 60 * 60;

    // 从token中获取用户名
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // 从token中获取过期时间
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // 解析token中的所有声明
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).getBody();
    }

    // 检查token是否过期
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // 生成token
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userId, username);
    }

    // 创建token
    private String doGenerateToken(Map<String, Object> claims, Long userId, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .claim("userId", userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SECRET_KEY)
                .compact();
    }

    // 验证token
    public Boolean validateToken(String token, Long userId) {
        try {
            final Long tokenUserId = getUserIdFromToken(token);
            return (tokenUserId.equals(userId) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }
    
    // 从token中获取用户ID
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }
}