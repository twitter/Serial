package com.serial.util.serialization.base;

import com.serial.util.object.ObjectUtils;
import com.serial.util.serialization.SerializationContext;
import com.serial.util.serialization.serializer.Serializer;

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
        return ObjectUtils.cast(this);
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
