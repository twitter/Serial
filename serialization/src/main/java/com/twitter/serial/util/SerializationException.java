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
