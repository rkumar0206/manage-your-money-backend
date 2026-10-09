package com.rtb.manageyourmoneybackend.firebase.util;

import com.google.cloud.Timestamp;
import com.rtb.manageyourmoneybackend.firebase.annotations.FirestoreCollection;
import com.rtb.manageyourmoneybackend.firebase.annotations.FirestoreId;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

public final class FirestoreMapper {

    private FirestoreMapper() {}

    public static Map<String, Object> toMap(Object model) {
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            for (Field f : allFields(model.getClass())) {
                int mod = f.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) continue;
                if (f.isAnnotationPresent(FirestoreId.class)) continue; // ID is the doc key, not a field
                f.setAccessible(true);
                map.put(f.getName(), convert(f.get(model)));
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read model " + model.getClass().getName(), e);
        }
        return map;
    }

    public static String getId(Object model) {
        try {
            for (Field f : allFields(model.getClass())) {
                if (f.isAnnotationPresent(FirestoreId.class)) {
                    f.setAccessible(true);
                    Object id = f.get(model);
                    if (id == null || id.toString().isBlank()) {
                        throw new IllegalArgumentException("@FirestoreId field is empty");
                    }
                    return id.toString();
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalArgumentException(model.getClass().getSimpleName() + " has no @FirestoreId field");
    }

    public static String getCollection(Class<?> type) {
        FirestoreCollection c = type.getAnnotation(FirestoreCollection.class);
        if (c == null) {
            throw new IllegalArgumentException(type.getSimpleName() + " is missing @FirestoreCollection");
        }
        return c.value();
    }

    public static void setId(Object model, String id) {
        try {
            for (Field f : allFields(model.getClass())) {
                if (f.isAnnotationPresent(FirestoreId.class)) {
                    f.setAccessible(true);
                    f.set(model, id);
                    return;
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object convert(Object v) {
        switch (v) {
            case null -> {
                return null;
            }
            case Instant i -> {
                return Timestamp.ofTimeSecondsAndNanos(i.getEpochSecond(), i.getNano());
            }
            case LocalDateTime l -> {
                return convert(l.toInstant(ZoneOffset.UTC));   // assumes UTC
            }
            case LocalDate d -> {
                return convert(d.atStartOfDay(ZoneOffset.UTC).toInstant());
            }
            case OffsetDateTime o -> {
                return convert(o.toInstant());
            }
            case ZonedDateTime z -> {
                return convert(z.toInstant());
            }
            case Date d -> {
                return Timestamp.of(d);
            }
            case BigDecimal b -> {
                return b.doubleValue();
            }
            case Enum<?> e -> {
                return e.name();
            }
            case UUID u -> {
                return u.toString();
            }
            default -> {
            }
        }
        if (v instanceof Number || v instanceof Boolean || v instanceof String || v instanceof Timestamp) return v;
        if (v instanceof Map<?, ?> m) {
            Map<String, Object> out = new LinkedHashMap<>();
            m.forEach((k, val) -> out.put(String.valueOf(k), convert(val)));
            return out;
        }
        if (v instanceof Collection<?> c) {
            List<Object> out = new ArrayList<>();
            c.forEach(x -> out.add(convert(x)));
            return out;
        }
        return toMap(v); // nested object
    }

    private static List<Field> allFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }
}
