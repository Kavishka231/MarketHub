package com.markethub.auth;

import com.markethub.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private static final int MINIMUM_HS256_KEY_BYTES = 32;

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(
            @Value("${security.jwt.secret}") String encodedSecret,
            @Value("${security.jwt.expiration-minutes}") long expirationMinutes) {
        byte[] keyBytes = Decoders.BASE64.decode(encodedSecret);
        if (keyBytes.length < MINIMUM_HS256_KEY_BYTES) {
            throw new IllegalArgumentException("JWT secret must contain at least 256 bits");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("JWT expiration must be greater than zero minutes");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationSeconds = Math.multiplyExact(expirationMinutes, 60);
    }

    public String generateToken(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expirationSeconds, ChronoUnit.SECONDS);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean validateToken(String token, UserDetails user) {
        try {
            Claims claims = extractClaims(token);
            return claims.getSubject().equals(user.getUsername())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
