package br.com.srm.credit.infrastructure.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.srm.credit.infrastructure.security.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class AuthControllerTest {

    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtTokenService tokenService = new JwtTokenService(
            new ObjectMapper(),
            Clock.fixed(Instant.parse("2026-06-27T12:00:00Z"), ZoneOffset.UTC),
            "credit-engine",
            "credit-engine-dev-secret-change-me-please-32bytes",
            java.time.Duration.ofMinutes(15));
    private final AuthController controller = new AuthController(
            authenticationManager,
            tokenService,
            Clock.fixed(Instant.parse("2026-06-27T12:00:00Z"), ZoneOffset.UTC),
            15);

    @Test
    void shouldReturnBearerTokenOnSuccessfulLogin() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(
                        "operator", "secret", List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))));

        var response = controller.login(new LoginRequest("operator", "secret"));

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.subject()).isEqualTo("operator");
        assertThat(response.roles()).containsExactly("OPERATOR");
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.expiresAt()).isNotNull();
    }
}
