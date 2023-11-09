package me.alphamode.mclong.core;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

import javax.annotation.concurrent.Immutable;
import me.alphamode.mclong.math.BigDecimal;
import me.alphamode.mclong.math.BigInteger;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Immutable
public class Vec3l implements Comparable<Vec3l> {
    public static final Codec<Vec3l> CODEC = ModdedCodec.STRING_STREAM.comapFlatMap((p_123318_) -> {
        return Util.fixedSize(p_123318_, 3).map((p_175586_) -> {
            return new Vec3l(new BigInteger(p_175586_[0]), new BigInteger(p_175586_[1]), new BigInteger(p_175586_[2]));
        });
    }, (p_123313_) -> {
        return Stream.of(p_123313_.getBigX().toString(), p_123313_.getBigY().toString(), p_123313_.getBigZ().toString());
    });
    public static final Vec3l ZERO = new Vec3l(0, 0, 0);
    private BigInteger x;
    private BigInteger y;
    private BigInteger z;

    private static Function<Vec3l, DataResult<Vec3l>> checkOffsetAxes(int p_194646_) {
        return (p_194649_) -> {
            return p_194649_.getBigX().abs().compareTo(p_194646_) < 0 && p_194649_.getBigY().abs().compareTo(p_194646_) < 0 && p_194649_.getBigZ().abs().compareTo(p_194646_) < 0 ? DataResult.success(p_194649_) : DataResult.error(() -> "Position out of range, expected at most " + p_194646_ + ": " + p_194649_);
        };
    }

    public static Codec<Vec3l> offsetCodec(int p_194651_) {
        return CODEC.flatXmap(checkOffsetAxes(p_194651_), checkOffsetAxes(p_194651_));
    }

