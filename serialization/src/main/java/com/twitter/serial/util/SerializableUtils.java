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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.Map;

/**
 * Provides methods to serialize a {@link Serializable} object to a byte array using Java serialization within the
 * custom byte stream.
 */
public class SerializableUtils {

    private static final int BYTE_ARRAY_BUFFER_SIZE = 512;

    /**
     * Converts the specified object to a byte array, failing silently. If an exception occurs, null
     * is returned.
     *
     * @param o The object to convert to a byte array.
     * @return byte array or null if an exception occurs.
     */
    @Nullable
    @Contract("null -> null")
    public static byte[] toByteArray(@Nullable Serializable o) throws IOException {
        if (o == null) {
            return null;
        }
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream(BYTE_ARRAY_BUFFER_SIZE);
        ObjectOutputStream objectStream = null;
        try {
            objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(o);
            return byteStream.toByteArray();
        } finally {
            InternalSerialUtils.closeSilently(objectStream);
            InternalSerialUtils.closeSilently(byteStream);
        }
    }

    @Nullable
    @Contract("null -> null")
    public static <T> T fromByteArray(@Nullable byte[] bytes) throws IOException, ClassNotFoundException {
        //noinspection BlacklistedMethod
        return fromByteArray(bytes, null);
    }

    /**
     * Deserializes an object from a byte array using regular Java serialization. The class mapping can be used
     * to replace a class in the stream (identified by uid) by another class. The replacement class must have the
     * same name and serialization id, but it may be located in a different package.
     */
    @Nullable
    @Contract("null, _ -> null")
    public static <T> T fromByteArray(@Nullable byte[] bytes, @Nullable final Map<Long, Class<?>> classMapping)
            throws IOException, ClassNotFoundException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        final ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectStream = null;
        try {
            if (classMapping == null) {
                objectStream = new ObjectInputStream(byteStream);
            } else {
                objectStream = new ObjectInputStream(byteStream) {
                    @Override
                    @NotNull
                    protected Class<?> resolveClass(@NotNull ObjectStreamClass osClass)
                            throws IOException, ClassNotFoundException {
                        final Class<?> resolvedClass = classMapping.get(osClass.getSerialVersionUID());
                        if (resolvedClass != null) {
                            return resolvedClass;
                        }
                        return super.resolveClass(osClass);
                    }
                };
            }
            final T item = (T) objectStream.readObject();
            return item;
        } catch (IOException | ClassNotFoundException | IllegalStateException e) {
            throw e;
        } finally {
            InternalSerialUtils.closeSilently(objectStream);
            InternalSerialUtils.closeSilently(byteStream);
        }
    }

}
