package me.alphamode.mclong.math;

import org.joml.FrustumIntersection;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

public class BigFrustumIntersection {
    private float nxX;
    private float nxY;
    private float nxZ;
    private float nxW;
    private float pxX;
    private float pxY;
    private float pxZ;
    private float pxW;
    private float nyX;
    private float nyY;
    private float nyZ;
    private float nyW;
    private float pyX;
    private float pyY;
    private float pyZ;
    private float pyW;
    private float nzX;
    private float nzY;
    private float nzZ;
    private float nzW;
    private float pzX;
    private float pzY;
    private float pzZ;
    private float pzW;
    private final Vector4f[] planes = new Vector4f[6];

    public BigFrustumIntersection() {
        for(int i = 0; i < 6; ++i) {
            this.planes[i] = new Vector4f();
        }

    }

    public BigFrustumIntersection set(Matrix4fc m) {
        return this.set(m, true);
    }

    public BigFrustumIntersection set(Matrix4fc m, boolean allowTestSpheres) {
        this.nxX = m.m03() + m.m00();
        this.nxY = m.m13() + m.m10();
        this.nxZ = m.m23() + m.m20();
        this.nxW = m.m33() + m.m30();
        float invl;
        if (allowTestSpheres) {
            invl = Math.invsqrt(this.nxX * this.nxX + this.nxY * this.nxY + this.nxZ * this.nxZ);
            this.nxX *= invl;
            this.nxY *= invl;
            this.nxZ *= invl;
            this.nxW *= invl;
        }

        this.planes[0].set(this.nxX, this.nxY, this.nxZ, this.nxW);
        this.pxX = m.m03() - m.m00();
        this.pxY = m.m13() - m.m10();
        this.pxZ = m.m23() - m.m20();
        this.pxW = m.m33() - m.m30();
        if (allowTestSpheres) {
            invl = Math.invsqrt(this.pxX * this.pxX + this.pxY * this.pxY + this.pxZ * this.pxZ);
            this.pxX *= invl;
            this.pxY *= invl;
            this.pxZ *= invl;
            this.pxW *= invl;
        }

        this.planes[1].set(this.pxX, this.pxY, this.pxZ, this.pxW);
        this.nyX = m.m03() + m.m01();
        this.nyY = m.m13() + m.m11();
        this.nyZ = m.m23() + m.m21();
        this.nyW = m.m33() + m.m31();
        if (allowTestSpheres) {
            invl = Math.invsqrt(this.nyX * this.nyX + this.nyY * this.nyY + this.nyZ * this.nyZ);
            this.nyX *= invl;
            this.nyY *= invl;
            this.nyZ *= invl;
            this.nyW *= invl;
        }

        this.planes[2].set(this.nyX, this.nyY, this.nyZ, this.nyW);
        this.pyX = m.m03() - m.m01();
        this.pyY = m.m13() - m.m11();
        this.pyZ = m.m23() - m.m21();
        this.pyW = m.m33() - m.m31();
        if (allowTestSpheres) {
            invl = Math.invsqrt(this.pyX * this.pyX + this.pyY * this.pyY + this.pyZ * this.pyZ);
            this.pyX *= invl;
            this.pyY *= invl;
            this.pyZ *= invl;
            this.pyW *= invl;
        }

        this.planes[3].set(this.pyX, this.pyY, this.pyZ, this.pyW);
        this.nzX = m.m03() + m.m02();
        this.nzY = m.m13() + m.m12();
        this.nzZ = m.m23() + m.m22();
        this.nzW = m.m33() + m.m32();
        if (allowTestSpheres) {
            invl = Math.invsqrt(this.nzX * this.nzX + this.nzY * this.nzY + this.nzZ * this.nzZ);
            this.nzX *= invl;
            this.nzY *= invl;
            this.nzZ *= invl;
            this.nzW *= invl;
        }

        this.planes[4].set(this.nzX, this.nzY, this.nzZ, this.nzW);
        this.pzX = m.m03() - m.m02();
        this.pzY = m.m13() - m.m12();
        this.pzZ = m.m23() - m.m22();
        this.pzW = m.m33() - m.m32();
        if (allowTestSpheres) {
            invl = Math.invsqrt(this.pzX * this.pzX + this.pzY * this.pzY + this.pzZ * this.pzZ);
            this.pzX *= invl;
            this.pzY *= invl;
            this.pzZ *= invl;
            this.pzW *= invl;
        }

        this.planes[5].set(this.pzX, this.pzY, this.pzZ, this.pzW);
        return this;
    }

