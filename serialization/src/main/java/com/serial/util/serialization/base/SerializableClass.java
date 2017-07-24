package com.serial.util.serialization.base;

import com.serial.util.object.ObjectUtils;
import com.serial.util.serialization.serializer.CoreSerializers;
import com.serial.util.serialization.serializer.Serializer;

import org.jetbrains.annotations.NotNull;

/**
 * Pairs a class with its serializer, used for mapping the two objects for serialization.
 *
 * @param <T> type of object to be serialized
 */
public class SerializableClass<T> {
    public static final SerializableClass<?> DUMMY = new SerializableClass<>(Object.class, CoreSerializers.EMPTY);

    @NotNull
    public final Class<T> klass;
    @NotNull
    public final Serializer<? super T> serializer;

    public SerializableClass(@NotNull Class<T> klass, @NotNull Serializer<? super T> serializer) {
        this.klass = klass;
        this.serializer = serializer;
    }

    @NotNull
    public static <T> SerializableClass<T> create(@NotNull Class<T> klass, @NotNull Serializer<? super T> serializer) {
        return new SerializableClass<>(klass, serializer);
    }

    @NotNull
    public static final <T> SerializableClass<T> getDummy() {
        return ObjectUtils.cast(DUMMY);
    }

    public static final boolean isDummy(@NotNull SerializableClass<?> klass) {
        return klass == DUMMY;
    }
}
