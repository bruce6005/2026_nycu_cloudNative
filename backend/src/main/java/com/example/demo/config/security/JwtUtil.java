package com.example.demo.config.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.demo.modules.auth.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtUtil {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String secret;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret:dev-secret-change-me-dev-secret-change-me}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        long now = Instant.now().toEpochMilli();

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(user.getId()));
        payload.put("email", user.getEmail());
        payload.put("role", user.getRole() == null ? null : user.getRole().name());
        payload.put("iat", now / 1000);
        payload.put("exp", (now + expirationMs) / 1000);

        String encodedHeader = base64UrlJson(header);
        String encodedPayload = base64UrlJson(payload);
        String signingInput = encodedHeader + "." + encodedPayload;
        return signingInput + "." + sign(signingInput);
    }

    public JwtClaims validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String signingInput = parts[0] + "." + parts[1];
            String expectedSignature = sign(signingInput);
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                return null;
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, new TypeReference<>() {});

            Number exp = (Number) payload.get("exp");
            if (exp == null || exp.longValue() < Instant.now().getEpochSecond()) {
                return null;
            }

            Object sub = payload.get("sub");
            if (sub == null) {
                return null;
            }

            return new JwtClaims(Long.valueOf(String.valueOf(sub)), (String) payload.get("email"));
        } catch (Exception e) {
            return null;
        }
    }

    private String base64UrlJson(Map<String, Object> data) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(data);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot encode JWT JSON", e);
        }
    }

    private String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign JWT", e);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < leftBytes.length; i++) {
            result |= leftBytes[i] ^ rightBytes[i];
        }
        return result == 0;
    }

    public record JwtClaims(Long userId, String email) {}
}
