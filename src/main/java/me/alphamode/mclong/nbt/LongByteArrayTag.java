package me.alphamode.mclong.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.minecraft.nbt.*;

public class LongByteArrayTag implements Tag {
    private static final int SELF_SIZE_IN_BYTES = 24;
    public static final TagType<LongByteArrayTag> TYPE = new TagType.VariableSize<LongByteArrayTag>() {
        public LongByteArrayTag load(DataInput input, int p_128248_, NbtAccounter p_128249_) throws IOException {
            p_128249_.accountBytes(24L);
            int i = input.readInt();
            p_128249_.accountBytes(1L * (long)i);
            Long2ByteMap abyte = new Long2ByteOpenHashMap(i);

            return new LongByteArrayTag(abyte);
        }

        public StreamTagVisitor.ValueResult parse(DataInput p_197433_, StreamTagVisitor p_197434_) throws IOException {
            int i = p_197433_.readInt();
            byte[] abyte = new byte[i];
            p_197433_.readFully(abyte);
            return p_197434_.visit(abyte);
        }

        public void skip(DataInput p_197431_) throws IOException {
            p_197431_.skipBytes(p_197431_.readInt() * 1);
        }

        public String getName() {
            return "BYTE[]";
        }

        public String getPrettyName() {
            return "TAG_Byte_Array";
        }
    };
    private Long2ByteMap data;

    public LongByteArrayTag(Long2ByteMap p_128191_) {
        this.data = p_128191_;
    }

    public void write(DataOutput p_128202_) throws IOException {
        p_128202_.writeInt(this.data.size());
//        p_128202_.write(this.data);
    }

    public int sizeInBytes() {
        return 24 + 1 * this.data.size();
    }

    public byte getId() {
        return 13;
    }

    public TagType<LongByteArrayTag> getType() {
        return TYPE;
    }

    public String toString() {
        return this.getAsString();
    }

    public Tag copy() {
        return new LongByteArrayTag(new Long2ByteOpenHashMap(this.data));
    }

    public boolean equals(Object p_128233_) {
        if (this == p_128233_) {
            return true;
        } else {
            return p_128233_ instanceof LongByteArrayTag && this.data.equals(((LongByteArrayTag)p_128233_).data);
        }
    }

    public int hashCode() {
        return this.data.hashCode();
    }

    public void accept(TagVisitor p_177839_) {
        p_177839_.visitLongByteArray(this);
    }

    public Long2ByteMap getData() {
        return this.data;
    }

    public int size() {
        return this.data.size();
    }

    public ByteTag get(long p_128194_) {
        return ByteTag.valueOf(this.data.get(p_128194_));
    }

    public ByteTag set(long p_128196_, ByteTag p_128197_) {
        byte b0 = this.data.get(p_128196_);
        this.data.put(p_128196_, p_128197_.getAsByte());
        return ByteTag.valueOf(b0);
    }

    public void add(long p_128215_, ByteTag p_128216_) {
        this.data.put(p_128215_, p_128216_.getAsByte());
    }

    public boolean setTag(long p_128199_, Tag p_128200_) {
        if (p_128200_ instanceof NumericTag) {
            this.data.put(p_128199_, ((NumericTag)p_128200_).getAsByte());
            return true;
        } else {
            return false;
        }
    }

    public boolean addTag(long p_128218_, Tag p_128219_) {
        if (p_128219_ instanceof NumericTag) {
            this.data.put(p_128218_, ((NumericTag)p_128219_).getAsByte());
            return true;
        } else {
            return false;
        }
    }

    public ByteTag remove(long p_128213_) {
        byte b0 = this.data.remove(p_128213_);
        return ByteTag.valueOf(b0);
    }

    public byte getElementType() {
        return 1;
    }

    public void clear() {
        this.data.clear();
    }

    public StreamTagVisitor.ValueResult accept(StreamTagVisitor p_197429_) {
        return p_197429_.visit(this.data);
    }
}