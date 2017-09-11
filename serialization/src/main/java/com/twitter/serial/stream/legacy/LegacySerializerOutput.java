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

import com.twitter.serial.stream.SerializerOutput;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectOutput;

public class LegacySerializerOutput extends SerializerOutput<LegacySerializerOutput> {
    @NotNull private final ObjectOutput mOutput;
    private final boolean mUseVersionNumbers;

    public LegacySerializerOutput(@NotNull ObjectOutput output) {
        this(output, true);
    }

    public LegacySerializerOutput(@NotNull ObjectOutput mOutput, boolean useVersionNumbers) {
        this.mOutput = mOutput;
        this.mUseVersionNumbers = useVersionNumbers;
    }

    @Override
    @NotNull
    public LegacySerializerOutput writeByte(byte val) throws IOException {
        mOutput.writeByte(val);
        return this;
    }

    @Override
    @NotNull
    public LegacySerializerOutput writeBoolean(boolean val) throws IOException {
        mOutput.writeBoolean(val);
        return this;
    }

    @Override
    @NotNull
    public LegacySerializerOutput writeInt(int val) throws IOException {
        mOutput.writeInt(val);
        return this;
    }

    @Override
    @NotNull
    public LegacySerializerOutput writeLong(long val) throws IOException {
        mOutput.writeLong(val);
        return this;
    }

    @Override
    @NotNull
    public LegacySerializerOutput writeFloat(float val) throws IOException {
        mOutput.writeFloat(val);
        return this;
    }

    @Override
    @NotNull
    public LegacySerializerOutput writeDouble(double val) throws IOException {
        mOutput.writeDouble(val);
        return this;
    }

    @Override
    @NotNull
    public LegacySerializerOutput writeString(@Nullable String val) throws IOException {
        if (val == null) {
            writeByte(LegacySerializerDefs.NULL_OBJECT);
        } else {
            writeByte(LegacySerializerDefs.NOT_NULL_OBJECT);
            mOutput.writeUTF(val);
        }
        return this;
    }

    @NotNull
    @Override
    public LegacySerializerOutput writeByteArray(@Nullable byte[] val) throws IOException {
        if (val == null) {
            writeByte(LegacySerializerDefs.NULL_OBJECT);
        } else {
            writeByte(LegacySerializerDefs.NOT_NULL_OBJECT);
            writeInt(val.length);
            mOutput.write(val);
        }
        return this;
    }

    @Override
    @NotNull
    public LegacySerializerOutput writeNull() throws IOException {
        mOutput.writeObject(null);
        return this;
    }

    @Override
    @NotNull
    public LegacySerializerOutput writeObjectStart(int versionNumber) throws IOException {
        if (mUseVersionNumbers) {
            writeInt(versionNumber);
        }
        return this;
    }

    @NotNull
    @Override
    public LegacySerializerOutput writeObjectStart(int versionNumber, @NotNull String className) throws IOException {
        return writeObjectStart(versionNumber);
    }

    @Override
    @NotNull
    public LegacySerializerOutput writeObjectEnd() {
        return this;
    }
}
