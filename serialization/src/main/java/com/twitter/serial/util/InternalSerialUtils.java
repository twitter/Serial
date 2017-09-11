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

package com.twitter.serial.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

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

    /**
     * Repeats a string n times.
     *
     * @param string - The string to repeat.
     * @param repetitions - Number of times to repeat `string`.
     * @return The generated string.
     */
    @NotNull
    public static String repeat(@NotNull String string, int repetitions) {
        final StringBuilder builder = new StringBuilder(repetitions * string.length());
        for (int i = 0; i < repetitions; i++) {
            builder.append(string);
        }
        return builder.toString();
    }

    @NotNull
    public static String lineSeparator() {
        return System.getProperty("line.separator");
    }

    @Contract("!null -> !null; null -> null")
    public static <T> T cast(@Nullable Object object) {
        //noinspection unchecked
        return (T) object;
    }

    /**
     * Compares two given objects by using {@link Object#equals(Object)} and accepting null
     * references.
     */
    @Contract("null, null -> true; null, _ -> false; _, null -> false")
    public static <T> boolean equals(@Nullable T o1, @Nullable T o2) {
        return o1 == null && o2 == null || o1 != null && o1.equals(o2);
    }

    /**
     * Returns the hash code of an object, and 0 if it's null.
     */
    public static <T> int hashCode(@Nullable T o) {
        //noinspection SSBasedInspection
        return o != null ? o.hashCode() : 0;
    }

    /**
     * Returns the hashcode for 2 objects.
     * Use this over .hash() since it will create an implicit varargs array,
     * and using .hash() in critical paths will impact performance due to the number of arrays created.
     */
    public static <T1, T2> int hashCode(@Nullable T1 o1, @Nullable T2 o2) {
        int result = hashCode(o1);
        result = 31 * result + hashCode(o2);
        return result;
    }

    public static int hashCode(@Nullable Iterable<?> iterable) {
        if (iterable == null) {
            return 0;
        }
        int hash = 1;
        for (Object item : iterable) {
            hash = hash * 31 + hashCode(item);
        }
        return hash;
    }

    /**
     * Convenience wrapper for {@link java.util.Arrays#hashCode}, adding varargs.
     * This can be used to compute a hash code for an object's fields as follows:
     * {@code ObjectUtils.hash(a, b, c)}.
     *
     * Same as Objects.hash(Object... values), which is only available after API 19
     */
    public static int hashCode(@Nullable Object value, @NotNull Object... values) {
        return Arrays.hashCode(values) * 31 + hashCode(value);
    }
}
