package com.bing.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal fluent {@code toString()} helper, the in-repo replacement for Guava's
 * {@code MoreObjects.toStringHelper(...)} after the Guava dependency was dropped.
 *
 * <p>Mirrors the subset of the Guava API actually used in this project:
 * {@link #of(Object)} / {@link #of(Class)}, {@link #omitNullValues()},
 * {@link #add(String, Object)} (plus primitive overloads) and {@link #toString()}.
 * Existing call sites only need {@code MoreObjects.toStringHelper(x)} →
 * {@code ToStringHelper.of(x)}; the {@code .omitNullValues().add(...).toString()}
 * chain is unchanged.
 */
public final class ToStringHelper {

    private final String className;
    private final Map<String, Object> fields = new LinkedHashMap<>();
    private boolean omitNulls = false;

    private ToStringHelper(String className) {
        this.className = className;
    }

    public static ToStringHelper of(Object self) {
        return of(self.getClass());
    }

    public static ToStringHelper of(Class<?> clazz) {
        return new ToStringHelper(clazz.getSimpleName());
    }

    public ToStringHelper omitNullValues() {
        this.omitNulls = true;
        return this;
    }

    public ToStringHelper add(String name, Object value) {
        fields.put(name, value);
        return this;
    }

    public ToStringHelper add(String name, byte value) { fields.put(name, value); return this; }
    public ToStringHelper add(String name, short value) { fields.put(name, value); return this; }
    public ToStringHelper add(String name, int value) { fields.put(name, value); return this; }
    public ToStringHelper add(String name, long value) { fields.put(name, value); return this; }
    public ToStringHelper add(String name, float value) { fields.put(name, value); return this; }
    public ToStringHelper add(String name, double value) { fields.put(name, value); return this; }
    public ToStringHelper add(String name, boolean value) { fields.put(name, value); return this; }
    public ToStringHelper add(String name, char value) { fields.put(name, value); return this; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(className).append('{');
        boolean first = true;
        for (Map.Entry<String, Object> e : fields.entrySet()) {
            Object v = e.getValue();
            if (omitNulls && v == null) {
                continue;
            }
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(e.getKey()).append('=').append(v);
        }
        return sb.append('}').toString();
    }
}
