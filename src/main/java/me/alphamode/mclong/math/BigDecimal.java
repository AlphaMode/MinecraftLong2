package me.alphamode.mclong.math;

import me.alphamode.mclong.core.BigConstants;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.math.MathContext;
import java.math.RoundingMode;

public class BigDecimal extends Number implements Comparable<BigDecimal> {
    public static final BigDecimal ZERO = new BigDecimal(java.math.BigDecimal.ZERO);
    public static final BigDecimal REAL_ZERO = BigDecimal.valueOf(0E-9);
    public static final BigDecimal ONE = new BigDecimal(java.math.BigDecimal.ONE);

    private final java.math.BigDecimal backing;
    private final double doubleBacking;

    public BigDecimal(java.math.BigDecimal val) {
        this.backing = val;
        this.doubleBacking = this.backing.doubleValue();
    }

    private BigDecimal(double val) {
        this.backing = java.math.BigDecimal.valueOf(val);
        this.doubleBacking = val;
    }

    public BigDecimal(String val) {
        this.backing = new java.math.BigDecimal(val);
        this.doubleBacking = this.backing.doubleValue();
    }

    public BigDecimal(me.alphamode.mclong.math.BigInteger val) {
        this.backing = new java.math.BigDecimal(val.getBacking());
        this.doubleBacking = this.backing.doubleValue();
    }

    public static BigDecimal valueOf(double val) {
        return new BigDecimal(java.math.BigDecimal.valueOf(val));
    }

    public BigDecimal negate() {
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.negate());
        return new BigDecimal(-doubleBacking);
    }

    public BigDecimal add(BigDecimal val) {
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.add(val.backing));
        return new BigDecimal(this.doubleBacking + val.doubleBacking);
    }

    public BigDecimal add(double val) {
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.add(java.math.BigDecimal.valueOf(val)));
        return new BigDecimal(this.doubleBacking + val);
    }

    public BigDecimal multiply(BigDecimal val) {
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.multiply(val.backing));
        return new BigDecimal(this.doubleBacking * val.doubleBacking);
    }

    public BigDecimal multiply(double val) {
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.multiply(java.math.BigDecimal.valueOf(val)));
        return new BigDecimal(this.doubleBacking * val);
    }

    public BigDecimal sqrt() {
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.sqrt(MathContext.UNLIMITED));
        return new BigDecimal(Math.sqrt(this.doubleBacking));
    }

    public BigDecimal subtract(BigDecimal val) {
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.subtract(val.backing));
        return new BigDecimal(this.doubleBacking - val.doubleBacking);
    }

    public BigDecimal subtract(double val) {
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.subtract(java.math.BigDecimal.valueOf(val)));
        return new BigDecimal(this.doubleBacking - val);
    }

    @Override
    public int compareTo(@NotNull BigDecimal o) {
        if (BigConstants.BIG_MODE)
            return this.backing.compareTo(o.backing);
        return Double.compare(this.doubleBacking, o.doubleBacking);
    }

    public int compareTo(double o) {
        if (BigConstants.BIG_MODE)
            return this.backing.compareTo(java.math.BigDecimal.valueOf(o));
        return Double.compare(this.doubleBacking, o);
    }

    @Override
    public int intValue() {
        if (BigConstants.BIG_MODE)
            return this.backing.intValue();
        return Mth.floor(this.doubleBacking);
    }

    @Override
    public long longValue() {
        if (BigConstants.BIG_MODE)
            return this.backing.longValue();
        return Mth.lfloor(this.doubleBacking);
    }

    @Override
    public float floatValue() {
        if (BigConstants.BIG_MODE)
            return this.backing.floatValue();
        return (float) this.doubleBacking;
    }

    @Override
    public double doubleValue() {
        if (BigConstants.BIG_MODE)
            return this.backing.doubleValue();
        return this.doubleBacking;
    }

    public me.alphamode.mclong.math.BigInteger toBigInteger() {
        if (BigConstants.BIG_MODE)
            return new BigInteger(this.backing.toBigInteger(), this.doubleBacking);
        return new BigInteger(this.doubleBacking);
    }

    public BigDecimal setScale(int i, RoundingMode roundingMode) {
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.setScale(i, roundingMode));
        if (roundingMode == RoundingMode.CEILING)
            return new BigDecimal(Mth.lceil(this.doubleBacking));
        if (roundingMode == RoundingMode.FLOOR)
            return new BigDecimal(Math.floor(this.doubleBacking));
        return new BigDecimal(Math.round(this.doubleBacking));
    }

    public BigDecimal min(BigDecimal val) {
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.min(val.backing));
        return new BigDecimal(Math.min(this.doubleBacking, val.doubleBacking));
    }

    public BigDecimal max(BigDecimal val) {
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
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.divide(java.math.BigDecimal.valueOf(divisor), mode));
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
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.divide(divisor.backing));
        return new BigDecimal(this.doubleBacking / divisor.doubleBacking);
    }

    public BigDecimal divide(double divisor) {
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.divide(java.math.BigDecimal.valueOf(divisor)));
        return new BigDecimal(this.doubleBacking / divisor);
    }

    public BigDecimal abs() {
        if (BigConstants.BIG_MODE)
            return new BigDecimal(this.backing.abs());
        return new BigDecimal(Math.abs(this.doubleBacking));
    }

    @Override
    public int hashCode() {
        if (BigConstants.BIG_MODE)
            return this.backing.hashCode();
        return Double.hashCode(this.doubleBacking);
    }

    @Override
    public boolean equals(Object obj) {
        if (BigConstants.BIG_MODE) {
            if (obj instanceof BigDecimal other)
                return this.backing.equals(other.backing);
            return this.backing.equals(obj);
        }
        if (obj instanceof BigDecimal other)
            return this.doubleBacking == other.doubleBacking;
        return false;
    }

    @Override
    public String toString() {
        if (BigConstants.BIG_MODE)
            return this.backing.toString();
        return Double.toString(this.doubleBacking);
    }

    public java.math.BigDecimal getBacking() {
        return backing;
    }
}