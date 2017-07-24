package com.serial.util.object;

import com.serial.util.serialization.serializer.BuilderSerializer;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for builders that can be used with the {@link BuilderSerializer} class. {@link ObjectBuilder} is an example
 * of a class that implements this.
 * @param <T> Object type to build
 */
public interface Builder<T> {
    @NotNull
    T build();
}