    public Vec3l(Vec3i vec3i) {
        this(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public Vec3l(BigInteger p_123296_, BigInteger p_123297_, BigInteger p_123298_) {
        this.x = p_123296_;
        this.y = p_123297_;
        this.z = p_123298_;
    }

    public Vec3l(BigDecimal p_123292_, BigDecimal p_123293_, BigDecimal p_123294_) {
        this(p_123292_.toBigInteger(), p_123293_.toBigInteger(), p_123294_.toBigInteger());
    }

    public Vec3l(double p_123292_, double p_123293_, double p_123294_) {
        this(BigInteger.valueOf(Mth.lfloor(p_123292_)), BigInteger.valueOf(Mth.lfloor(p_123293_)), BigInteger.valueOf(Mth.lfloor(p_123294_)));
    }

    public boolean equals(Object p_123327_) {
        if (this == p_123327_) {
            return true;
        } else if (!(p_123327_ instanceof Vec3l)) {
            return false;
        } else {
            Vec3l vec3l = (Vec3l) p_123327_;
            if (!this.getBigX().equals(vec3l.getBigX())) {
                return false;
            } else if (!this.getBigY().equals(vec3l.getBigY())) {
                return false;
            } else {
                return this.getBigZ().equals(vec3l.getBigZ());
            }
        }
    }

    public int hashCode() {
        return (int) ((this.getBigY().add(this.getBigZ()).multiply(31)).multiply(31).add(this.getBigX())).intValue();
    }

    public int compareTo(Vec3l p_123330_) {
        if (this.getBigY().equals(p_123330_.getBigY())) {
            return (int) (this.getBigZ().equals(p_123330_.getBigZ()) ? this.getBigX().subtract(p_123330_.getBigX()) : this.getBigZ().subtract(p_123330_.getBigZ())).intValue();
        } else {
            return (int) (this.getBigY().subtract(p_123330_.getBigY())).intValue();
        }
    }

    public long getX() {
        return this.x.longValue();
    }

    public long getY() {
        return this.y.longValue();
    }

    public long getZ() {
        return this.z.longValue();
    }

    public me.alphamode.mclong.math.BigInteger getBigX() {
        return this.x;
    }

    public me.alphamode.mclong.math.BigInteger getBigY() {
        return this.y;
    }

    public me.alphamode.mclong.math.BigInteger getBigZ() {
        return this.z;
    }

    public me.alphamode.mclong.math.BigDecimal getBigDecX() {
        return new me.alphamode.mclong.math.BigDecimal(this.x);
    }

    public me.alphamode.mclong.math.BigDecimal getBigDecY() {
        return new me.alphamode.mclong.math.BigDecimal(this.y);
    }

    public me.alphamode.mclong.math.BigDecimal getBigDecZ() {
        return new me.alphamode.mclong.math.BigDecimal(this.z);
    }

    protected Vec3l setX(long p_175605_) {
        this.x = BigInteger.valueOf(p_175605_);
        return this;
    }

    protected Vec3l setY(long p_175604_) {
        this.y = BigInteger.valueOf(p_175604_);
        return this;
    }

    protected Vec3l setZ(long p_175603_) {
        this.z = BigInteger.valueOf(p_175603_);
        return this;
    }

    protected Vec3l setX(BigInteger x) {
        this.x = x;
        return this;
    }

    protected Vec3l setY(BigInteger y) {
        this.y = y;
        return this;
    }

    protected Vec3l setZ(BigInteger z) {
        this.z = z;
        return this;
    }

    public Vec3l offset(double p_175587_, double p_175588_, double p_175589_) {
        return p_175587_ == 0.0D && p_175588_ == 0.0D && p_175589_ == 0.0D ? this : new Vec3l(this.getBigDecX().add(p_175587_), this.getBigDecY().add(p_175588_), this.getBigDecZ().add(p_175589_));
    }

    public Vec3l offset(long p_175593_, long p_175594_, long p_175595_) {
        return p_175593_ == 0 && p_175594_ == 0 && p_175595_ == 0 ? this : new Vec3l(this.getBigX().add(p_175593_), this.getBigY().add(p_175594_), this.getBigZ().add(p_175595_));
    }

    public Vec3l offset(me.alphamode.mclong.math.BigInteger x, me.alphamode.mclong.math.BigInteger y, me.alphamode.mclong.math.BigInteger z) {
        return x.equals(me.alphamode.mclong.math.BigInteger.ZERO) && y.equals(me.alphamode.mclong.math.BigInteger.ZERO) && z.equals(me.alphamode.mclong.math.BigInteger.ZERO) ? this : new Vec3l(this.getBigX().add(x), this.getBigY().add(y), this.getBigZ().add(z));
    }

    public Vec3l offset(Vec3l p_175597_) {
        return this.offset(p_175597_.getBigX(), p_175597_.getBigY(), p_175597_.getBigZ());
    }

    public Vec3l subtract(Vec3l p_175596_) {
        return this.offset(p_175596_.getBigX().negate(), p_175596_.getBigY().negate(), p_175596_.getBigZ().negate());
    }

    public Vec3l multiply(int p_175602_) {
        if (p_175602_ == 1) {
            return this;
        } else {
            return p_175602_ == 0 ? ZERO : new Vec3l(this.getBigX().multiply(p_175602_), this.getBigY().multiply(p_175602_), this.getBigZ().multiply(p_175602_));
        }
    }

    public Vec3l above() {
        return this.above(1);
    }

    public Vec3l above(long p_123336_) {
        return this.relative(Direction.UP, p_123336_);
    }

    public Vec3l below() {
        return this.below(1);
    }

    public Vec3l below(long p_123335_) {
        return this.relative(Direction.DOWN, p_123335_);
    }

    public Vec3l north() {
        return this.north(1);
    }

    public Vec3l north(long p_175601_) {
        return this.relative(Direction.NORTH, p_175601_);
    }

    public Vec3l south() {
        return this.south(1);
    }

    public Vec3l south(long p_175600_) {
        return this.relative(Direction.SOUTH, p_175600_);
    }

    public Vec3l west() {
        return this.west(1);
    }

    public Vec3l west(long p_175599_) {
        return this.relative(Direction.WEST, p_175599_);
    }

    public Vec3l east() {
        return this.east(1);
    }

    public Vec3l east(long p_175598_) {
        return this.relative(Direction.EAST, p_175598_);
    }

    public Vec3l relative(Direction p_175592_) {
        return this.relative(p_175592_, 1);
    }

    public Vec3l relative(Direction p_123321_, long p_123322_) {
        return p_123322_ == 0 ? this : new Vec3l(this.getBigX().add(p_123321_.getStepX() * p_123322_), this.getBigY().add(p_123321_.getStepY() * p_123322_), this.getBigZ().add(p_123321_.getStepZ() * p_123322_));
    }

    public Vec3l relative(Direction.Axis p_175590_, long p_175591_) {
        if (p_175591_ == 0) {
            return this;
        } else {
            long i = p_175590_ == Direction.Axis.X ? p_175591_ : 0;
            long j = p_175590_ == Direction.Axis.Y ? p_175591_ : 0;
            long k = p_175590_ == Direction.Axis.Z ? p_175591_ : 0;
            return new Vec3l(this.getBigX().add(i), this.getBigY().add(j), this.getBigZ().add(k));
        }
    }

    public Vec3l cross(Vec3l p_123325_) {
        return new Vec3l(this.getBigY().multiply(p_123325_.getBigZ()).subtract(this.getBigZ().multiply(p_123325_.getBigY())), this.getBigZ().multiply(p_123325_.getBigX()).subtract(this.getBigX().multiply(p_123325_.getBigZ())), this.getBigX().multiply(p_123325_.getBigY()).subtract(this.getBigY().multiply(p_123325_.getBigX())));
    }

    public boolean closerThan(Vec3l p_123315_, double p_123316_) {
        return this.distSqr(p_123315_) < Mth.square(p_123316_);
    }

    public boolean closerToCenterThan(Position p_203196_, double p_203197_) {
        return this.distToCenterSqr(p_203196_) < Mth.square(p_203197_);
    }

    public double distSqr(Vec3l p_123332_) {
        return this.distToLowCornerSqr(p_123332_.getBigDecX(), p_123332_.getBigDecY(), p_123332_.getBigDecZ());
    }

    public double distToCenterSqr(Position p_203194_) {
        return this.distToCenterSqr(p_203194_.getX(), p_203194_.getY(), p_203194_.getZ());
    }

    public double distToCenterSqr(me.alphamode.mclong.math.BigDecimal p_203199_, me.alphamode.mclong.math.BigDecimal p_203200_, me.alphamode.mclong.math.BigDecimal p_203201_) {
        double d0 = (double) this.getBigDecX().add(me.alphamode.mclong.core.BigConstants.AABB).subtract(p_203199_).doubleValue();
        double d1 = (double) this.getBigDecY().add(me.alphamode.mclong.core.BigConstants.AABB).subtract(p_203200_).doubleValue();
        double d2 = (double) this.getBigDecZ().add(me.alphamode.mclong.core.BigConstants.AABB).subtract(p_203201_).doubleValue();
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double distToLowCornerSqr(me.alphamode.mclong.math.BigDecimal x, me.alphamode.mclong.math.BigDecimal y, me.alphamode.mclong.math.BigDecimal z) {
        double d0 = this.getBigDecX().subtract(x).doubleValue();
        double d1 = this.getBigDecY().subtract(y).doubleValue();
        double d2 = this.getBigDecZ().subtract(z).doubleValue();
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public int distManhattan(Vec3l p_123334_) {
        float f = (float) p_123334_.getBigX().subtract(this.getBigX()).abs().floatValue();
        float f1 = (float) p_123334_.getBigY().subtract(this.getBigY()).abs().floatValue();
        float f2 = (float) p_123334_.getBigZ().subtract(this.getBigZ()).abs().floatValue();
        return (int) (f + f1 + f2);
    }

    public long get(Direction.Axis p_123305_) {
        return p_123305_.choose(this.x, this.y, this.z).longValue();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", this.getBigX()).add("y", this.getBigY()).add("z", this.getBigZ()).toString();
    }

    public String toShortString() {
        return this.getBigX() + ", " + this.getBigY() + ", " + this.getBigZ();
    }
}
