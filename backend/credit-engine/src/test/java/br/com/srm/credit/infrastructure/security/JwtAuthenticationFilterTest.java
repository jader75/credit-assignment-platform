package br.com.srm.credit.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenService);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPopulateSecurityContextWhenBearerTokenIsPresent() throws Exception {
        var authentication = new UsernamePasswordAuthenticationToken(
                "operator", null, List.of(new SimpleGrantedAuthority("ROLE_OPERATOR")));
        when(jwtTokenService.parse("token")).thenReturn(authentication);
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (req, res) -> {
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
        });

        verify(jwtTokenService).parse("token");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldIgnoreMissingAuthorizationHeader() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (req, res) -> {});

        verify(jwtTokenService, never()).parse(org.mockito.ArgumentMatchers.anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
