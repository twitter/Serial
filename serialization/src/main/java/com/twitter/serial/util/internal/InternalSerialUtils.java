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

package com.twitter.serial.util.internal;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

public class InternalSerialUtils {

    public static final int KB_BYTES = 1024;
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    @NotNull
    @Contract("null -> fail")
    public static <T> T checkIsNotNull(@Nullable T value) {
        if (!(value != null)) {
            throw new AssertionError("Assertion failed.");
        }
        return value;
    }

    /**
     * Returns the given string if not null, otherwise an empty string.
     */
    @NotNull
    public static String getOrEmpty(@Nullable String string) {
        return string == null ? "" : string;
    }

    /**
     * Returns the given value if non null, otherwise the default value.
     */
    @Contract("_, !null -> !null; !null, _ -> !null; null, null -> null")
    public static <T> T getOrDefault(@Nullable T value, @Nullable T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Closes the passed closeable without raising exceptions.
     *
     * @param closeable the object to close or null
     */
    public static void closeSilently(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
            }
        }
    }

    @NotNull
    public static String toHex(@NotNull byte[] data, int offset, int length) {
        final char[] chars = new char[length * 2];
        for (int i = 0; i < length; i++) {
            chars[i * 2] = HEX_DIGITS[(data[offset + i] >> 4) & 0xf];
            chars[i * 2 + 1] = HEX_DIGITS[data[offset + i] & 0xf];
        }
        return new String(chars);
    }
}
