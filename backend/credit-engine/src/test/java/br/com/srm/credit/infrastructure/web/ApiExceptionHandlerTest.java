package br.com.srm.credit.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import br.com.srm.credit.domain.pricing.PricingBusinessException;
import br.com.srm.credit.domain.pricing.PricingMessage;
import br.com.srm.credit.domain.pricing.PricingValidationException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldHandlePricingValidationException() {
        MDC.put(RequestCorrelationFilter.CORRELATION_ID_KEY, "corr-001");
        try {
            var response = handler.handlePricingValidation(
                    new PricingValidationException(PricingMessage.PRICING_REQUEST_INVALID),
                    request("/api/v1/pricing/simulations"));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).isEqualTo(PricingMessage.PRICING_REQUEST_INVALID.message());
            assertThat(response.getBody().correlationId()).isEqualTo("corr-001");
        } finally {
            MDC.clear();
        }
    }

    @Test
    void shouldHandlePricingBusinessException() {
        var response = handler.handlePricingBusiness(
                new PricingBusinessException(PricingMessage.PRICING_RULE_MISMATCH),
                request("/api/v1/pricing/simulations"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo(PricingMessage.PRICING_RULE_MISMATCH.message());
    }

    @Test
    void shouldUseFieldErrorMessageWhenValidationHasFieldErrors() throws Exception {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "payload");
        bindingResult.addError(new FieldError("payload", "operationReference", "Referencia obrigatoria."));
        var exception = new MethodArgumentNotValidException(dummyMethodParameter(), bindingResult);

        var response = handler.handleValidation(exception, request("/api/v1/pricing/simulations"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Referencia obrigatoria.");
    }

    @Test
    void shouldUseDefaultMessageWhenValidationHasNoFieldErrors() throws Exception {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "payload");
        var exception = new MethodArgumentNotValidException(dummyMethodParameter(), bindingResult);

        var response = handler.handleValidation(exception, request("/api/v1/pricing/simulations"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).extracting(ApiErrorResponse::message).isEqualTo("Requisição inválida.");
    }

    @Test
    void shouldHandleMalformedJsonPayload() {
        var response = handler.handleNotReadable(
                new HttpMessageNotReadableException("malformed", null, mock(HttpInputMessage.class)),
                request("/api/v1/pricing/simulations"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).extracting(ApiErrorResponse::message).isEqualTo("Corpo da requisição inválido.");
    }

    @Test
    void shouldHandleUnexpectedException() {
        var response =
                handler.handleUnexpected(new IllegalStateException("boom"), request("/api/v1/pricing/simulations"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .extracting(ApiErrorResponse::message)
                .isEqualTo("Não foi possível processar a requisição.");
    }

    private static MockHttpServletRequest request(String uri) {
        var request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }

    private static MethodParameter dummyMethodParameter() throws NoSuchMethodException {
        return new MethodParameter(ApiExceptionHandlerTest.class.getDeclaredMethod("sampleMethod", String.class), 0);
    }

    @SuppressWarnings("unused")
    private void sampleMethod(String value) {}
}
