package br.com.srm.credit.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
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

    @Test
    void shouldRenderValuesFromDifferentTypesAndQuoteWhenNeeded() {
        var rendered = StructuredLog.warn()
                .message("pricing simulation completed")
                .append("flag", true)
                .append("quantity", 12)
                .append("tags", List.of("alpha", "beta"))
                .append("attributes", attributes())
                .append("matrix", new int[] {1, 2, 3})
                .append(" ", "ignored")
                .appendExcluding(new FieldOnlyPojo("OP-002", "READY"), "missing")
                .append(new BooleanGetterPojo(true), "active")
                .render();

        assertThat(rendered).contains("msg=\"pricing simulation completed\"");
        assertThat(rendered).contains("flag=true");
        assertThat(rendered).contains("quantity=12");
        assertThat(rendered).contains("tags=[alpha,beta]");
        assertThat(rendered).contains("attributes={origin=api,step=\"final review\"}");
        assertThat(rendered).contains("matrix=[1,2,3]");
        assertThat(rendered).contains("operationReference=OP-002");
        assertThat(rendered).contains("status=READY");
        assertThat(rendered).contains("active=true");
        assertThat(rendered).doesNotContain("ignored");
    }

    @Test
    void shouldFailWhenRequestedFieldDoesNotExist() {
        assertThatThrownBy(() -> StructuredLog.info()
                        .append(new SamplePojo("OP-001", "ACTIVE", "hidden"), "missing")
                        .render())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Campo nao encontrado");
    }

    @Test
    void shouldIgnoreNullObjectsAndEmptyIncludes() {
        var rendered = StructuredLog.info()
                .append((Object) null, "operationReference")
                .append(new SamplePojo("OP-001", "ACTIVE", "hidden"), (String[]) null)
                .appendExcluding(null, "secret")
                .render();

        assertThat(rendered).contains("m=");
        assertThat(rendered).contains("l=");
        assertThat(rendered).doesNotContain("operationReference=OP-001");
        assertThat(rendered).doesNotContain("status=ACTIVE");
    }

    private record SampleEvent(String operationReference, String status, String secret) {}

    private static LinkedHashMap<String, Object> attributes() {
        var attributes = new LinkedHashMap<String, Object>();
        attributes.put("origin", "api");
        attributes.put("step", "final review");
        return attributes;
    }

    private static final class FieldOnlyPojo {
        private final String operationReference;
        private final String status;

        private FieldOnlyPojo(String operationReference, String status) {
            this.operationReference = operationReference;
            this.status = status;
        }
    }

    private static final class BooleanGetterPojo {
        private final boolean active;

        private BooleanGetterPojo(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }
    }

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
