package com.rtb.manageyourmoneybackend.common.util;

import java.math.BigDecimal;

public class CommonAppUtil {

    private CommonAppUtil() {}

    /**
     * JPQL's avg(...) return type isn't guaranteed to be BigDecimal across every
     * Hibernate version (it can come back as Double), so this normalizes whatever
     * Number comes back into a BigDecimal via its String form, rather than risking
     * precision loss from a Double->BigDecimal cast.
     */
    public static BigDecimal toBigDecimal(Object value) {
        return switch (value) {
            case null -> BigDecimal.ZERO;
            case BigDecimal bd -> bd;
            case Number num -> BigDecimal.valueOf(num.doubleValue());
            default -> new BigDecimal(value.toString());
        };
    }

}
