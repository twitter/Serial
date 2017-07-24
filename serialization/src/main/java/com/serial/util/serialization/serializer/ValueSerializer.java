package com.serial.util.serialization.serializer;

import com.serial.util.serialization.SerializationContext;
import com.serial.util.serialization.base.ByteBufferSerializerOutput;
import com.serial.util.serialization.base.SerializationUtils;
import com.serial.util.serialization.base.SerializerDefs;
import com.serial.util.serialization.base.SerializerOutput;
import com.serial.util.serialization.base.SerializerInput;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * A simple serializer that writes the plain fields into the stream without versioning. This is meant to be used with
 * simple classes that have very well defined value semantics and are not expected to change (eg. boxed types,
 * points, vectors...).
 */
public abstract class ValueSerializer<T> extends Serializer<T> {
    @Override
    public final void serialize(@NotNull SerializationContext context,
            @NotNull SerializerOutput output, @Nullable T object) throws IOException {
        if (!SerializationUtils.writeNullIndicator(output, object)) {
            final ByteBufferSerializerOutput byteBufferOutput =
                    output instanceof ByteBufferSerializerOutput ? (ByteBufferSerializerOutput) output : null;
            final int position = byteBufferOutput != null ? byteBufferOutput.getPosition() : 0;

            serializeValue(context, output, object);

            // Check that the value does not start with a null, since it would be ambiguous with a null value.
            if (byteBufferOutput != null && byteBufferOutput.peekTypeAtPosition(position) == SerializerDefs.TYPE_NULL) {
                throw new IllegalStateException("Values with null in the first field are ambiguous.");
            }
        }
    }

    /**
     * Serialize the value into the output serializer. Do NOT write null in the first field, since it would be
     * ambiguous with the serialized null value.
     */
    protected abstract void serializeValue(@NotNull SerializationContext context,
            @NotNull SerializerOutput output, @NotNull T object) throws IOException;

    @Override
    @Nullable
    public final T deserialize(@NotNull SerializationContext context,
            @NotNull SerializerInput input)
            throws IOException, ClassNotFoundException {
        return !SerializationUtils.readNullIndicator(input) ? deserializeValue(context,
                input) : null;
    }

    @NotNull
    protected abstract T deserializeValue(@NotNull SerializationContext context,
            @NotNull SerializerInput input)
            throws IOException, ClassNotFoundException;
}
