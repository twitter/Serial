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

package com.twitter.serial.serializer;

/**
 * Provides context about serialization and deserialization environment.
 * <p>
 * Could be configured to debug or release mode which are expected to be mutually exclusive.
 */
public interface SerializationContext {

    /**
     * Default serialization context implementation.
     * Always returns true for {@link SerializationContext#isRelease()}.
     */
    SerializationContext ALWAYS_RELEASE = new SerializationContext() {
        @Override
        public boolean isDebug() {
            return false;
        }

        @Override
        public boolean isRelease() {
            return true;
        }
    };

    /**
     * @return true - if serialization and deserialization should be done for development environment, false - if not.
     */
    boolean isDebug();

    /**
     * @return true - if serialization and deserialization should be done for release environment, false - if not.
     */
    boolean isRelease();
}
