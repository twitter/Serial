package com.serial.util.serialization.base;

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
