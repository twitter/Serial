package com.serial.util.serialization.base;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * If you add a field to the end of an object, your new serializer will reach the end of an old object when trying to
 * read the new field, which will cause it to throw an OptionalFieldException.
 */
public class OptionalFieldException extends IOException {
    private static final long serialVersionUID = 3220575393192463254L;

    public OptionalFieldException(@Nullable String message) {
        super(message);
    }

    public OptionalFieldException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
