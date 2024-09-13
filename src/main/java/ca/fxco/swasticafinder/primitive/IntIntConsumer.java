package ca.fxco.swasticafinder.primitive;

import it.unimi.dsi.fastutil.ints.IntConsumer;

import java.util.function.BiConsumer;

// Primitive for: BiConsumer<Integer, Consumer<Integer>> - BiConsumer<Integer, IntConsumer>
@FunctionalInterface
public interface IntIntConsumer extends BiConsumer<Integer, IntConsumer> {

    void accept(int i, IntConsumer intConsumer);

    @Override
    default void accept(Integer i, IntConsumer intConsumer) {
        accept(i.intValue(), intConsumer);
    }
}
