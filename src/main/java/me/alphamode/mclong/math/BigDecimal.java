package me.alphamode.mclong.math;

import me.alphamode.mclong.core.BigConstants;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.math.MathContext;
import java.math.RoundingMode;

public class BigDecimal extends Number implements Comparable<BigDecimal> {
    private static final MathContext PRECISION = MathContext.UNLIMITED;
    public static final BigDecimal MAX_VALUE = new BigDecimal(Double.MAX_VALUE);
    public static final BigDecimal ZERO = new BigDecimal(0);
    public static final BigDecimal REAL_ZERO = new BigDecimal(0E-9);
    public static final BigDecimal ONE = new BigDecimal(1);

    private final java.math.BigDecimal backing;
    private final double doubleBacking;

    public BigDecimal(java.math.BigDecimal val) {
        if (!BigConstants.BIG_MODE)
            throw new IllegalStateException("Using big decimal is not support when big mode is off!");
        this.backing = val;
        this.doubleBacking = this.backing.doubleValue();
    }

    private BigDecimal(double val) {
        this.backing = BigConstants.BIG_MODE ? new java.math.BigDecimal(val, PRECISION) : null;
        this.doubleBacking = val;
    }

    public BigDecimal(String val) {
        java.math.BigDecimal backing = null;
        if (BigConstants.BIG_MODE) {
            try {
                backing = new java.math.BigDecimal(val);
            } catch (NumberFormatException e) {
                backing = new java.math.BigDecimal(Double.parseDouble(val), PRECISION);
            }
        }
        this.backing = backing;
        this.doubleBacking = Double.parseDouble(val);
    }

    public BigDecimal(me.alphamode.mclong.math.BigInteger val) {
        this.backing = BigConstants.BIG_MODE ? BigConstants.Ints.BIG_MODE ? new java.math.BigDecimal(val.getBacking(), PRECISION) : new java.math.BigDecimal(val.longValue(), PRECISION) : null;
        this.doubleBacking = val.doubleValue();
    }

    public static BigDecimal valueOf(double val) {
        return new BigDecimal(val);
    }

