package com.serial.util.serialization.serializer;

import com.serial.util.serialization.SerializationContext;
import com.serial.util.serialization.base.SerializationException;
import com.serial.util.serialization.base.SerializationUtils;
import com.serial.util.serialization.base.SerializerInput;
import com.serial.util.serialization.base.SerializerOutput;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * A base serializer that uses versioning for backwards compatibility.
 *
 * @param <T> class of the objects to serialize.
 *
 * See https://docbird.twitter.biz/twitter_for_android/data_models/serialization.html.
 * See https://docbird.twitter.biz/twitter_for_android/data_models/serialization.html#defining-serializers.
 * See https://docbird.twitter.biz/twitter_for_android/data_models/serialization.html#updating-serializers.
 */
public abstract class ObjectSerializer<T> extends Serializer<T> {
    public static final short DEFAULT_VERSION = 0;

    // null indicators for nullable object
    public static final byte NULL_OBJECT = 0;
    public static final byte NOT_NULL_OBJECT = 1;

    protected final int mVersionNumber;

    protected ObjectSerializer() {
        mVersionNumber = DEFAULT_VERSION;
    }

    protected ObjectSerializer(int versionNumber) {
        if (versionNumber < DEFAULT_VERSION) {
            throw new IllegalArgumentException("The version number is negative: " + versionNumber + ".");
        }
        mVersionNumber = versionNumber;
    }

    @Override
    public final void serialize(@NotNull SerializationContext context,
            @NotNull SerializerOutput output, @Nullable T object) throws IOException {
        if (!SerializationUtils.writeNullIndicator(output, object)) {
            if (context.isDebug()) {
                output.writeObjectStart(mVersionNumber, getClass().getSimpleName());
            } else {
                output.writeObjectStart(mVersionNumber);
            }
            //noinspection BlacklistedMethod
            serializeObject(context, output, object);
            output.writeObjectEnd();
        }
    }

    protected abstract void serializeObject(@NotNull SerializationContext context,
            @NotNull SerializerOutput output, @NotNull T object) throws IOException;

    @Nullable
    @Override
    public T deserialize(@NotNull SerializationContext context, @NotNull SerializerInput input)
            throws IOException, ClassNotFoundException {
        if (SerializationUtils.readNullIndicator(input)) {
            return null;
        }
        final int deserializedVersionNumber = input.readObjectStart();
        if (deserializedVersionNumber > mVersionNumber) {
            throw new SerializationException("Version number found (" + deserializedVersionNumber + ") is " +
                    "greater than the maximum supported value (" + mVersionNumber + ")");
        }
        final T deserializedObject = deserializeObject(context, input, deserializedVersionNumber);
        input.readObjectEnd();
        return deserializedObject;
    }

    @Nullable
    protected abstract T deserializeObject(@NotNull SerializationContext context,
            @NotNull SerializerInput input, int versionNumber)
            throws IOException, ClassNotFoundException;
}
