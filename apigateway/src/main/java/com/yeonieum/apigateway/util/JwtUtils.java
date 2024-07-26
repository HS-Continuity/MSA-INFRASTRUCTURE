package com.yeonieum.apigateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;
    @Value("${jwt.token-validation-time}")
    private long TOKEN_VALIDATION_TIME; // 25분

    private final String ROLE = "role";

    private Key key;
    @PostConstruct
    public void init() {
        byte[] decodedBytes = Base64.getDecoder().decode(SECRET_KEY);
        key = Keys.hmacShaKeyFor(decodedBytes);
    }


    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
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


    public boolean validateToken(String token) {
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
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