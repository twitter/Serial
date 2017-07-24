package com.serial.util.serialization.base;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SerializationException extends IOException {
    private static final long serialVersionUID = 1354673450935990055L;

    public SerializationException(@NotNull String message) {
        super(message);
    }

    public SerializationException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
