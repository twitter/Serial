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

import com.twitter.serial.stream.SerializerDefs;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class SerializerDefsTests {
    @Test
    public void testGetTypeName() throws IllegalAccessException {
        assertThat(SerializerDefs.getTypeName(SerializerDefs.TYPE_UNKNOWN))
                .contains("unknown");

        for (Field field : SerializerDefs.class.getFields()) {
            if (field.getName().startsWith("TYPE_")) {
                final byte type = field.getByte(null);
                if (type != SerializerDefs.TYPE_UNKNOWN) {
                    assertThat(SerializerDefs.getTypeName(type))
                            .as("Type name is unknown: " + field.getName())
                            .doesNotContain("unknown");
                }
            }
        }
    }
}
