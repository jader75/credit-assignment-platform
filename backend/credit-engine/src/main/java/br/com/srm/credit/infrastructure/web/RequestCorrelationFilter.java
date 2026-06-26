package br.com.srm.credit.infrastructure.web;

import br.com.srm.credit.domain.shared.StructuredLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String TRANSACTION_HEADER_NAME = "X-Transaction-Id";
    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var correlationId = resolveCorrelationId(request);
        var transactionId = resolveTransactionId(request);
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(TRANSACTION_ID_KEY, transactionId);
        response.setHeader(HEADER_NAME, correlationId);
        response.setHeader(TRANSACTION_HEADER_NAME, transactionId);

        var startNanos = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            var durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            StructuredLog.info()
                    .step("end")
                    .append("method", request.getMethod())
                    .append("path", request.getRequestURI())
                    .append("status", response.getStatus())
                    .append("durationMs", durationMs)
                    .log();
            MDC.remove(CORRELATION_ID_KEY);
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }

    private static String resolveCorrelationId(HttpServletRequest request) {
        var correlationId = request.getHeader(HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return correlationId;
    }

    private static String resolveTransactionId(HttpServletRequest request) {
        var transactionId = request.getHeader(TRANSACTION_HEADER_NAME);
        if (transactionId == null || transactionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return transactionId;
    }
}
