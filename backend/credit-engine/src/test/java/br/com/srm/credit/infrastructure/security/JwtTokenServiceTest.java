package br.com.srm.credit.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-27T12:00:00Z"), ZoneOffset.UTC);
    private final JwtTokenService tokenService = new JwtTokenService(
            new ObjectMapper(),
            clock,
            "credit-engine",
            "credit-engine-dev-secret-change-me-please-32bytes",
            java.time.Duration.ofMinutes(15));

    @Test
    void shouldCreateAndParseJwtToken() {
        var token = tokenService.createToken("operator", List.of("OPERATOR"));

        var authentication = tokenService.parse(token);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("operator");
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_OPERATOR");
    }

    @Test
    void shouldCreateTokenWithEmptyRolesWhenRolesAreNull() {
        var token = tokenService.createToken("operator", null);

        var authentication = tokenService.parse(token);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities()).isEmpty();
    }

    @Test
    void shouldRejectMalformedExpiredOrTamperedTokens() {
        assertThat(tokenService.parse("not-a-jwt")).isNull();

        var expiredService = new JwtTokenService(
                new ObjectMapper(),
                Clock.fixed(Instant.parse("2026-06-27T11:00:00Z"), ZoneOffset.UTC),
                "credit-engine",
                "credit-engine-dev-secret-change-me-please-32bytes",
                java.time.Duration.ofMinutes(15));
        var expiredToken = expiredService.createToken("operator", List.of("OPERATOR"));
        assertThat(tokenService.parse(expiredToken)).isNull();

        var otherIssuerService = new JwtTokenService(
                new ObjectMapper(),
                clock,
                "other-issuer",
                "credit-engine-dev-secret-change-me-please-32bytes",
                java.time.Duration.ofMinutes(15));
        var otherIssuerToken = otherIssuerService.createToken("operator", List.of("OPERATOR"));
        assertThat(tokenService.parse(otherIssuerToken)).isNull();

        var validToken = tokenService.createToken("operator", List.of("OPERATOR"));
        var tamperedToken = validToken.substring(0, validToken.length() - 1) + "x";
        assertThat(tokenService.parse(tamperedToken)).isNull();
    }

    @Test
    void shouldParseTokenWithStringClaimsAndRolesAsString() {
        var token = buildToken(Map.of(
                "iss", "credit-engine",
                "sub", "operator",
                "iat", "1893456000",
                "exp", "1893459600",
                "roles", "OPERATOR"));

        var authentication = tokenService.parse(token);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("operator");
        assertThat(authentication.getAuthorities()).isEmpty();
    }

    @Test
    void shouldRejectTokenWhenSubjectIsNullButParseStringExpiration() {
        var claims = new LinkedHashMap<String, Object>();
        claims.put("iss", "credit-engine");
        claims.put("sub", null);
        claims.put("iat", "1893456000");
        claims.put("exp", "1893459600");
        claims.put("roles", List.of("OPERATOR"));

        var token = buildToken(claims);

        assertThat(tokenService.parse(token)).isNull();
    }

    private static String buildToken(Map<String, Object> claims) {
        try {
            var objectMapper = new ObjectMapper();
            var header = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
            var payload = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(new LinkedHashMap<>(claims)));
            var content = header + "." + payload;
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    "credit-engine-dev-secret-change-me-please-32bytes".getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));
            var signature = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
            return content + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
