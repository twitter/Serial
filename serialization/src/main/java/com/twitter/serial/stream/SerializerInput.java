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

import com.twitter.serial.util.DebugClassDescriptor;
import com.twitter.serial.util.InternalSerialUtils;
import com.twitter.serial.serializer.SerializationContext;
import com.twitter.serial.serializer.Serializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public abstract class SerializerInput extends SerializerStream {

    public abstract byte readByte() throws IOException;

    public abstract boolean readBoolean() throws IOException;

    public abstract int readInt() throws IOException;

    public abstract long readLong() throws IOException;

    public abstract float readFloat() throws IOException;

    public abstract double readDouble() throws IOException;

    @Nullable
    public abstract String readString() throws IOException;

    @NotNull
    public final String readNotNullString() throws IOException {
        return InternalSerialUtils.checkIsNotNull(readString());
    }

    @Nullable
    public abstract byte[] readByteArray() throws IOException;

    @NotNull
    public final byte[] readNotNullByteArray() throws IOException {
        return InternalSerialUtils.checkIsNotNull(readByteArray());
    }

    @Nullable
    public final <T> T readObject(@NotNull SerializationContext context,
            @NotNull Serializer<T> serializer) throws IOException, ClassNotFoundException {
        return serializer.deserialize(context, this);
    }

    @NotNull
    public final <T> T readNotNullObject(@NotNull SerializationContext context,
            @NotNull Serializer<T> serializer) throws IOException, ClassNotFoundException {
        return InternalSerialUtils.checkIsNotNull(readObject(context, serializer));
    }

    public int readObjectStart() throws IOException {
        return 0;
    }

    @NotNull
    public DebugClassDescriptor readDebugObjectStart() throws IOException {
        return new DebugClassDescriptor(readObjectStart());
    }

    public void readObjectEnd() throws IOException {
    }

    public abstract void readNull() throws IOException;

    public byte peekType() {
        return SerializerDefs.TYPE_UNKNOWN;
    }

    public int getPosition() {
        throw new UnsupportedOperationException();
    }
}
