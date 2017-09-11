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

import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.util.SerializationException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;

public class LegacySerializerInput extends SerializerInput {
    @NotNull private final ObjectInput mInput;
    private final boolean mUseVersionNumbers;

    public LegacySerializerInput(@NotNull ObjectInput input) {
        this(input, true);
    }

    public LegacySerializerInput(@NotNull ObjectInput mInput, boolean mUseVersionNumbers) {
        this.mInput = mInput;
        this.mUseVersionNumbers = mUseVersionNumbers;
    }

    @Override
    public byte readByte() throws IOException {
        return mInput.readByte();
    }

    @Override
    public boolean readBoolean() throws IOException {
        return mInput.readBoolean();
    }

    @Override
    public int readInt() throws IOException {
        return mInput.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return mInput.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return mInput.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return mInput.readDouble();
    }

    @Override
    @Nullable
    public String readString() throws IOException {
        final byte nullIndicator = readByte();
        if (nullIndicator == LegacySerializerDefs.NULL_OBJECT) {
            return null;
        }
        return mInput.readUTF();
    }

    @Nullable
    @Override
    public byte[] readByteArray() throws IOException {
        final byte nullIndicator = readByte();
        if (nullIndicator == LegacySerializerDefs.NULL_OBJECT) {
            return null;
        }
        final int length = readInt();
        final byte[] data = new byte[length];
        final int readLength = mInput.read(data);
        if (readLength != length) {
            throw new SerializationException("Expected byte[] of length " + length + " but only read " + readLength);
        }
        return data;
    }

    @Override
    public void readNull() throws IOException {
        try {
            final Object o = mInput.readObject();
            if (o != null) {
                throw new SerializationException("Expected null object but found " + o);
            }
        } catch (ClassNotFoundException e) {
            throw new SerializationException("Expected null object but found unclassified object", e);
        }
    }

    @Override
    public int readObjectStart() throws IOException {
        if (mUseVersionNumbers) {
            return mInput.readInt();
        }
        return 0;
    }
}
