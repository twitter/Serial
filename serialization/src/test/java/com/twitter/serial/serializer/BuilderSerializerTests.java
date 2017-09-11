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
import com.twitter.serial.object.ObjectBuilder;
import com.twitter.serial.stream.bytebuffer.ByteBufferSerializerInput;
import com.twitter.serial.stream.bytebuffer.ByteBufferSerializerOutput;
import com.twitter.serial.stream.SerializerOutput;
import com.twitter.serial.stream.SerializerInput;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class BuilderSerializerTests {
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        SerializationTestUtils.checkSerializationComparingFieldByField(
                new SerializableObject.Builder().setNumber(0).setString(null).build(), SerializableObject.SERIALIZER);
        SerializationTestUtils.checkSerializationComparingFieldByField(
                new SerializableObject.Builder().setNumber(42).setString("tst").build(), SerializableObject.SERIALIZER);
    }

    @Test
    public void testOptionalFields() throws Exception {
        SerializationTestUtils.checkSerializationComparingFieldByField(
                new SerializableObject.Builder().setNumber(42).build(), SerializableObject.SERIALIZER);
        SerializationTestUtils.checkSerializationComparingFieldByField(
                new SerializableObject.Builder().setString("tst").build(), SerializableObject.SERIALIZER);
        SerializationTestUtils.checkSerializationComparingFieldByField(new SerializableObject.Builder().build(),
                SerializableObject.SERIALIZER);
    }

    @Test
    public void testVersionNumbers() throws Exception {
        final Serializer<SerializableObject> v1Serializer = new SerializableObject.SerializableObjectSerializer(1);
        final SerializableObject object = new SerializableObject.Builder().setNumber(2).setString("e").buildObject();

        final byte[] bytes = new ByteBufferSerializerOutput()
                .writeObjectStart(1)
                .writeLong(object.number)
                .writeString(object.string)
                .writeObjectEnd()
                .getSerializedData();

        final SerializerInput input = new ByteBufferSerializerInput(bytes);
        final SerializationContext context = SerializationContext.ALWAYS_RELEASE;
        final SerializableObject deserializedObject = input.readObject(context, v1Serializer);
        assertThat(deserializedObject).isEqualToComparingFieldByField(object);
    }

    public static class SerializableObject {
        private static final BuilderSerializer<SerializableObject, Builder> SERIALIZER =
                new SerializableObjectSerializer();

        public final int number;
        @Nullable
        public final String string;

        SerializableObject(@NotNull Builder builder) {
            this.number = builder.mNumber;
            this.string = builder.mString;
        }

        public static class Builder extends ObjectBuilder<SerializableObject> {
            int mNumber;
            String mString;

            @NotNull
            public Builder setNumber(int number) {
                mNumber = number;
                return this;
            }

            @NotNull
            public Builder setString(@Nullable String string) {
                mString = string;
                return this;
            }

            @NotNull
            @Override
            protected SerializableObject buildObject() {
                return new SerializableObject(this);
            }
        }

        private static class SerializableObjectSerializer extends
                BuilderSerializer<SerializableObject, Builder> {
            protected SerializableObjectSerializer() {
            }

            protected SerializableObjectSerializer(int versionNumber) {
                super(versionNumber);
            }

            @Override
            protected void serializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerOutput output, @NotNull SerializableObject object)
                    throws IOException {
                output.writeInt(object.number)
                        .writeString(object.string);
            }

            @NotNull
            @Override
            protected Builder createBuilder() {
                return new Builder();
            }

            @Override
            protected void deserializeToBuilder(@NotNull SerializationContext context,
                    @NotNull SerializerInput input, @NotNull Builder builder, int versionNumber)
                    throws IOException, ClassNotFoundException {
                if (versionNumber < 1) {
                    builder.setNumber(input.readInt());
                } else {
                    builder.setNumber((int) input.readLong());
                }
                builder.setString(input.readString())
                                // Read an additional field, just to test optional fields.
                        .setString(input.readString());
            }
        }
    }
}
