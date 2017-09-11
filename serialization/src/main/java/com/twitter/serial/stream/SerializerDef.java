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

package com.twitter.serial.stream;

import org.jetbrains.annotations.NotNull;

/**
 * Defines the location of the serializer. {@link #type()} defines the class the serializer instance is in, and the
 * {@link #fieldName()} defines the name of the field holding the static instance.
 */
public @interface SerializerDef {
    String DEFAULT_SERIALIZER_NAME = "SERIALIZER";

    /**
     * @return the serializer class to look for the serializer instance in; only valid if the return type is deemed
     * serializable.
     */
    @NotNull Class<?> type();

    /**
     * @return the name of the serializer instance field; default "SERIALIZER". Only valid if the return type is deemed
     * serializable.
     */
    @NotNull String fieldName() default DEFAULT_SERIALIZER_NAME;
}
