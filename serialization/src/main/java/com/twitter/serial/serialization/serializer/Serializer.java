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

package com.twitter.serial.serialization.serializer;

import com.twitter.serial.util.internal.InternalSerialUtils;
import com.twitter.serial.serialization.SerializationContext;
import com.twitter.serial.serialization.base.SerializerInput;
import com.twitter.serial.serialization.base.SerializerOutput;

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