    public int intersectAab(BigDecimal minX, BigDecimal minY, BigDecimal minZ, BigDecimal maxX, BigDecimal maxY, BigDecimal maxZ) {
        int plane = 0;
        boolean inside = true;
        var nxXB = BigDecimal.valueOf(this.nxX);
        if (nxXB.multiply((this.nxX < 0.0F ? minX : maxX)).add(BigDecimal.valueOf(this.nxY).multiply((this.nxY < 0.0F ? minY : maxY))).add(BigDecimal.valueOf(this.nxZ).multiply((this.nxZ < 0.0F ? minZ : maxZ))).compareTo(BigDecimal.valueOf(-this.nxW)) >= 0) {
            plane = 1;
            inside &= nxXB.multiply((this.nxX < 0.0F ? maxX : minX)).add(BigDecimal.valueOf(this.nxY).multiply((this.nxY < 0.0F ? maxY : minY))).add(BigDecimal.valueOf(this.nxZ).multiply((this.nxZ < 0.0F ? maxZ : minZ))).compareTo(BigDecimal.valueOf(-this.nxW)) >= 0;
            if (BigDecimal.valueOf(this.pxX).multiply((this.pxX < 0.0F ? minX : maxX)).add(BigDecimal.valueOf(this.pxY).multiply((this.pxY < 0.0F ? minY : maxY))).add(BigDecimal.valueOf(this.pxZ).multiply((this.pxZ < 0.0F ? minZ : maxZ))).compareTo(BigDecimal.valueOf(-this.pxW)) >= 0) {
                plane = 2;
                inside &= BigDecimal.valueOf(this.pxX).multiply((this.pxX < 0.0F ? maxX : minX)).add(BigDecimal.valueOf(this.pxY).multiply((this.pxY < 0.0F ? maxY : minY))).add(BigDecimal.valueOf(this.pxZ).multiply((this.pxZ < 0.0F ? maxZ : minZ))).compareTo(BigDecimal.valueOf(-this.pxW)) >= 0;
                if (BigDecimal.valueOf(this.nyX).multiply((this.nyX < 0.0F ? minX : maxX)).add(BigDecimal.valueOf(this.nyY).multiply((this.nyY < 0.0F ? minY : maxY))).add(BigDecimal.valueOf(this.nyZ).multiply((this.nyZ < 0.0F ? minZ : maxZ))).compareTo(BigDecimal.valueOf(-this.nyW)) >= 0) {
                    plane = 3;
                    inside &= BigDecimal.valueOf(this.nyX).multiply((this.nyX < 0.0F ? maxX : minX)).add(BigDecimal.valueOf(this.nyY).multiply((this.nyY < 0.0F ? maxY : minY))).add(BigDecimal.valueOf(this.nyZ).multiply((this.nyZ < 0.0F ? maxZ : minZ))).compareTo(BigDecimal.valueOf(-this.nyW)) >= 0;
                    if (BigDecimal.valueOf(this.pyX).multiply((this.pyX < 0.0F ? minX : maxX)).add(BigDecimal.valueOf(this.pyY).multiply((this.pyY < 0.0F ? minY : maxY))).add(BigDecimal.valueOf(this.pyZ).add((this.pyZ < 0.0F ? minZ : maxZ))).compareTo(BigDecimal.valueOf(-this.pyW)) >= 0) {
                        plane = 4;
                        inside &= BigDecimal.valueOf(this.pyX).multiply((this.pyX < 0.0F ? maxX : minX)).add(BigDecimal.valueOf(this.pyY).multiply((this.pyY < 0.0F ? maxY : minY))).add(BigDecimal.valueOf(this.pyZ).multiply((this.pyZ < 0.0F ? maxZ : minZ))).compareTo(BigDecimal.valueOf(-this.pyW)) >= 0;
                        if (BigDecimal.valueOf(this.nzX).multiply((this.nzX < 0.0F ? minX : maxX)).add(BigDecimal.valueOf(this.nzY).multiply((this.nzY < 0.0F ? minY : maxY))).add(BigDecimal.valueOf(this.nzZ).multiply((this.nzZ < 0.0F ? minZ : maxZ))).compareTo(BigDecimal.valueOf(-this.nzW)) >= 0) {
                            plane = 5;
                            inside &= BigDecimal.valueOf(this.nzX).multiply((this.nzX < 0.0F ? maxX : minX)).add(BigDecimal.valueOf(this.nzY).multiply((this.nzY < 0.0F ? maxY : minY))).add(BigDecimal.valueOf(this.nzZ).multiply((this.nzZ < 0.0F ? maxZ : minZ))).compareTo(BigDecimal.valueOf(-this.nzW)) >= 0;
                            if (BigDecimal.valueOf(this.pzX).multiply((this.pzX < 0.0F ? minX : maxX)).add(BigDecimal.valueOf(this.pzY).multiply((this.pzY < 0.0F ? minY : maxY))).add(BigDecimal.valueOf(this.pzZ).multiply((this.pzZ < 0.0F ? minZ : maxZ))).compareTo(BigDecimal.valueOf(-this.pzW)) >= 0) {
                                inside &= BigDecimal.valueOf(this.pzX).multiply((this.pzX < 0.0F ? maxX : minX)).add(BigDecimal.valueOf(this.pzY).multiply((this.pzY < 0.0F ? maxY : minY))).add(BigDecimal.valueOf(this.pzZ).multiply((this.pzZ < 0.0F ? maxZ : minZ))).compareTo(BigDecimal.valueOf(-this.pzW)) >= 0;
                                return inside ? -2 : -1;
                            }
                        }
                    }
                }
            }
        }

        return plane;
    }

