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

import com.twitter.serial.SerializationTestUtils;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.stream.SerializerOutput;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;

public class ValueSerializerTests {
    @Test
    public void testValueSerialization() throws Exception {
        final SerializationTestUtils.TestObject testObject =
                new SerializationTestUtils.TestObject("test", 5);
        SerializationTestUtils.checkSerialization(testObject, SerializationTestUtils.TestObject.VALUE_SERIALIZER);
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidValueSerializerThrows() throws Exception {
        SerializationTestUtils.checkSerialization(5, new InvalidValueSerializer());
    }

    public static class InvalidValueSerializer extends ValueSerializer<Integer> {
        @Override
        protected void serializeValue(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Integer integer) throws IOException {
            output.writeNull().writeInt(integer);
        }

        @Override
        @NotNull
        protected Integer deserializeValue(@NotNull SerializationContext context,
                @NotNull SerializerInput input) throws IOException, ClassNotFoundException {
            input.readNull();
            return input.readInt();
        }
    }
}
