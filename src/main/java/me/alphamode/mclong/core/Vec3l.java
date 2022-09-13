package me.alphamode.mclong.core;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import javax.annotation.concurrent.Immutable;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.util.Mth;

@Immutable
public class Vec3l implements Comparable<Vec3l> {
    public static final Codec<Vec3l> CODEC = Codec.LONG_STREAM.comapFlatMap((p_123318_) -> {
        return Util.fixedSize(p_123318_, 3).map((p_175586_) -> {
            return new Vec3l(p_175586_[0], p_175586_[1], p_175586_[2]);
        });
    }, (p_123313_) -> {
        return LongStream.of(p_123313_.getX(), p_123313_.getY(), p_123313_.getZ());
    });
    public static final Vec3l ZERO = new Vec3l(0, 0, 0);
    private long x;
    private long y;
    private long z;

    private static Function<Vec3l, DataResult<Vec3l>> checkOffsetAxes(int p_194646_) {
        return (p_194649_) -> {
            return Math.abs(p_194649_.getX()) < p_194646_ && Math.abs(p_194649_.getY()) < p_194646_ && Math.abs(p_194649_.getZ()) < p_194646_ ? DataResult.success(p_194649_) : DataResult.error("Position out of range, expected at most " + p_194646_ + ": " + p_194649_);
        };
    }

    public static Codec<Vec3l> offsetCodec(int p_194651_) {
        return CODEC.flatXmap(checkOffsetAxes(p_194651_), checkOffsetAxes(p_194651_));
    }

    public Vec3l(int p_123296_, int p_123297_, int p_123298_) {
        this.x = p_123296_;
        this.y = p_123297_;
        this.z = p_123298_;
    }

    public Vec3l(double p_123292_, double p_123293_, double p_123294_) {
        this(Mth.floor(p_123292_), Mth.floor(p_123293_), Mth.floor(p_123294_));
    }

    public boolean equals(Object p_123327_) {
        if (this == p_123327_) {
            return true;
        } else if (!(p_123327_ instanceof Vec3l)) {
            return false;
        } else {
            Vec3l vec3l = (Vec3l) p_123327_;
            if (this.getX() != vec3l.getX()) {
                return false;
            } else if (this.getY() != vec3l.getY()) {
                return false;
            } else {
                return this.getZ() == vec3l.getZ();
            }
        }
    }

    public int hashCode() {
        return (int) ((this.getY() + this.getZ() * 31) * 31 + this.getX());
    }

    public int compareTo(Vec3l p_123330_) {
        if (this.getY() == p_123330_.getY()) {
            return (int) (this.getZ() == p_123330_.getZ() ? this.getX() - p_123330_.getX() : this.getZ() - p_123330_.getZ());
        } else {
            return (int) (this.getY() - p_123330_.getY());
        }
    }

    public long getX() {
        return this.x;
    }

    public long getY() {
        return this.y;
    }

    public long getZ() {
        return this.z;
    }

    protected Vec3l setX(long p_175605_) {
        this.x = p_175605_;
        return this;
    }

    protected Vec3l setY(long p_175604_) {
        this.y = p_175604_;
        return this;
    }

    protected Vec3l setZ(long p_175603_) {
        this.z = p_175603_;
        return this;
    }

    public Vec3l offset(double p_175587_, double p_175588_, double p_175589_) {
        return p_175587_ == 0.0D && p_175588_ == 0.0D && p_175589_ == 0.0D ? this : new Vec3l((double) this.getX() + p_175587_, (double) this.getY() + p_175588_, (double) this.getZ() + p_175589_);
    }

    public Vec3l offset(long p_175593_, long p_175594_, long p_175595_) {
        return p_175593_ == 0 && p_175594_ == 0 && p_175595_ == 0 ? this : new Vec3l(this.getX() + p_175593_, this.getY() + p_175594_, this.getZ() + p_175595_);
    }

    public Vec3l offset(Vec3l p_175597_) {
        return this.offset(p_175597_.getX(), p_175597_.getY(), p_175597_.getZ());
    }

    public Vec3l subtract(Vec3l p_175596_) {
        return this.offset(-p_175596_.getX(), -p_175596_.getY(), -p_175596_.getZ());
    }

