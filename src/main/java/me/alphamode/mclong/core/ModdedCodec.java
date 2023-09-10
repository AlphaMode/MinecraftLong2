package me.alphamode.mclong.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface ModdedCodec {
    static Codec<Long> longRange(final long minInclusive, final long maxInclusive) {
        final Function<Long, DataResult<Long>> checker = Codec.checkRange(minInclusive, maxInclusive);
        return Codec.LONG.flatXmap(checker, checker);
    }

    static <T> DataResult<Stream<String>> getStringStream(DynamicOps<T> ops, final T input) {
        return ops.getStream(input).flatMap(stream -> {
            final List<T> list = stream.toList();
            if (list.stream().allMatch(element -> ops.getStringValue(element).result().isPresent())) {
                return DataResult.success(list.stream().map(element -> ops.getStringValue(element).result().get()));
            }
            return DataResult.error(() -> "Some elements are not ints: " + input);
        });
    }

    PrimitiveCodec<Stream<String>> STRING_STREAM = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Stream<String>> read(final DynamicOps<T> ops, final T input) {
            return getStringStream(ops, input);
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
