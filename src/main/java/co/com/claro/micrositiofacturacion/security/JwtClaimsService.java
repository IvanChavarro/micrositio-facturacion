package co.com.claro.micrositiofacturacion.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtClaimsService {

    private final SecretKey key;
    private final String issuer;

    public JwtClaimsService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException exception) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes) for HS256");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = issuer;
    }

    public Claims extractClaims(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractSubject(String authorizationHeader) {
        return extractClaims(authorizationHeader).getSubject();
    }

    public String extractName(String authorizationHeader) {
        return extractClaims(authorizationHeader).get("name", String.class);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException("Missing Authorization header");
        }

        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new IllegalArgumentException("Authorization header must use Bearer token");
        }

        return authorizationHeader.substring(7).trim();
    }
}
