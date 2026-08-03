package com.fellowlodge.api.security;

import com.fellowlodge.api.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Issues and validates JWT access tokens and refresh tokens.
 *
 * <p>The signing secret must be supplied via the {@code JWT_SECRET} environment
 * variable. When it is missing the application fails to start on the
 * {@code prod} profile; in development an ephemeral secret is generated so
 * tokens never survive a restart (which is fine for local testing).
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(AppProperties appProperties, Environment environment) {
        String secret = appProperties.getJwt().getSecret();
        if (secret == null || secret.isBlank()) {
            if (environment.acceptsProfiles(Profiles.of("prod", "supabase"))) {
                throw new IllegalStateException(
                        "JWT_SECRET is required when running with the 'prod' or 'supabase' profile. "
                        + "Generate one with e.g. 'openssl rand -base64 64' and export it before starting.");
            }
            byte[] bytes = new byte[48];
            new SecureRandom().nextBytes(bytes);
            secret = Base64.getEncoder().encodeToString(bytes);
            log.warn("No JWT_SECRET configured - generated an ephemeral signing secret for this run. "
                    + "Sessions will be invalidated on restart. Set JWT_SECRET for persistent dev sessions.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = appProperties.getJwt().getAccessTokenExpirationMs();
        this.refreshExpirationMs = appProperties.getJwt().getRefreshTokenExpirationMs();
    }

    public String generateAccessToken(AuthUser user) {
        return generateAccessToken(user.getId(), user.getUsername(), user.getRole(), user.getPermissions());
    }

    public String generateAccessToken(UUID userId, String username, String role, Set<String> permissions) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId.toString())
                .claim("role", role == null ? "GUEST" : role)
                .claim("permissions", permissions)
                .claim("typ", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessExpirationMs)))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("typ", "refresh")
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshExpirationMs)))
                .signWith(key)
                .compact();
    }

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseClaims(token).get("typ"));
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
