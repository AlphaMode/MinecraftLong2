package me.alphamode.mclong.math;

import me.alphamode.mclong.core.BigConstants;

public class BigMath {
    public static BigInteger floorDiv(final BigInteger x, final BigInteger y) {
        if (BigConstants.Ints.BIG_MODE) {
            if (x.signum() * y.signum() >= 0) {
                return x.divide(y);
            }
            final java.math.BigInteger[] qr = x.divideAndRemainder(y);
            return new BigInteger(qr[1].signum() == 0 ? qr[0] : qr[0].subtract(java.math.BigInteger.ONE));
        }
        return new BigInteger(Math.floorDiv(x.longValue(), y.longValue()));
    }

    public static BigInteger floorMod(BigInteger dividend, BigInteger divisor) {
        if (BigConstants.Ints.BIG_MODE) {
            BigInteger result = dividend.mod(divisor);
            if (result.signum() < 0) {
                result = result.add(divisor);
            }
            return result;
        }
        return new BigInteger(Math.floorMod(dividend.longValue(), divisor.longValue()));
    }

}