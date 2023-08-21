package me.alphamode.mclong.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;

import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface ModdedCodec {
    static Codec<Long> longRange(final long minInclusive, final long maxInclusive) {
        final Function<Long, DataResult<Long>> checker = Codec.checkRange(minInclusive, maxInclusive);
        return Codec.LONG.flatXmap(checker, checker);
    }

    PrimitiveCodec<Stream<String>> STRING_STREAM = new PrimitiveCodec<Stream<String>>() {
        @Override
        public <T> DataResult<Stream<String>> read(final DynamicOps<T> ops, final T input) {
            return ops
                    .getStream(input).map(tStream -> (Stream<String>) tStream);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Stream<String> value) {
            return ops.createList(value.map(ops::createString));
        }

        @Override
        public String toString() {
            return "StringStream";
        }
    };
}
