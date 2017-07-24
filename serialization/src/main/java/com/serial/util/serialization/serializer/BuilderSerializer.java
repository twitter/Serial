package com.serial.util.serialization.serializer;

import com.serial.util.object.Builder;
import com.serial.util.serialization.SerializationContext;
import com.serial.util.serialization.base.OptionalFieldException;
import com.serial.util.serialization.base.SerializationException;
import com.serial.util.serialization.base.SerializationUtils;
import com.serial.util.serialization.base.SerializerInput;

import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.OptionalDataException;

/**
 * A base serializer for a class that has an {@link Builder}. The serialized data delimits the boundaries
 * of the object and provides basic support for versioning.
 */
public abstract class BuilderSerializer<T, B extends Builder<T>> extends ObjectSerializer<T> {
    protected BuilderSerializer() {
    }

    protected BuilderSerializer(int versionNumber) {
        super(versionNumber);
    }

    public void deserialize(@NotNull SerializationContext context, @NotNull SerializerInput input,
            @NotNull B builder)
            throws IOException, ClassNotFoundException {
        if (SerializationUtils.readNullIndicator(input)) {
            return;
        }
        final int deserializedVersionNumber = input.readObjectStart();
        if (deserializedVersionNumber > mVersionNumber) {
            throw new SerializationException(
                    "Version number found (" + deserializedVersionNumber + ") is " +
                            "greater than the maximum supported value (" + mVersionNumber + ")");
        }
        deserialize(context, input, builder, deserializedVersionNumber);
        input.readObjectEnd();
    }

    @NotNull
    @Override
    protected final T deserializeObject(@NotNull SerializationContext context,
            @NotNull SerializerInput input, int versionNumber)
            throws IOException, ClassNotFoundException {
        final B builder = createBuilder();
        deserialize(context, input, builder, versionNumber);
        return builder.build();
    }

    private void deserialize(@NotNull SerializationContext context, @NotNull SerializerInput input, @NotNull B builder,
            int versionNumber)
            throws IOException, ClassNotFoundException {
        try {
            //noinspection BlacklistedMethod
            deserializeToBuilder(context, input, builder, versionNumber);
        } catch (OptionalDataException | EOFException | OptionalFieldException ignore) {
            // This may happen when reading optional fields. The builder should already
            // contain all the available fields, so just ignore the exception.
        }
    }

    @NotNull
    protected abstract B createBuilder();

    protected abstract void deserializeToBuilder(@NotNull SerializationContext context,
            @NotNull SerializerInput input, @NotNull B builder, int versionNumber)
            throws IOException, ClassNotFoundException;
}