    public BigDecimal negate() {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.negate(PRECISION));
        return new BigDecimal(-doubleBacking);
    }

    public BigDecimal add(BigDecimal val) {
        ensureValid();
        val.ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.add(val.backing, PRECISION));
        return new BigDecimal(this.doubleBacking + val.doubleBacking);
    }

    public BigDecimal add(double val) {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.add(new java.math.BigDecimal(val, PRECISION)));
        return new BigDecimal(this.doubleBacking + val);
    }

    public BigDecimal add() {
        return add(ONE);
    }

    public BigDecimal multiply(BigDecimal val) {
        ensureValid();
        val.ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.multiply(val.backing, PRECISION));
        return new BigDecimal(this.doubleBacking * val.doubleBacking);
    }

    public BigDecimal multiply(double val) {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.multiply(new java.math.BigDecimal(val, PRECISION)));
        return new BigDecimal(this.doubleBacking * val);
    }

    public BigDecimal sqrt() {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.sqrt(MathContext.DECIMAL128));
        return new BigDecimal(Math.sqrt(this.doubleBacking));
    }

    public BigDecimal subtract() {
        ensureValid();
        return subtract(ONE);
    }

    public BigDecimal subtract(BigDecimal val) {
        ensureValid();
        val.ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.subtract(val.backing, PRECISION));
        return new BigDecimal(this.doubleBacking - val.doubleBacking);
    }

    public BigDecimal subtract(double val) {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.subtract(new java.math.BigDecimal(val, PRECISION)));
        return new BigDecimal(this.doubleBacking - val);
    }

    @Override
    public int compareTo(@NotNull BigDecimal o) {
        ensureValid();
        o.ensureValid();
        if (BigConstants.BIG_MODE)
            return this.backing.compareTo(o.backing);
        return Double.compare(this.doubleBacking, o.doubleBacking);
    }

    public int compareTo(double o) {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return this.backing.compareTo(new java.math.BigDecimal(o, PRECISION));
        return Double.compare(this.doubleBacking, o);
    }

    @Override
    public int intValue() {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return this.backing.intValue();
        return Mth.floor(this.doubleBacking);
    }

    @Override
    public long longValue() {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return this.backing.longValue();
        return Mth.lfloor(this.doubleBacking);
    }

    @Override
    public float floatValue() {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return this.backing.floatValue();
        return (float) this.doubleBacking;
    }

    @Override
    public double doubleValue() {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return this.backing.doubleValue();
        return this.doubleBacking;
    }

    public me.alphamode.mclong.math.BigInteger toBigInteger() {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigInteger(this.backing.toBigInteger(), this.doubleBacking);
        return new BigInteger(this.doubleBacking);
    }

    public BigDecimal setScale(int i, RoundingMode roundingMode) {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.setScale(i, roundingMode));
        if (roundingMode == RoundingMode.CEILING)
            return new BigDecimal(Mth.lceil(this.doubleBacking));
        if (roundingMode == RoundingMode.FLOOR)
            return new BigDecimal(Math.floor(this.doubleBacking));
        return new BigDecimal(Math.round(this.doubleBacking));
    }

    public BigDecimal min(BigDecimal val) {
        ensureValid();
        val.ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.min(val.backing));
        return new BigDecimal(Math.min(this.doubleBacking, val.doubleBacking));
    }

    public BigDecimal max(BigDecimal val) {
        ensureValid();
        val.ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.max(val.backing));
        return new BigDecimal(Math.max(this.doubleBacking, val.doubleBacking));
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose scale is {@code this.scale()}.  If
     * rounding must be performed to generate a result with the given
     * scale, the specified rounding mode is applied.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  roundingMode rounding mode to apply.
     * @return {@code this / divisor}
     * @throws ArithmeticException if {@code divisor==0}, or
     *         {@code roundingMode==RoundingMode.UNNECESSARY} and
     *         {@code this.scale()} is insufficient to represent the result
     *         of the division exactly.
     * @since 1.5
     */
    public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
        ensureValid();
        divisor.ensureValid();
        if (equals(ZERO) || divisor.equals(ZERO))
            return ZERO;
        if (BigConstants.BIG_MODE) {
            try {
                return new BigDecimal(this.backing.divide(divisor.backing, roundingMode));
            } catch (ArithmeticException e) {
                System.out.println("WTF");
            }
        }
        return new BigDecimal(this.doubleBacking / divisor.doubleBacking);
    }

    public BigDecimal divide(double divisor, RoundingMode mode) {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.divide(new java.math.BigDecimal(divisor, PRECISION), mode));
        return new BigDecimal(this.doubleBacking / divisor);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose preferred scale is {@code (this.scale() -
     * divisor.scale())}; if the exact quotient cannot be
     * represented (because it has a non-terminating decimal
     * expansion) an {@code ArithmeticException} is thrown.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @throws ArithmeticException if the exact quotient does not have a
     *         terminating decimal expansion, including dividing by zero
     * @return {@code this / divisor}
     * @since 1.5
     * @author Joseph D. Darcy
     */
    public BigDecimal divide(BigDecimal divisor) {
        ensureValid();
        divisor.ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.divide(divisor.backing, RoundingMode.HALF_UP));
        return new BigDecimal(this.doubleBacking / divisor.doubleBacking);
    }

    public BigDecimal divide(double divisor) {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.divide(new java.math.BigDecimal(divisor, PRECISION), RoundingMode.HALF_UP));
        return new BigDecimal(this.doubleBacking / divisor);
    }

    public BigDecimal pow(int n) {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.pow(n, PRECISION));
        return new BigDecimal(Math.pow(this.doubleBacking, n));
    }

    public BigDecimal abs() {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.abs(PRECISION));
        return new BigDecimal(Math.abs(this.doubleBacking));
    }

    @Override
    public int hashCode() {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return this.backing.hashCode();
        return Double.hashCode(this.doubleBacking);
    }

    @Override
    public boolean equals(Object obj) {
        ensureValid();
        if (BigConstants.BIG_MODE) {
            if (obj instanceof BigDecimal other) {
                other.ensureValid();
                return this.backing.equals(other.backing);
            }
            return this.backing.equals(obj);
        }
        if (obj instanceof BigDecimal other)
            return this.doubleBacking == other.doubleBacking;
        return false;
    }

    @Override
    public String toString() {
        ensureValid();
        if (BigConstants.BIG_MODE)
            return this.backing.toString();
        return Double.toString(this.doubleBacking);
    }

    public java.math.BigDecimal getBacking() {
        return backing;
    }

    public BigInteger floor() {
        ensureValid();
        long val = Mth.lfloor(this.doubleBacking);
        return BigConstants.Ints.BIG_MODE && BigConstants.BIG_MODE ? new BigInteger(this.backing.setScale(0, RoundingMode.FLOOR).toBigInteger(), val) : new BigInteger(val);
    }

    public void ensureValid() {
//        if (BigConstants.BIG_MODE && backing == null)
//            this.backing = new java.math.BigDecimal(this.doubleBacking, PRECISION);
    }
}