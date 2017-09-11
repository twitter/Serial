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

package com.twitter.serial.stream;

import com.twitter.serial.util.InternalSerialUtils;
import com.twitter.serial.serializer.SerializationContext;
import com.twitter.serial.serializer.Serializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class SerializerOutput<S extends SerializerOutput> extends SerializerStream {

    @NotNull
    public abstract S writeByte(byte val) throws IOException;

    @NotNull
    public abstract S writeBoolean(boolean val) throws IOException;

    @NotNull
    public abstract S writeInt(int val) throws IOException;

    @NotNull
    public abstract S writeLong(long val) throws IOException;

    @NotNull
    public abstract S writeFloat(float val) throws IOException;

    @NotNull
    public abstract S writeDouble(double val) throws IOException;

    @NotNull
    public abstract S writeString(@Nullable String val) throws IOException;

    @NotNull
    public abstract S writeByteArray(@Nullable byte[] val) throws IOException;

    @NotNull
    public final <T> S writeObject(@NotNull SerializationContext context, @Nullable T val,
            @NotNull Serializer<T> serializer) throws IOException {
        serializer.serialize(context, this, val);
        return InternalSerialUtils.cast(this);
    }

    @NotNull
    public abstract S writeNull() throws IOException;

    @NotNull
    public abstract S writeObjectStart(int versionNumber) throws IOException;

    @NotNull
    public S writeObjectStart(int versionNumber, @NotNull String className) throws IOException {
        return writeObjectStart(versionNumber);
    }

    @NotNull
    public abstract S writeObjectEnd() throws IOException;
}
