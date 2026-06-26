package br.com.srm.credit.domain.shared;

import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StructuredLog {

    private StructuredLog() {}

    public static Entry info() {
        return new Entry(Level.INFO, null);
    }

    public static Entry warn() {
        return new Entry(Level.WARN, null);
    }

    public static Entry debug() {
        return new Entry(Level.DEBUG, null);
    }

    public static Entry trace() {
        return new Entry(Level.TRACE, null);
    }

    public static Entry error() {
        return new Entry(Level.ERROR, null);
    }

    public static Entry error(Throwable throwable) {
        return new Entry(Level.ERROR, throwable);
    }

    public enum Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    public static final class Entry {

        private static final StackWalker STACK_WALKER =
                StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

        private final Level level;
        private final Throwable throwable;
        private final Map<String, Object> fields = new LinkedHashMap<>();
        private final Logger logger;
        private String message;

        private Entry(Level level, Throwable throwable) {
            this.level = level;
            this.throwable = throwable;
            this.logger = LoggerFactory.getLogger(resolveCallerClass());
        }

        public Entry message(String message) {
            this.message = message;
            return this;
        }

        public Entry step(String step) {
            return append("step", step);
        }

        public Entry append(String key, Object value) {
            if (key != null && !key.isBlank()) {
                fields.put(key, value);
            }
            return this;
        }

        public Entry append(Object object, String... includeFields) {
            if (object == null || includeFields == null || includeFields.length == 0) {
                return this;
            }
            for (var fieldName : includeFields) {
                appendField(object, fieldName);
            }
            return this;
        }

        public Entry appendExcluding(Object object, String... excludeFields) {
            if (object == null) {
                return this;
            }

            var excluded = new LinkedHashSet<String>();
            if (excludeFields != null) {
                for (var fieldName : excludeFields) {
                    if (fieldName != null && !fieldName.isBlank()) {
                        excluded.add(fieldName);
                    }
                }
            }

            for (var fieldName : extractFieldNames(object.getClass())) {
                if (!excluded.contains(fieldName)) {
                    appendField(object, fieldName);
                }
            }
            return this;
        }

        public Entry fields(Map<String, ?> values) {
            if (values != null) {
                values.forEach(this::append);
            }
            return this;
        }

        public void log() {
            var payload = render();
            switch (level) {
                case TRACE -> logger.trace(payload);
                case DEBUG -> logger.debug(payload);
                case INFO -> logger.info(payload);
                case WARN -> logger.warn(payload);
                case ERROR -> {
                    if (throwable == null) {
                        logger.error(payload);
                    } else {
                        logger.error(payload, throwable);
                    }
                }
            }
        }

        String render() {
            var joiner = new StringJoiner(" ");
            if (message != null && !message.isBlank()) {
                joiner.add("msg=" + quoted(message));
            }

            var caller = resolveCallerFrame();
            joiner.add("m=" + quotedOrRaw(caller.getMethodName()));
            joiner.add("l=" + caller.getLineNumber());

            for (var entry : fields.entrySet()) {
                joiner.add(entry.getKey() + "=" + renderValue(entry.getValue()));
            }

            return joiner.toString();
        }

        private static Class<?> resolveCallerClass() {
            return resolveCallerFrame().getDeclaringClass();
        }

        private static StackFrame resolveCallerFrame() {
            return STACK_WALKER.walk(stream -> stream.filter(frame -> {
                        var className = frame.getDeclaringClass().getName();
                        return !className.equals(StructuredLog.class.getName())
                                && !className.equals(StructuredLog.Entry.class.getName());
                    })
                    .findFirst()
                    .orElseThrow());
        }

        private static String renderValue(Object value) {
            if (value == null) {
                return "null";
            }
            if (value instanceof CharSequence sequence) {
                return quotedOrRaw(sequence.toString());
            }
            if (value instanceof Number || value instanceof Boolean) {
                return value.toString();
            }
            if (value instanceof Iterable<?> iterable) {
                var joiner = new StringJoiner(",", "[", "]");
                iterable.forEach(item -> joiner.add(renderValue(item)));
                return joiner.toString();
            }
            if (value.getClass().isArray()) {
                var joiner = new StringJoiner(",", "[", "]");
                var length = Array.getLength(value);
                for (var index = 0; index < length; index++) {
                    joiner.add(renderValue(Array.get(value, index)));
                }
                return joiner.toString();
            }
            if (value instanceof Map<?, ?> map) {
                var joiner = new StringJoiner(",", "{", "}");
                map.forEach((k, v) -> joiner.add(k + "=" + renderValue(v)));
                return joiner.toString();
            }
            return quotedOrRaw(Objects.toString(value));
        }

        private static String quotedOrRaw(String value) {
            if (value == null) {
                return "null";
            }
            if (value.matches("[A-Za-z0-9_.:/-]+")) {
                return value;
            }
            return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }

        private static String quoted(String value) {
            if (value == null) {
                return "null";
            }
            return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }

        private void appendField(Object object, String fieldName) {
            if (fieldName == null || fieldName.isBlank()) {
                return;
            }
            var value = extractValue(object, fieldName);
            fields.put(fieldName, value);
        }

        private static Object extractValue(Object object, String fieldName) {
            var type = object.getClass();

            if (type.isRecord()) {
                return extractRecordValue(object, type, fieldName);
            }

            var field = findField(type, fieldName);
            if (field != null) {
                try {
                    field.setAccessible(true);
                    return field.get(object);
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException("Falha ao ler o campo " + fieldName, exception);
                }
            }

            var getter = findGetter(type, fieldName);
            if (getter != null) {
                try {
                    return getter.invoke(object);
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException("Falha ao ler o campo " + fieldName, exception);
                }
            }

            throw new IllegalArgumentException("Campo nao encontrado: " + fieldName);
        }

        private static Object extractRecordValue(Object object, Class<?> type, String fieldName) {
            try {
                for (var component : type.getRecordComponents()) {
                    if (component.getName().equals(fieldName)) {
                        return component.getAccessor().invoke(object);
                    }
                }
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Falha ao ler o campo " + fieldName, exception);
            }
            throw new IllegalArgumentException("Campo nao encontrado: " + fieldName);
        }

        private static Field findField(Class<?> type, String fieldName) {
            var currentType = type;
            while (currentType != null && !Object.class.equals(currentType)) {
                try {
                    return currentType.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignored) {
                    currentType = currentType.getSuperclass();
                }
            }
            return null;
        }

        private static Method findGetter(Class<?> type, String fieldName) {
            var capitalized = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            try {
                return type.getMethod("get" + capitalized);
            } catch (NoSuchMethodException ignored) {
                try {
                    return type.getMethod("is" + capitalized);
                } catch (NoSuchMethodException ignoredAgain) {
                    return null;
                }
            }
        }

        private static Set<String> extractFieldNames(Class<?> type) {
            var names = new LinkedHashSet<String>();
            if (type.isRecord()) {
                for (var component : type.getRecordComponents()) {
                    names.add(component.getName());
                }
                return names;
            }

            for (var currentType = type;
                    currentType != null && !Object.class.equals(currentType);
                    currentType = currentType.getSuperclass()) {
                for (var field : currentType.getDeclaredFields()) {
                    if (!field.isSynthetic()) {
                        names.add(field.getName());
                    }
                }
            }
            return names;
        }
    }
}
