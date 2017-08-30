/*
 * Copyright 2017 Twitter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.serial.serializer;

import com.twitter.serial.stream.bytebuffer.ByteBufferSerializerOutput;
import com.twitter.serial.util.SerializationUtils;
import com.twitter.serial.stream.SerializerDefs;
import com.twitter.serial.stream.SerializerOutput;
import com.twitter.serial.stream.SerializerInput;

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
