package me.alphamode.mclong.math;

import me.alphamode.mclong.core.BigConstants;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BigInteger extends Number implements Comparable<BigInteger> {
    public static final BigInteger ZERO = new BigInteger(0);
    public static final BigInteger ONE = new BigInteger(1);
    private final java.math.BigInteger backing;
//    private final long longBacking;

//    public BigInteger(java.math.BigInteger backing, double longBacking) {
//        this.backing = backing;
//        this.longBacking = Mth.lfloor(longBacking);
//    }

//    public BigInteger(java.math.BigInteger backing, long longBacking) {
//        this(backing, longBacking, false);
//    }

//    public BigInteger(java.math.BigInteger backing, long longBacking, boolean constant) {
//        if (backing != null && !BigConstants.Ints.BIG_MODE && !constant)
//            throw new IllegalStateException("Trying to use Big Integer when Big Integer mode is off!");
//        this.backing = backing;
//        this.longBacking = longBacking;
//    }

    public BigInteger(String backing) {
        this.backing = new java.math.BigInteger(backing);
//        this.longBacking = Long.parseLong(backing);
    }

    public BigInteger(java.math.BigInteger backing) {
//        if (!BigConstants.Ints.BIG_MODE)
//            throw new IllegalStateException("Trying to use Big Integer when Big Integer mode is off!");
        this.backing = backing;
//        this.longBacking = backing.longValue();
    }

    public BigInteger(long backing) {
//        this.longBacking = backing;
        this.backing = java.math.BigInteger.valueOf(backing);
    }

    public BigInteger(double backing) {
//        this.longBacking = Mth.lfloor(backing);
        this.backing = java.math.BigInteger.valueOf(Mth.lfloor(backing));
    }

    public static BigInteger valueOf(long val) {
        return new BigInteger(java.math.BigInteger.valueOf(val));
    }

    public static BigInteger constant(long val) {
        return valueOf(val);//new BigInteger(java.math.BigInteger.valueOf(val), val, true);
    }


    public java.math.BigInteger getBacking() {
        return backing;
    }

    @Override
    public int compareTo(@NotNull BigInteger o) {
//        if (BigConstants.Ints.BIG_MODE)
            return this.backing.compareTo(o.getBacking());
//        return Long.compare(this.longBacking, o.longBacking);
    }

    public int compareTo(long o) {
        return compareTo(new BigInteger(o));
    }

    @Override
    public int intValue() {
//        if (BigConstants.Ints.BIG_MODE)
            return this.backing.intValue();
//        return (int) this.longBacking;
    }

    @Override
    public long longValue() {
//        if (compareTo(Long.MAX_VALUE) >= 0) {
//            System.out.println("WAA");
//        }
        return this.backing.longValue();
    }

    @Override
    public float floatValue() {
//        if (BigConstants.Ints.BIG_MODE)
            return this.backing.floatValue();
//        return this.longBacking;
    }

    @Override
    public double doubleValue() {
//        if (BigConstants.Ints.BIG_MODE)
            return this.backing.doubleValue();
//        return this.longBacking;
    }

    public BigInteger add(BigInteger val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.add(val.backing));
//        return new BigInteger(this.longBacking + val.longBacking);
    }

    public BigInteger add(long val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.add(java.math.BigInteger.valueOf(val)));
//        return new BigInteger(this.longBacking + val);
    }

    public BigInteger add() {
        return add(ONE);
    }

    public BigInteger and(BigInteger val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.and(val.backing));
//        return new BigInteger(this.longBacking & val.longBacking);
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
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.shiftRight(n));
//        return new BigInteger(this.longBacking >> n);
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
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.shiftLeft(n));
//        return new BigInteger(this.longBacking << n);
    }

    public BigInteger or(BigInteger n) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.or(n.backing));
//        return new BigInteger(this.longBacking | n.longBacking);
    }

    public BigInteger subtract(BigInteger val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.subtract(val.backing));
//        return new BigInteger(this.longBacking - val.longBacking);
    }

    public BigInteger subtract() {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.subtract(java.math.BigInteger.ONE));
//        return new BigInteger(this.longBacking - 1);
    }

    public BigInteger subtract(long val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.subtract(java.math.BigInteger.valueOf(val)));
//        return new BigInteger(this.longBacking - val);
    }

    public BigInteger abs() {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.abs());
//        return new BigInteger(Math.abs(this.longBacking));
    }

    public BigInteger multiply(BigInteger val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.multiply(val.backing));
//        return new BigInteger(this.longBacking * val.longBacking);
    }

    public BigInteger multiply(long val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.multiply(java.math.BigInteger.valueOf(val)));
//        return new BigInteger(this.longBacking * val);
    }

    public BigInteger min(BigInteger val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.min(val.backing));
//        return new BigInteger(Math.min(this.longBacking, val.longBacking));
    }

    public BigInteger max(BigInteger val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.max(val.backing));
//        return new BigInteger(Math.max(this.longBacking, val.longBacking));
    }

    public BigInteger max(long val) {
        return max(valueOf(val));
    }

    public BigInteger divide(BigInteger val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.divide(val.backing));
//        return new BigInteger(this.longBacking / val.longBacking);
    }

    public BigInteger mod(BigInteger val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.mod(val.backing));
//        return new BigInteger(this.longBacking % val.longBacking);
    }

    @Override
    public int hashCode() {
//        if (BigConstants.Ints.BIG_MODE)
            return this.backing.hashCode();
//        return Long.hashCode(this.longBacking);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BigInteger other) {
//            if (BigConstants.Ints.BIG_MODE)
                return this.backing.equals(other.backing);
//            return this.longBacking == other.longBacking;
        }
        return false;
    }

    @Override
    public String toString() {
//        if (BigConstants.Ints.BIG_MODE)
            return this.backing.toString();
//        return Long.toString(this.longBacking);
    }

    private BigDecimal cachedDecimal;

    public BigDecimal toBigDecimal() {
        if (this.cachedDecimal == null)
            this.cachedDecimal = new BigDecimal(this);
        return this.cachedDecimal;
    }

    public int signum() {
        return this.backing.signum();
    }

    @ApiStatus.Internal
    public java.math.BigInteger[] divideAndRemainder(BigInteger y) {
        return this.backing.divideAndRemainder(y.backing);
    }

    public BigInteger negate() {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.negate());
//        return new BigInteger(-this.longBacking);
    }

    public BigInteger remainder(BigInteger val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.remainder(val.backing));
//        return new BigInteger(this.longBacking % val.longBacking);
    }

    public BigInteger remainder(int val) {
//        if (BigConstants.Ints.BIG_MODE)
            return new BigInteger(this.backing.remainder(java.math.BigInteger.valueOf(val)));
//        return new BigInteger(this.longBacking % val);
    }
}