package com.serial.util.serialization.base;

import com.serial.util.serialization.serializer.Serializer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * A class that runs serialization and deserialization operations.
 */
public interface Serial {

    @NotNull
    <T> byte[] toByteArray(@Nullable T value, @NotNull Serializer<T> serializer) throws IOException;

    @Nullable
    @Contract("null, _ -> null")
    <T> T fromByteArray(@Nullable byte[] bytes, @NotNull Serializer<T> serializer) throws IOException,
            ClassNotFoundException;
}
