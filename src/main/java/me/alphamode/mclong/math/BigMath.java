package me.alphamode.mclong.math;

public class BigMath {
    public static BigInteger floorDiv(final BigInteger x, final BigInteger y) {
        if (x.signum() * y.signum() >= 0) {
            return x.divide(y);
        }
        final java.math.BigInteger[] qr = x.divideAndRemainder(y);
        return new BigInteger(qr[1].signum() == 0 ? qr[0] : qr[0].subtract(java.math.BigInteger.ONE));
    }

    public static BigInteger floorDiv(final BigInteger x, long yLong) {
        var y = BigInteger.valueOf(yLong);
        if (x.signum() * y.signum() >= 0) {
            return x.divide(y);
        }
        final java.math.BigInteger[] qr = x.divideAndRemainder(y);
        return new BigInteger(qr[1].signum() == 0 ? qr[0] : qr[0].subtract(java.math.BigInteger.ONE));
    }

    public static BigInteger floorMod(BigInteger dividend, BigInteger divisor) {
        BigInteger result = dividend.mod(divisor);
        if (result.signum() < 0) {
            result = result.add(divisor);
        }
        return result;
    }

    public static BigInteger floorMod(BigInteger dividend, long longDivisor) {
        var divisor = BigInteger.valueOf(longDivisor);
        BigInteger result = dividend.mod(divisor);
        if (result.signum() < 0) {
            result = result.add(divisor);
        }
        return result;
    }
}