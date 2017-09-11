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
