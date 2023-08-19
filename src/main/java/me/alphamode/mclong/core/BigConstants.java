package me.alphamode.mclong.core;

import com.mojang.logging.LogUtils;
import me.alphamode.mclong.math.BigDecimal;
import me.alphamode.mclong.math.BigInteger;
import org.slf4j.Logger;

public class BigConstants {
    public static final BigDecimal POINT_ONE = BigDecimal.valueOf(0.1D);
    public static final BigDecimal DISTANCE = BigDecimal.valueOf(256.0D);
    public static final BigDecimal NINE = BigDecimal.valueOf(9.0D);
    public static final BigDecimal BEE = BigDecimal.valueOf(0.3F);
    public static final BigDecimal BOAT = BigDecimal.valueOf(0.7D);
    public static final BigDecimal GUARDIAN = BigDecimal.valueOf(1.5D);
    public static final BigDecimal MINECART = BigDecimal.valueOf(0.8D);
    public static final BigDecimal EXPLODE_DISTANCE = BigDecimal.valueOf(4096.0D);
    public static final BigDecimal FOUR = BigDecimal.valueOf(4.0D);
    public static final BigDecimal THREE = BigDecimal.valueOf(3.0D);
    public static final BigDecimal TEN = BigDecimal.valueOf(10.0D);
    public static final BigDecimal SIX = BigDecimal.valueOf(6.0D);
    public static final BigDecimal FIFTEEN = BigDecimal.valueOf(15.0D);
    public static boolean BIG_MODE = true;

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final BigDecimal POSITIVE_INFINITY = BigDecimal.valueOf(Double.MAX_VALUE);
    public static final BigDecimal NEGATIVE_INFINITY = BigDecimal.valueOf(Double.MAX_VALUE);
    public static final BigDecimal ALMOST_ONE = BigDecimal.valueOf(0.9999999D);
    public static final BigDecimal FIND_BITS = BigDecimal.valueOf(1.0000001D);
    public static final BigDecimal PARTICLE_TICK = BigDecimal.valueOf(0.2D);
    public static final BigDecimal PARTICLE = BigDecimal.valueOf(0.125D);
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
    public static final BigDecimal EPSILON_EQUAL = BigDecimal.valueOf(1.0E-5F);

    public static class Ints {
        public static final BigInteger TWO = BigInteger.valueOf(2);
        public static final BigInteger EIGHT = BigInteger.valueOf(8);
        public static final BigInteger THREE = BigInteger.valueOf(3);
        public static final BigInteger TWELVE = BigInteger.valueOf(12);
        public static final BigInteger TEN = BigInteger.valueOf(10);
        public static boolean BIG_MODE = true;
        public static final BigInteger FIFTEEN = BigInteger.valueOf(15);
        public static final BigInteger SIXTEEN = BigInteger.valueOf(15);
    }
}