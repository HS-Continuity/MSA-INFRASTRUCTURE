package com.yeonieum.apigateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("${jwt.secret-key}")
    private final String SECRET_KEY;
    @Value("${jwt.token-validation-time}")
    private final long TOKEN_VALIDATION_TIME; // 25분

    private final String ROLE = "role";

    public JwtUtils(String secret_key, long token_validation_time) {
        SECRET_KEY = secret_key;
        TOKEN_VALIDATION_TIME = token_validation_time;
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public String getRole(String token) {
        return extractAllClaims(token).get(ROLE, String.class);
    }

    private Key getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        try{
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            System.out.println("Invalid JWT Signature");
        } catch (ExpiredJwtException e) {
            System.out.println("Expired JWT");
        } catch (UnsupportedJwtException e) {
            System.out.println("Unsupported JWT");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT claim is empty");
        }
        return false;
    }
}