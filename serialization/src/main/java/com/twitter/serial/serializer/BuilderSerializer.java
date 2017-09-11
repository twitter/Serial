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

import com.twitter.serial.object.Builder;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.util.OptionalFieldException;
import com.twitter.serial.util.SerializationException;
import com.twitter.serial.util.SerializationUtils;

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