    public boolean testAab(BigDecimal minX, BigDecimal minY, BigDecimal minZ, BigDecimal maxX, BigDecimal maxY, BigDecimal maxZ) {
        return BigDecimal.valueOf(this.nxX).multiply((this.nxX < 0.0F ? minX : maxX)).add(BigDecimal.valueOf(this.nxY).multiply((this.nxY < 0.0F ? minY : maxY))).add(BigDecimal.valueOf(this.nxZ).multiply((this.nxZ < 0.0F ? minZ : maxZ))).compareTo(BigDecimal.valueOf(-this.nxW)) >= 0 && BigDecimal.valueOf(this.pxX).multiply((this.pxX < 0.0F ? minX : maxX)).add(BigDecimal.valueOf(this.pxY).multiply((this.pxY < 0.0F ? minY : maxY))).add(BigDecimal.valueOf(this.pxZ).multiply((this.pxZ < 0.0F ? minZ : maxZ))).compareTo(BigDecimal.valueOf(-this.pxW)) >= 0 && BigDecimal.valueOf(this.nyX).multiply((this.nyX < 0.0F ? minX : maxX)).add(BigDecimal.valueOf(this.nyY).multiply((this.nyY < 0.0F ? minY : maxY))).add(BigDecimal.valueOf(this.nyZ).multiply((this.nyZ < 0.0F ? minZ : maxZ))).compareTo(BigDecimal.valueOf(-this.nyW)) >= 0 && BigDecimal.valueOf(this.pyX).multiply((this.pyX < 0.0F ? minX : maxX)).add(BigDecimal.valueOf(this.pyY).multiply((this.pyY < 0.0F ? minY : maxY))).add(BigDecimal.valueOf(this.pyZ).multiply((this.pyZ < 0.0F ? minZ : maxZ))).compareTo(BigDecimal.valueOf(-this.pyW)) >= 0 && BigDecimal.valueOf(this.nzX).multiply((this.nzX < 0.0F ? minX : maxX)).add(BigDecimal.valueOf(this.nzY).multiply((this.nzY < 0.0F ? minY : maxY))).add(BigDecimal.valueOf(this.nzZ).multiply((this.nzZ < 0.0F ? minZ : maxZ))).compareTo(BigDecimal.valueOf(-this.nzW)) >= 0 && BigDecimal.valueOf(this.pzX).multiply((this.pzX < 0.0F ? minX : maxX)).add(BigDecimal.valueOf(this.pzY).multiply((this.pzY < 0.0F ? minY : maxY))).add(BigDecimal.valueOf(this.pzZ).multiply((this.pzZ < 0.0F ? minZ : maxZ))).compareTo(BigDecimal.valueOf(-this.pzW)) >= 0;
    }
}
