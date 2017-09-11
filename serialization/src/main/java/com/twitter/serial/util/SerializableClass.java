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

import com.twitter.serial.serializer.CoreSerializers;
import com.twitter.serial.serializer.Serializer;

import org.jetbrains.annotations.NotNull;

/**
 * Pairs a class with its serializer, used for mapping the two objects for serialization.
 *
 * @param <T> type of object to be serialized
 */
public class SerializableClass<T> {
    public static final SerializableClass<?> DUMMY = new SerializableClass<>(Object.class, CoreSerializers.EMPTY);

    @NotNull
    public final Class<T> klass;
    @NotNull
    public final Serializer<? super T> serializer;

    public SerializableClass(@NotNull Class<T> klass, @NotNull Serializer<? super T> serializer) {
        this.klass = klass;
        this.serializer = serializer;
    }

    @NotNull
    public static <T> SerializableClass<T> create(@NotNull Class<T> klass, @NotNull Serializer<? super T> serializer) {
        return new SerializableClass<>(klass, serializer);
    }

    @NotNull
    public static final <T> SerializableClass<T> getDummy() {
        return InternalSerialUtils.cast(DUMMY);
    }

    public static final boolean isDummy(@NotNull SerializableClass<?> klass) {
        return klass == DUMMY;
    }
}
