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

package com.twitter.serial.stream.legacy;

import com.twitter.serial.stream.Serial;
import com.twitter.serial.util.InternalSerialUtils;
import com.twitter.serial.serializer.SerializationContext;
import com.twitter.serial.serializer.Serializer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class LegacySerial implements Serial {

    @NotNull
    private SerializationContext mContext;

    public LegacySerial() {
        this(SerializationContext.ALWAYS_RELEASE);
    }

    public LegacySerial(@NotNull SerializationContext context) {
        mContext = context;
    }

    /**
     * Serialize the given value and compress the result bytes. This method should be used only for values
     * that may occupy large amount of memories. Do not use this method for small objects because the
     * compression incurs performance overheads and the compressed data cannot be inspected using
     * SerializationUtils.dumpSerializedData
     *
     * @param value the value to be serialized.
     * @param serializer the serializer used to serialize value.
     * @return compressed bytes of the serialized value.
     */
    @Override
    @NotNull
    public <T> byte[] toByteArray(@Nullable T value, @NotNull Serializer<T> serializer) throws IOException {
        if (value == null) {
            return InternalSerialUtils.EMPTY_BYTE_ARRAY;
        }
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = null;
        try {
            objectOutput = new ObjectOutputStream(byteOutputStream);
            serializer.serialize(mContext, new LegacySerializerOutput(objectOutput), value);
        } catch (IOException e) {
            throw e;
        } finally {
            if (objectOutput != null) {
                try {
                    objectOutput.close();
                } catch (IOException ignore) { }
            }
        }
        return byteOutputStream.toByteArray();
    }

    /**
     * Deserialize the value that was serialized by toCompressedByteArray().
     *
     * @param bytes the bytes returned by toCompressedByteArray().
     * @param serializer the serializer used to deserialize value.
     * @return the value.
     */
    @Override
    @Nullable
    @Contract("null, _ -> null")
    public <T> T fromByteArray(@Nullable byte[] bytes, @NotNull Serializer<T> serializer) throws IOException,
            ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ObjectInputStream objectInput = null;
        try {
            objectInput = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return serializer.deserialize(mContext, new LegacySerializerInput(objectInput));
        } catch (IOException | ClassNotFoundException | IllegalStateException e) {
            throw e;
        } finally {
            if (objectInput != null) {
                try {
                    objectInput.close();
                } catch (IOException ignore) { }
            }
        }
    }

    @NotNull
    public <T> byte[] toCompressedByteArray(@Nullable T value, @NotNull Serializer<T> serializer) throws IOException {
        if (value == null) {
            return InternalSerialUtils.EMPTY_BYTE_ARRAY;
        }
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = null;
        try {
            objectOutput = new ObjectOutputStream(new GZIPOutputStream(byteOutputStream));
            serializer.serialize(mContext, new LegacySerializerOutput(objectOutput), value);
        } catch (IOException e) {
            throw e;
        } finally {
            if (objectOutput != null) {
                try {
                    objectOutput.close();
                } catch (IOException ignore) { }
            }
        }
        return byteOutputStream.toByteArray();
    }

    @Nullable
    @Contract("null, _ -> null")
    public <T> T fromCompressedByteArray(@Nullable byte[] bytes, @NotNull Serializer<T> serializer) throws IOException,
            ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ObjectInputStream objectInput = null;
        try {
            objectInput = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)));
            return serializer.deserialize(mContext, new LegacySerializerInput(objectInput));
        } catch (IOException | ClassNotFoundException | IllegalStateException e) {
            throw e;
        } finally {
            if (objectInput != null) {
                try {
                    objectInput.close();
                } catch (IOException ignore) { }
            }
        }
    }
}
