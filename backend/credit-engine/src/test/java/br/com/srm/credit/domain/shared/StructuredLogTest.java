package br.com.srm.credit.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class StructuredLogTest {

    @Test
    void shouldRenderCompactStructuredEntryWithoutMessage() {
        var rendered = StructuredLog.info()
                .step("validation")
                .append("operationReference", "OP-001")
                .append("netAmount", "966.18")
                .append("codes", List.of("A", "B"))
                .render();

        assertThat(rendered).doesNotContain("msg=");
        assertThat(rendered).contains("step=validation");
        assertThat(rendered).contains("m=");
        assertThat(rendered).contains("l=");
        assertThat(rendered).contains("operationReference=OP-001");
        assertThat(rendered).contains("netAmount=966.18");
        assertThat(rendered).contains("codes=[A,B]");
    }

    @Test
    void shouldQuoteMessageWhenPresent() {
        var rendered =
                StructuredLog.debug().message("pricing.simulation.completed").render();

        assertThat(rendered).contains("msg=\"pricing.simulation.completed\"");
    }

    @Test
    void shouldAppendOnlyIncludedFieldsFromRecord() {
        var rendered = StructuredLog.info()
                .step("end")
                .append(new SampleEvent("OP-001", "ACTIVE", "hidden"), "operationReference", "status")
                .render();

        assertThat(rendered).contains("operationReference=OP-001");
        assertThat(rendered).contains("status=ACTIVE");
        assertThat(rendered).doesNotContain("secret");
    }

    @Test
    void shouldAppendAllNonExcludedFieldsFromObject() {
        var rendered = StructuredLog.info()
                .step("end")
                .appendExcluding(new SamplePojo("OP-001", "ACTIVE", "hidden"), "secret")
                .render();

        assertThat(rendered).contains("operationReference=OP-001");
        assertThat(rendered).contains("status=ACTIVE");
        assertThat(rendered).doesNotContain("secret");
    }

    private record SampleEvent(String operationReference, String status, String secret) {}

    private static final class SamplePojo {
        private final String operationReference;
        private final String status;
        private final String secret;

        private SamplePojo(String operationReference, String status, String secret) {
            this.operationReference = operationReference;
            this.status = status;
            this.secret = secret;
        }

        public String getOperationReference() {
            return operationReference;
        }

        public String getStatus() {
            return status;
        }

        public String getSecret() {
            return secret;
        }
    }
}
