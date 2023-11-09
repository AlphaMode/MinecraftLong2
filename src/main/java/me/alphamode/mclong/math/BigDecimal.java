package me.alphamode.mclong.math;

import me.alphamode.mclong.core.BigConstants;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.math.MathContext;
import java.math.RoundingMode;

public class BigDecimal extends Number implements Comparable<BigDecimal> {
    private static final MathContext PRECISION = MathContext.UNLIMITED;
    public static final BigDecimal MAX_VALUE = BigDecimal.valueOf(Double.MAX_VALUE);
    public static final BigDecimal ZERO = BigDecimal.valueOf(0);
    public static final BigDecimal REAL_ZERO = BigDecimal.valueOf(0E-9);
    public static final BigDecimal ONE = BigDecimal.valueOf(1);

    private final java.math.BigDecimal backing;

    public BigDecimal(java.math.BigDecimal val) {
        this.backing = val;
    }

    private BigDecimal(double val) {
        this.backing = new java.math.BigDecimal(val, PRECISION);
    }

    public BigDecimal(String val) {
        this.backing = new java.math.BigDecimal(val);
    }

    public BigDecimal(me.alphamode.mclong.math.BigInteger val) {
        this.backing = new java.math.BigDecimal(val.getBacking(), PRECISION);
    }

    public static BigDecimal valueOf(double val) {
        return new BigDecimal(val);
    }

    public BigDecimal negate() {
        return new BigDecimal(this.backing.negate(PRECISION));
    }

    public BigDecimal add(BigDecimal val) {
        return new BigDecimal(this.backing.add(val.backing, PRECISION));
    }

    public BigDecimal add(double val) {
        return new BigDecimal(this.backing.add(new java.math.BigDecimal(val, PRECISION)));
    }

    public BigDecimal add() {
        return add(ONE);
    }

    public BigDecimal multiply(BigDecimal val) {
        return new BigDecimal(this.backing.multiply(val.backing, PRECISION));
    }

    public BigDecimal multiply(double val) {
        return new BigDecimal(this.backing.multiply(new java.math.BigDecimal(val, PRECISION)));
    }

    public BigDecimal sqrt() {
        return new BigDecimal(this.backing.sqrt(MathContext.DECIMAL128));
    }

    public BigDecimal subtract() {
        return subtract(ONE);
    }

    public BigDecimal subtract(BigDecimal val) {
        return new BigDecimal(this.backing.subtract(val.backing, PRECISION));
    }

    public BigDecimal subtract(double val) {
        return new BigDecimal(this.backing.subtract(new java.math.BigDecimal(val, PRECISION)));
    }

    @Override
    public int compareTo(@NotNull BigDecimal o) {
        return this.backing.compareTo(o.backing);
    }

    public int compareTo(double o) {
        return this.backing.compareTo(new java.math.BigDecimal(o, PRECISION));
    }

    @Override
    public int intValue() {
        return this.backing.intValue();
    }

    @Override
    public long longValue() {
        return this.backing.longValue();
    }

    @Override
    public float floatValue() {
        return this.backing.floatValue();
    }

    @Override
    public double doubleValue() {
        return this.backing.doubleValue();
    }

    public me.alphamode.mclong.math.BigInteger toBigInteger() {
        return new BigInteger(this.backing.toBigInteger());
    }

    public BigDecimal setScale(int i, RoundingMode roundingMode) {
        return new BigDecimal(this.backing.setScale(i, roundingMode));
    }

    public BigDecimal min(BigDecimal val) {
        return new BigDecimal(this.backing.min(val.backing));
    }

    public BigDecimal max(BigDecimal val) {
        return new BigDecimal(this.backing.max(val.backing));
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose scale is {@code this.scale()}.  If
     * rounding must be performed to generate a result with the given
     * scale, the specified rounding mode is applied.
     *
     * @param divisor      value by which this {@code BigDecimal} is to be divided.
     * @param roundingMode rounding mode to apply.
     * @return {@code this / divisor}
     * @throws ArithmeticException if {@code divisor==0}, or
     *                             {@code roundingMode==RoundingMode.UNNECESSARY} and
     *                             {@code this.scale()} is insufficient to represent the result
     *                             of the division exactly.
     * @since 1.5
     */
    public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
        if (equals(ZERO) || divisor.equals(ZERO)) {
            return ZERO;
        }
        try {
            return new BigDecimal(this.backing.divide(divisor.backing, roundingMode));
        } catch (ArithmeticException e) {
            System.out.println("WTF");
        }
        throw new RuntimeException("Failed to divide " + this + " by " + divisor);
    }

    public BigDecimal divide(double divisor, RoundingMode mode) {
        return new BigDecimal(this.backing.divide(new java.math.BigDecimal(divisor, PRECISION), mode));
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose preferred scale is {@code (this.scale() -
     * divisor.scale())}; if the exact quotient cannot be
     * represented (because it has a non-terminating decimal
     * expansion) an {@code ArithmeticException} is thrown.
     *
     * @param divisor value by which this {@code BigDecimal} is to be divided.
     * @return {@code this / divisor}
     * @throws ArithmeticException if the exact quotient does not have a
     *                             terminating decimal expansion, including dividing by zero
     * @author Joseph D. Darcy
     * @since 1.5
     */
    public BigDecimal divide(BigDecimal divisor) {
        return new BigDecimal(this.backing.divide(divisor.backing, RoundingMode.HALF_UP));
    }

    public BigDecimal divide(double divisor) {
        return new BigDecimal(this.backing.divide(new java.math.BigDecimal(divisor, PRECISION), RoundingMode.HALF_UP));
    }

    public BigDecimal pow(int n) {
        return new BigDecimal(this.backing.pow(n, PRECISION));
    }

    public BigDecimal abs() {
        return new BigDecimal(this.backing.abs(PRECISION));
    }

    @Override
    public int hashCode() {
        return this.backing.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BigDecimal other) {
            return this.backing.equals(other.backing);
        }
        return this.backing.equals(obj);
    }

    @Override
    public String toString() {
        return this.backing.toString();
    }

    public java.math.BigDecimal getBacking() {
        return backing;
    }

    public BigInteger floor() {
        return new BigInteger(this.backing.toBigInteger());
    }
}