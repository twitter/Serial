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

package com.twitter.serial;

import com.twitter.serial.serializer.SerializationContext;
import com.twitter.serial.stream.bytebuffer.ByteBufferSerializerInput;
import com.twitter.serial.stream.bytebuffer.ByteBufferSerializerOutput;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.stream.SerializerOutput;
import com.twitter.serial.serializer.ObjectSerializer;
import com.twitter.serial.serializer.Serializer;
import com.twitter.serial.serializer.ValueSerializer;
import com.twitter.serial.util.InternalSerialUtils;
import com.twitter.serial.util.SerializableUtils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class SerializationTestUtils {

    @NotNull
    private static final SerializationContext CONTEXT = SerializationContext.ALWAYS_RELEASE;

    /**
     * Returns the result of serializing and deserializing an object.
     */
    @Contract("!null -> !null")
    public static <T> T performRoundTripThroughSerialization(@Nullable T object) throws IOException,
            ClassNotFoundException {
        if (object != null && !(object instanceof Serializable)) {
            throw new IllegalArgumentException("Input object must be serializable");
        }
        //noinspection BlacklistedMethod
        return SerializableUtils.fromByteArray(SerializableUtils.toByteArray((Serializable) object));
    }

    @Contract("!null, _ -> !null")
    public static <T> T performRoundTripThroughSerialization(@Nullable T object,
            @NotNull Serializer<? super T> serializer) throws IOException, ClassNotFoundException {
        // serialize
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput();
        serializer.serialize(CONTEXT, output, object);

        // deserialize
        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(output.getSerializedData());
        return InternalSerialUtils.cast(serializer.deserialize(CONTEXT, input));
    }

    public static <T> void checkSerialization(@Nullable T object, @NotNull Serializer<? super T> serializer)
            throws IOException, ClassNotFoundException {
        final T deserializedObject = performRoundTripThroughSerialization(object, serializer);
        assertThat(deserializedObject).isEqualTo(object);
    }

    public static <T> void checkSerializationComparingFieldByField(@Nullable T object,
            @NotNull Serializer<T> serializer) throws IOException, ClassNotFoundException {
        checkSerializationComparingFieldByField(object, serializer, "");
    }

    public static <T> void checkSerializationComparingFieldByField(@Nullable T object,
            @NotNull Serializer<T> serializer, @NotNull String... fieldsToIgnore)
            throws IOException, ClassNotFoundException {
        final T deserializedObject = performRoundTripThroughSerialization(object, serializer);
        if (object == null) {
            assertThat(deserializedObject).isNull();
        } else {
            InternalSerialUtils.checkIsNotNull(deserializedObject);
            assertThat(deserializedObject.getClass()).isEqualTo(object.getClass());
            assertThat(deserializedObject).isEqualToIgnoringGivenFields(object, fieldsToIgnore);
        }
    }

    public static <T> void checkSerializationComparingFieldByFieldRecursively(@Nullable T object,
            @NotNull Serializer<T> serializer)
            throws IOException, ClassNotFoundException {
        final T deserializedObject = performRoundTripThroughSerialization(object, serializer);
        if (object == null) {
            assertThat(deserializedObject).isNull();
        } else {
            InternalSerialUtils.checkIsNotNull(deserializedObject);
            assertThat(deserializedObject.getClass()).isEqualTo(object.getClass());
            assertThat(deserializedObject).isEqualToComparingFieldByFieldRecursively(object);
        }
    }

    public static class TestObject extends BaseTestObject implements Comparable<TestObject> {
        public static final ObjectSerializer<TestObject> SERIALIZER = new TestObjectSerializer();
        public static final ObjectSerializer<TestObject> V1_SERIALIZER = new TestObjectSerializer(1);
        public static final ValueSerializer<TestObject> VALUE_SERIALIZER = new TestObjectValueSerializer();

        @NotNull
        public final String name;
        public int val;

        public TestObject(@NotNull String name, int val) {
            this.name = name;
            this.val = val;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            return o instanceof TestObject && val == ((TestObject) o).val && name.equals(((TestObject) o).name);
        }

        @Override
        public int hashCode() {
            return 31 * name.hashCode() + val;
        }

        @Override
        public int compareTo(@NotNull TestObject other) {
            return name.compareTo(other.name);
        }

        public static class TestObjectSerializer extends ObjectSerializer<TestObject> {
            public TestObjectSerializer() {
            }

            public TestObjectSerializer(int versionNumber) {
                super(versionNumber);
            }

            @Override
            protected void serializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerOutput output, @NotNull TestObject object)
                    throws IOException {
                output.writeNull()
                        .writeString(object.name)
                        .writeInt(object.val);
            }

            @NotNull
            @Override
            protected TestObject deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber)
                    throws IOException, ClassNotFoundException {
                input.readNull();
                final TestObject testObject = new TestObject(input.readNotNullString(),
                        input.readInt());
                if (versionNumber == 1) {
                    testObject.val++;
                }
                return testObject;
            }
        }

        public static class TestObjectValueSerializer extends ValueSerializer<TestObject> {
            @Override
            protected void serializeValue(@NotNull SerializationContext context,
                    @NotNull SerializerOutput output, @NotNull TestObject object)
                    throws IOException {
                output.writeString(object.name)
                        .writeInt(object.val);
            }

            @Override
            @NotNull
            protected TestObject deserializeValue(@NotNull SerializationContext context,
                    @NotNull SerializerInput input)
                    throws IOException, ClassNotFoundException {
                return new TestObject(input.readNotNullString(), input.readInt());
            }
        }
    }

    public enum TestType {
        DEFAULT("default"),
        FIRST("first"),
        SECOND("second");

        @NotNull final String mValue;

        TestType(@NotNull String value) {
            mValue = value;
        }
    }

    public static class TestObjectComparator implements Comparator<TestObject> {
        @Override
        public int compare(@NotNull TestObject lhs, @NotNull TestObject rhs) {
            return lhs.name.compareTo(rhs.name);
        }

        @Override
        public boolean equals(@NotNull Object o) {
            return o instanceof TestObjectComparator;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    public static class InvalidTestObjectComparator implements Comparator<TestObject> {
        final int mValue;

        public InvalidTestObjectComparator(int value) {
            mValue = value;
        }

        @Override
        public int compare(@NotNull TestObject lhs, @NotNull TestObject rhs) {
            return lhs.name.compareTo(rhs.name);
        }

        @Override
        public boolean equals(@NotNull Object o) {
            return o instanceof InvalidTestObjectComparator;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    public static class TestObject2 extends BaseTestObject {
        public static final Serializer<TestObject2> SERIALIZER = new ObjectSerializer<TestObject2>() {
            @Override
            protected void serializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerOutput output, @NotNull TestObject2 object) throws IOException {
                output.writeBoolean(object.isObj);
            }

            @NotNull
            @Override
            protected TestObject2 deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber)
                    throws IOException, ClassNotFoundException {
                return new TestObject2(input.readBoolean());
            }
        };

        public final boolean isObj;

        public TestObject2(boolean isObj) {
            this.isObj = isObj;
        }
    }

    public abstract static class BaseTestObject {
    }
}
