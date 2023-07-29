package me.alphamode.mclong.core;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BigConstants {
    public static final BigDecimal POSITIVE_INFINITY = BigDecimal.valueOf(Double.MAX_VALUE);
    public static final BigDecimal NEGATIVE_INFINITY = BigDecimal.valueOf(Double.MAX_VALUE);
    public static final BigDecimal ALMOST_ONE = BigDecimal.valueOf(0.9999999D);
    public static final BigDecimal FIND_BITS = BigDecimal.valueOf(1.0000001D);
    public static final BigDecimal PARTICLES = BigDecimal.valueOf(0.25D);
    public static final BigDecimal EPSILON = BigDecimal.valueOf(1.0E-7D);
    public static final BigDecimal ENTITY = BigDecimal.valueOf(1.0E-6D);
    public static final BigDecimal AABB = BigDecimal.valueOf(0.5D);
    public static final BigDecimal TWO = BigDecimal.valueOf(2.0D);
    public static final BigDecimal FIVE = BigDecimal.valueOf(5.0D);
    public static final BigDecimal EIGHT = BigDecimal.valueOf(8.0D);
    public static final BigDecimal VAL = BigDecimal.valueOf(0.33F);
    public static final BigDecimal TWELVE = BigDecimal.valueOf(12.0D);
    public static final BigDecimal SIXTEEN = BigDecimal.valueOf(16.0D);
    public static final BigDecimal BREAK_PROGRESS = BigDecimal.valueOf(1024.0D);
    public static final BigDecimal BIG_VAL = BigDecimal.valueOf(2048.0D);

    public static class Ints {
        public static final BigInteger FIFTEEN = BigInteger.valueOf(15);
        public static final BigInteger SIXTEEN = BigInteger.valueOf(15);
    }
}