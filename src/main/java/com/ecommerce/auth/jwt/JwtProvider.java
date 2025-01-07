package com.ecommerce.auth.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import com.ecommerce.auth.exception.TokenExpiredException;
import com.ecommerce.auth.exception.TokenInvalidException;
import com.ecommerce.auth.exception.TokenMissingException;
import com.ecommerce.user.model.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtProvider {
    private final SecretKey atSecretKey;
    private final SecretKey rtSecretKey;
    private final long validityInMilliseconds;
    private final long rtValidityInMilliseconds;
    private final long tempValidityInMilliseconds;

    public JwtProvider(@Value("${jwt.secret-key}") String base64SecretKey,
                       @Value("${jwt.rt-secret-key}") String rtBase64SecretKey,
                       @Value("${jwt.validity-in-milliseconds}") long validityInMilliseconds,
                       @Value("${jwt.rt-validity-in-milliseconds}") long rtValidityInMilliseconds,
                       @Value("${jwt.temp-validity-in-milliseconds}") long tempValidityInMilliseconds) {
        this.validityInMilliseconds = validityInMilliseconds;
        this.rtValidityInMilliseconds = rtValidityInMilliseconds;
        this.tempValidityInMilliseconds = tempValidityInMilliseconds;

        this.atSecretKey = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(base64SecretKey));
        this.rtSecretKey = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(rtBase64SecretKey));
    }

    public String createTempToken(String sub) {
        return createToken(sub, UserRole.TEMP, tempValidityInMilliseconds, atSecretKey);
    }

    public String createRefreshToken(String sub) {
        return createToken(sub,  null, rtValidityInMilliseconds, rtSecretKey);
    }
    public String createAccessToken(String sub, UserRole role) {
        return createToken(sub, role, validityInMilliseconds, atSecretKey);
    }

    private String createToken(String sub, UserRole role, long validityInMilliseconds, SecretKey secretKey) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                                 .setSubject(sub)
                                 .setIssuedAt(now)
                                 .setExpiration(new Date(now.getTime() + validityInMilliseconds))
                                 .signWith(secretKey, SignatureAlgorithm.HS256);
        if(role != null) {
            builder.claim("role", role.name());
        }
        return builder.compact();
    }

    public boolean validateAccessToken(String token) {
        validate(token, atSecretKey);
        return true;
    }

    public boolean validateRefreshToken(String token) {
        validate(token, rtSecretKey);
        return true;
    }



    private Jws<Claims> validate(String token, SecretKey secretKey) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            log.error("Token has expired.");
            throw new TokenExpiredException("Token has expired.");
        } catch (SignatureException | MalformedJwtException e) {
            log.error("Token is invalid.");
            throw new TokenInvalidException("Token is invalid.");
        }
    }

    public String getSubjectFromAccessToken(String token) {
        Claims claims = getClaims(token, atSecretKey);
        return claims.getSubject(); // subject 추출
    }

    public String getSubjectFromRefreshToken(String token) {
        Claims claims = getClaims(token, rtSecretKey);
        return claims.getSubject(); // subject 추출
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token, atSecretKey);
        return claims.get("role", String.class); // role 추출
    }

    private Claims getClaims(String token, SecretKey secretKey) {
        return validate(token, secretKey).getBody();
    }

    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.error("Authorization header is missing or invalid.");
            throw new TokenMissingException("Authorization header is missing or invalid.");
        }
        return bearerToken.substring(7);
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("Refresh-Token");
        if(refreshToken == null) {
            log.error("Refresh-Token header is missing.");
            throw new TokenMissingException("Refresh-Token header is missing.");
        }
        return refreshToken;
    }
}
