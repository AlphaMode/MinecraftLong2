package me.alphamode.mclong.math;

import me.alphamode.mclong.core.BigConstants;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class BigInteger extends Number implements Comparable<BigInteger> {
    public static final BigInteger ZERO = new BigInteger(java.math.BigInteger.ZERO, 0);
    public static final BigInteger ONE = new BigInteger(java.math.BigInteger.ONE, 1);
    private final java.math.BigInteger backing;
    private final long longBacking;

    public BigInteger(java.math.BigInteger backing, double longBacking) {
        this.backing = backing;
        this.longBacking = Mth.lfloor(longBacking);
    }

    public BigInteger(java.math.BigInteger backing, long longBacking) {
        this.backing = backing;
        this.longBacking = longBacking;
    }

    public BigInteger(String backing) {
        this.backing = new java.math.BigInteger(backing);
        this.longBacking = this.backing.longValue();
    }

    public BigInteger(java.math.BigInteger backing) {
        this.backing = backing;
        this.longBacking = backing.longValue();
    }

    public BigInteger(double backing) {
        this.longBacking = Mth.lfloor(backing);
        this.backing = java.math.BigInteger.valueOf(this.longBacking);
    }

    public static BigInteger valueOf(long val) {
        return new BigInteger(java.math.BigInteger.valueOf(val), val);
    }


    public java.math.BigInteger getBacking() {
        return backing;
    }

    @Override
    public int compareTo(@NotNull BigInteger o) {
        if (BigConstants.Ints.BIG_MODE)
            return this.backing.compareTo(o.getBacking());
        return Long.compare(this.longBacking, o.longBacking);
    }

    @Override
    public int intValue() {
        if (BigConstants.Ints.BIG_MODE)
            return this.backing.intValue();
        return (int) this.longBacking;
    }

    @Override
    public long longValue() {
        if (BigConstants.Ints.BIG_MODE)
            return this.backing.longValue();
        return this.longBacking;
    }

    @Override
    public float floatValue() {
        if (BigConstants.Ints.BIG_MODE)
            return this.backing.floatValue();
        return this.longBacking;
    }

    @Override
    public double doubleValue() {
        if (BigConstants.Ints.BIG_MODE)
            return this.backing.doubleValue();
        return this.longBacking;
    }

    public BigInteger add(BigInteger val) {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.add(val.backing));
        return new BigInteger(this.longBacking + val.longBacking);
    }

    public BigInteger add(long val) {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.add(java.math.BigInteger.valueOf(val)));
        return new BigInteger(this.longBacking + val);
    }

    public BigInteger and(BigInteger val) {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.and(val.backing));
        return new BigInteger(this.longBacking & val.longBacking);
    }

    /**
     * Returns a BigInteger whose value is {@code (this >> n)}.  Sign
     * extension is performed.  The shift distance, {@code n}, may be
     * negative, in which case this method performs a left shift.
     * (Computes <code>floor(this / 2<sup>n</sup>)</code>.)
     *
     * @param  n shift distance, in bits.
     * @return {@code this >> n}
     * @see #shiftLeft
     */
    public BigInteger shiftRight(int n) {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.shiftRight(n));
        return new BigInteger(this.longBacking >> n);
    }

    /**
     * Returns a BigInteger whose value is {@code (this << n)}.
     * The shift distance, {@code n}, may be negative, in which case
     * this method performs a right shift.
     * (Computes <code>floor(this * 2<sup>n</sup>)</code>.)
     *
     * @param  n shift distance, in bits.
     * @return {@code this << n}
     * @see #shiftRight
     */
    public BigInteger shiftLeft(int n) {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.shiftLeft(n));
        return new BigInteger(this.longBacking << n);
    }

    public BigInteger subtract(BigInteger val) {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.subtract(val.backing));
        return new BigInteger(this.longBacking - val.longBacking);
    }

    public BigInteger subtract(long val) {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.subtract(java.math.BigInteger.valueOf(val)));
        return new BigInteger(this.longBacking - val);
    }

    public BigInteger abs() {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.abs());
        return new BigInteger(Math.abs(this.longBacking));
    }

    public BigInteger multiply(BigInteger val) {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.multiply(val.backing));
        return new BigInteger(this.longBacking * val.longBacking);
    }

    public BigInteger min(BigInteger val) {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.min(val.backing));
        return new BigInteger(Math.min(this.longBacking, val.longBacking));
    }

    public BigInteger max(BigInteger val) {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.max(val.backing));
        return new BigInteger(Math.max(this.longBacking, val.longBacking));
    }

    public BigInteger divide(BigInteger val) {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.divide(val.backing));
        return new BigInteger(this.longBacking / val.longBacking);
    }

    public BigInteger mod(BigInteger val) {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.mod(val.backing));
        return new BigInteger(this.longBacking % val.longBacking);
    }

    @Override
    public int hashCode() {
        if (BigConstants.Ints.BIG_MODE)
            return this.backing.hashCode();
        return Long.hashCode(this.longBacking);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BigInteger other) {
            if (BigConstants.Ints.BIG_MODE)
                return this.backing.equals(other.backing);
            return this.longBacking == other.longBacking;
        }
        return false;
    }

    @Override
    public String toString() {
        if (BigConstants.Ints.BIG_MODE)
            return this.backing.toString();
        return Long.toString(this.longBacking);
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(this);
    }

    public int signum() {
        return this.backing.signum();
    }

    @ApiStatus.Internal
    public java.math.BigInteger[] divideAndRemainder(BigInteger y) {
        return this.backing.divideAndRemainder(y.backing);
    }

    public BigInteger negate() {
        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.negate());
        return new BigInteger(-this.longBacking);
    }
}