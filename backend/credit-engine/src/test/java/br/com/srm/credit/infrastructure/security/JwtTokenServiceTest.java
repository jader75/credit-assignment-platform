package br.com.srm.credit.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
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
}
