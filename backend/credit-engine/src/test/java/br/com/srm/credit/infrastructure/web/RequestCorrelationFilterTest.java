package br.com.srm.credit.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestCorrelationFilterTest {

    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();

    @Test
    void shouldReuseIncomingCorrelationIdAndExposeItInResponseHeader() throws ServletException, IOException {
        var request = new MockHttpServletRequest("GET", "/api/v1/pricing/simulations");
        request.addHeader(RequestCorrelationFilter.HEADER_NAME, "corr-123");
        request.addHeader(RequestCorrelationFilter.TRANSACTION_HEADER_NAME, "tx-123");
        var response = new MockHttpServletResponse();

        try {
            filter.doFilter(request, response, new MockFilterChain());

            assertThat(response.getHeader(RequestCorrelationFilter.HEADER_NAME)).isEqualTo("corr-123");
            assertThat(response.getHeader(RequestCorrelationFilter.TRANSACTION_HEADER_NAME))
                    .isEqualTo("tx-123");
        } finally {
            MDC.clear();
        }
    }

    @Test
    void shouldGenerateCorrelationIdWhenMissing() throws ServletException, IOException {
        var request = new MockHttpServletRequest("GET", "/api/v1/pricing/simulations");
        var response = new MockHttpServletResponse();

        try {
            filter.doFilter(request, response, new MockFilterChain());

            assertThat(response.getHeader(RequestCorrelationFilter.HEADER_NAME)).isNotBlank();
            assertThat(response.getHeader(RequestCorrelationFilter.TRANSACTION_HEADER_NAME))
                    .isNotBlank();
        } finally {
            MDC.clear();
        }
    }
}
