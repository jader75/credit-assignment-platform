package br.com.srm.credit.infrastructure.web;

import br.com.srm.credit.domain.pricing.PricingBusinessException;
import br.com.srm.credit.domain.pricing.PricingValidationException;
import br.com.srm.credit.domain.shared.StructuredLog;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PricingValidationException.class)
    public ResponseEntity<ApiErrorResponse> handlePricingValidation(
            PricingValidationException exception, HttpServletRequest request) {
        StructuredLog.warn()
                .step("validation")
                .append("type", exception.getClass().getSimpleName())
                .append("path", request.getRequestURI())
                .append("reason", exception.getMessage())
                .log();
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(PricingBusinessException.class)
    public ResponseEntity<ApiErrorResponse> handlePricingBusiness(
            PricingBusinessException exception, HttpServletRequest request) {
        StructuredLog.warn()
                .step("business")
                .append("type", exception.getClass().getSimpleName())
                .append("path", request.getRequestURI())
                .append("reason", exception.getMessage())
                .log();
        return build(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        var message = exception.getBindingResult().getFieldError() != null
                ? exception.getBindingResult().getFieldError().getDefaultMessage()
                : "Requisicao invalida.";
        StructuredLog.warn()
                .step("validation")
                .append("type", exception.getClass().getSimpleName())
                .append("path", request.getRequestURI())
                .append("reason", message)
                .log();
        return build(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        StructuredLog.warn()
                .step("validation")
                .append("type", exception.getClass().getSimpleName())
                .append("path", request.getRequestURI())
                .append("reason", "Corpo da requisicao invalido.")
                .log();
        return build(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido.", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        StructuredLog.error(exception)
                .step("error")
                .append("type", exception.getClass().getSimpleName())
                .append("path", request.getRequestURI())
                .append("reason", exception.getMessage())
                .log();
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado.", request.getRequestURI());
    }

    private static ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(
                        OffsetDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        path,
                        MDC.get(RequestCorrelationFilter.CORRELATION_ID_KEY)));
    }
}
