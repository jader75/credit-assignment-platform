package br.com.srm.credit.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DomainValidationTest {

    @Test
    void shouldReturnValueWhenConditionIsTrue() {
        assertThatCode(() -> DomainValidation.require(true, IllegalStateException::new))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowWhenConditionIsFalse() {
        assertThatThrownBy(() -> DomainValidation.require(false, IllegalStateException::new))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldReturnNonNullValue() {
        var value = DomainValidation.requireNonNull("value", IllegalStateException::new);

        assertThat(value).isEqualTo("value");
    }

    @Test
    void shouldThrowWhenValueIsNull() {
        assertThatThrownBy(() -> DomainValidation.requireNonNull(null, IllegalStateException::new))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldReturnNonBlankValue() {
        var value = DomainValidation.requireNotBlank("value", IllegalStateException::new);

        assertThat(value).isEqualTo("value");
    }

    @Test
    void shouldThrowWhenValueIsBlank() {
        assertThatThrownBy(() -> DomainValidation.requireNotBlank(" ", IllegalStateException::new))
                .isInstanceOf(IllegalStateException.class);
    }
}
