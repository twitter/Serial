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

package com.twitter.serial.object;

import com.twitter.serial.serializer.BuilderSerializer;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for builders that can be used with the {@link BuilderSerializer} class. {@link ObjectBuilder} is an example
 * of a class that implements this.
 * @param <T> Object type to build
 */
public interface Builder<T> {
    @NotNull
    T build();
}
