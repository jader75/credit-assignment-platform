package br.com.srm.credit.infrastructure.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtTokenService {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final String issuer;
    private final Duration expiration;
    private final SecretKeySpec secretKeySpec;

    public JwtTokenService(ObjectMapper objectMapper, Clock clock, String issuer, String secret, Duration expiration) {
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.issuer = issuer;
        this.expiration = expiration;
        this.secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String createToken(String subject, Collection<String> roles) {
        var now = Instant.now(clock);
        var claims = new LinkedHashMap<String, Object>();
        claims.put("iss", issuer);
        claims.put("sub", subject);
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", now.plus(expiration).getEpochSecond());
        claims.put("jti", UUID.randomUUID().toString());
        claims.put(
                "roles",
                roles == null
                        ? List.of()
                        : roles.stream()
                                .map(role -> role.toUpperCase(Locale.ROOT))
                                .toList());

        var payload = toJson(claims);
        var headerPart = ENCODER.encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        var payloadPart = ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        var signaturePart = sign(headerPart + "." + payloadPart);
        return headerPart + "." + payloadPart + "." + signaturePart;
    }

    public Authentication parse(String token) {
        var parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }

        var payloadJson = verifyAndDecodePayload(parts[0], parts[1], parts[2]);
        if (payloadJson == null) {
            return null;
        }

        try {
            var payload = objectMapper.readValue(payloadJson, Map.class);
            var subject = asString(payload.get("sub"));
            var issuerValue = asString(payload.get("iss"));
            var expirationEpochSecond = asLong(payload.get("exp"));
            var roles = asStringList(payload.get("roles"));

            if (subject == null || issuerValue == null || expirationEpochSecond == null) {
                return null;
            }
            if (!issuer.equals(issuerValue)) {
                return null;
            }
            if (Instant.now(clock).isAfter(Instant.ofEpochSecond(expirationEpochSecond))) {
                return null;
            }

            var authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
            return new UsernamePasswordAuthenticationToken(subject, null, authorities);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private String verifyAndDecodePayload(String headerPart, String payloadPart, String signaturePart) {
        var expectedSignature = sign(headerPart + "." + payloadPart);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8), signaturePart.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }
        try {
            return new String(DECODER.decode(payloadPart), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String sign(String content) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            return ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException exception) {
            throw new IllegalStateException("Nao foi possivel assinar o token.", exception);
        }
    }

    private String toJson(Map<String, Object> claims) {
        try {
            return objectMapper.writeValueAsString(claims);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Nao foi possivel gerar o token.", exception);
        }
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        return Long.valueOf(value.toString());
    }

    @SuppressWarnings("unchecked")
    private static List<String> asStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(item -> item == null ? null : item.toString())
                    .filter(item -> item != null)
                    .toList();
        }
        return List.of();
    }
}
