package com.serial.util.serialization.serializer;

import com.serial.util.internal.InternalSerialUtils;
import com.serial.util.serialization.SerializationContext;
import com.serial.util.serialization.base.SerializerInput;
import com.serial.util.serialization.base.SerializerOutput;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * The base class for all serializers. Objects can subclass to define their serialization format.
 *
 * @param <T> class of the objects to serialize.
 *
 * @see ObjectSerializer
 */
public abstract class Serializer<T> {
    public abstract void serialize(@NotNull SerializationContext context,
            @NotNull SerializerOutput output, @Nullable T object) throws IOException;

    @Nullable
    public abstract T deserialize(@NotNull SerializationContext context,
            @NotNull SerializerInput input) throws IOException, ClassNotFoundException;

    @NotNull
    public final T deserializeNotNull(@NotNull SerializationContext context,
            @NotNull SerializerInput input) throws IOException, ClassNotFoundException {
        final T deserializedObject = deserialize(context, input);
        return InternalSerialUtils.checkIsNotNull(deserializedObject);
    }
}