    public Vec3l multiply(int p_175602_) {
        if (p_175602_ == 1) {
            return this;
        } else {
            return p_175602_ == 0 ? ZERO : new Vec3l(this.getX() * p_175602_, this.getY() * p_175602_, this.getZ() * p_175602_);
        }
    }

    public Vec3l above() {
        return this.above(1);
    }

    public Vec3l above(int p_123336_) {
        return this.relative(Direction.UP, p_123336_);
    }

    public Vec3l below() {
        return this.below(1);
    }

    public Vec3l below(int p_123335_) {
        return this.relative(Direction.DOWN, p_123335_);
    }

    public Vec3l north() {
        return this.north(1);
    }

    public Vec3l north(int p_175601_) {
        return this.relative(Direction.NORTH, p_175601_);
    }

    public Vec3l south() {
        return this.south(1);
    }

    public Vec3l south(int p_175600_) {
        return this.relative(Direction.SOUTH, p_175600_);
    }

    public Vec3l west() {
        return this.west(1);
    }

    public Vec3l west(int p_175599_) {
        return this.relative(Direction.WEST, p_175599_);
    }

    public Vec3l east() {
        return this.east(1);
    }

    public Vec3l east(int p_175598_) {
        return this.relative(Direction.EAST, p_175598_);
    }

    public Vec3l relative(Direction p_175592_) {
        return this.relative(p_175592_, 1);
    }

    public Vec3l relative(Direction p_123321_, int p_123322_) {
        return p_123322_ == 0 ? this : new Vec3l(this.getX() + p_123321_.getStepX() * p_123322_, this.getY() + p_123321_.getStepY() * p_123322_, this.getZ() + p_123321_.getStepZ() * p_123322_);
    }

    public Vec3l relative(Direction.Axis p_175590_, int p_175591_) {
        if (p_175591_ == 0) {
            return this;
        } else {
            int i = p_175590_ == Direction.Axis.X ? p_175591_ : 0;
            int j = p_175590_ == Direction.Axis.Y ? p_175591_ : 0;
            int k = p_175590_ == Direction.Axis.Z ? p_175591_ : 0;
            return new Vec3l(this.getX() + i, this.getY() + j, this.getZ() + k);
        }
    }

    public Vec3l cross(Vec3l p_123325_) {
        return new Vec3l(this.getY() * p_123325_.getZ() - this.getZ() * p_123325_.getY(), this.getZ() * p_123325_.getX() - this.getX() * p_123325_.getZ(), this.getX() * p_123325_.getY() - this.getY() * p_123325_.getX());
    }

    public boolean closerThan(Vec3l p_123315_, double p_123316_) {
        return this.distSqr(p_123315_) < Mth.square(p_123316_);
    }

    public boolean closerToCenterThan(Position p_203196_, double p_203197_) {
        return this.distToCenterSqr(p_203196_) < Mth.square(p_203197_);
    }

    public double distSqr(Vec3l p_123332_) {
        return this.distToLowCornerSqr((double) p_123332_.getX(), (double) p_123332_.getY(), (double) p_123332_.getZ());
    }

    public double distToCenterSqr(Position p_203194_) {
        return this.distToCenterSqr(p_203194_.x(), p_203194_.y(), p_203194_.z());
    }

    public double distToCenterSqr(double p_203199_, double p_203200_, double p_203201_) {
        double d0 = (double) this.getX() + 0.5D - p_203199_;
        double d1 = (double) this.getY() + 0.5D - p_203200_;
        double d2 = (double) this.getZ() + 0.5D - p_203201_;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double distToLowCornerSqr(double p_203203_, double p_203204_, double p_203205_) {
        double d0 = (double) this.getX() - p_203203_;
        double d1 = (double) this.getY() - p_203204_;
        double d2 = (double) this.getZ() - p_203205_;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public int distManhattan(Vec3l p_123334_) {
        float f = (float) Math.abs(p_123334_.getX() - this.getX());
        float f1 = (float) Math.abs(p_123334_.getY() - this.getY());
        float f2 = (float) Math.abs(p_123334_.getZ() - this.getZ());
        return (int) (f + f1 + f2);
    }

    public long get(Direction.Axis p_123305_) {
        return p_123305_.choose(this.x, this.y, this.z);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }

    public String toShortString() {
        return this.getX() + ", " + this.getY() + ", " + this.getZ();
    }
}
