package me.alphamode.mclong.core;

import me.alphamode.mclong.math.BigDecimal;

public class MutableVec3 {
    public BigDecimal x, y, z;

    public MutableVec3(BigDecimal x, BigDecimal y, BigDecimal z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MutableVec3(double x, double y, double z) {
        this.x = BigDecimal.valueOf(x);
        this.y = BigDecimal.valueOf(y);
        this.z = BigDecimal.valueOf(z);
    }
}
