package br.com.srm.credit.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class RequestCorrelationFilterBranchTest {

    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGenerateHeadersAndResolveAuthenticatedUser() throws ServletException, IOException {
        var request = new MockHttpServletRequest("GET", "/api/v1/pricing/simulations");
        var response = new MockHttpServletResponse();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        "operator", null, java.util.List.of(new SimpleGrantedAuthority("ROLE_OPERATOR"))));

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(RequestCorrelationFilter.HEADER_NAME)).isNotBlank();
        assertThat(response.getHeader(RequestCorrelationFilter.TRANSACTION_HEADER_NAME))
                .isNotBlank();
    }

    @Test
    void shouldReuseBlankHeadersByGeneratingNewIds() throws ServletException, IOException {
        var request = new MockHttpServletRequest("GET", "/api/v1/pricing/simulations");
        request.addHeader(RequestCorrelationFilter.HEADER_NAME, " ");
        request.addHeader(RequestCorrelationFilter.TRANSACTION_HEADER_NAME, " ");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(RequestCorrelationFilter.HEADER_NAME)).isNotBlank();
        assertThat(response.getHeader(RequestCorrelationFilter.TRANSACTION_HEADER_NAME))
                .isNotBlank();
    }
}
