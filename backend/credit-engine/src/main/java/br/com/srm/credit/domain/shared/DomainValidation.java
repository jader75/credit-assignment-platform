package br.com.srm.credit.domain.shared;

import java.util.function.Supplier;

public final class DomainValidation {

    private DomainValidation() {}

    public static void require(boolean condition, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (!condition) {
            throw exceptionSupplier.get();
        }
    }

    public static <T> T requireNonNull(T value, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (value == null) {
            throw exceptionSupplier.get();
        }
        return value;
    }

    public static String requireNotBlank(String value, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (value == null || value.isBlank()) {
            throw exceptionSupplier.get();
        }
        return value;
    }
}
