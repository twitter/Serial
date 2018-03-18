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

package com.twitter.serial.stream.bytebuffer;

import com.twitter.serial.serializer.SerializationContext;
import com.twitter.serial.serializer.Serializer;
import com.twitter.serial.stream.Serial;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.util.InternalSerialUtils;
import com.twitter.serial.util.Pools;
import com.twitter.serial.util.SerializationException;
import com.twitter.serial.util.SerializationUtils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class ByteBufferSerial implements Serial {
    @Nullable
    private final Pools.SynchronizedPool<byte[]> mBufferPool;
    @NotNull
    private final SerializationContext mContext;

    public ByteBufferSerial(int bufferCount, int bufferSize) {
        this(createPool(bufferCount, bufferSize));
    }

    public ByteBufferSerial() {
        this(SerializationContext.ALWAYS_RELEASE, null);
    }

    public ByteBufferSerial(@NotNull SerializationContext context) {
        this(context, null);
    }

    public ByteBufferSerial(@Nullable Pools.SynchronizedPool<byte[]> pool) {
        this(SerializationContext.ALWAYS_RELEASE, pool);
    }

    public ByteBufferSerial(@NotNull SerializationContext context,
            @Nullable Pools.SynchronizedPool<byte[]> pool) {
        mBufferPool = pool;
        mContext = context;
    }

    @Override
    @NotNull
    public <T> byte[] toByteArray(@Nullable T value, @NotNull Serializer<T> serializer)
            throws IOException {
        if (value == null) {
            return InternalSerialUtils.EMPTY_BYTE_ARRAY;
        }
        final Pools.SynchronizedPool<byte[]> currentPool = mBufferPool;
        final byte[] tempBuffer = currentPool != null ? currentPool.acquire() : null;
        if (tempBuffer != null) {
            try {
                synchronized (tempBuffer) {
                    return toByteArray(value, serializer, tempBuffer);
                }
            } finally {
                currentPool.release(tempBuffer);
            }
        }
        return toByteArray(value, serializer, null);
    }

    @NotNull
    public <T> byte[] toByteArray(@Nullable T value, @NotNull Serializer<T> serializer,
            @Nullable byte[] tempBuffer) throws IOException {
        if (value == null) {
            return InternalSerialUtils.EMPTY_BYTE_ARRAY;
        }
        final ByteBufferSerializerOutput serializerOutput = new ByteBufferSerializerOutput(tempBuffer);
        try {
            serializer.serialize(mContext, serializerOutput, value);
        } catch (IOException e) {
            throw e;
        }
        return serializerOutput.getSerializedData();
    }

    @Override
    @Nullable
    @Contract("null, _ -> null")
    public <T> T fromByteArray(@Nullable byte[] bytes, @NotNull Serializer<T> serializer)
            throws IOException,
            ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        final SerializerInput serializerInput = new ByteBufferSerializerInput(bytes);
        try {
            return serializer.deserialize(mContext, serializerInput);
        } catch (IOException | ClassNotFoundException | IllegalStateException e) {
            throw new SerializationException("Invalid serialized data:\n" +
                    SerializationUtils.dumpSerializedData(bytes, serializerInput.getPosition(), mContext.isDebug()), e);
        }
    }

    @NotNull
    private static Pools.SynchronizedPool<byte[]> createPool(int bufferCount, int bufferSize) {
        final Pools.SynchronizedPool<byte[]> pool = new Pools.SynchronizedPool<>(bufferCount);
        for (int i = 0; i < bufferCount; i++) {
            pool.release(new byte[bufferSize]);
        }
        return pool;
    }
}
