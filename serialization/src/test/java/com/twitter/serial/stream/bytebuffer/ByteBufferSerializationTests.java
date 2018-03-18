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

package com.twitter.serial.stream.bytebuffer;

import com.twitter.serial.serializer.ObjectSerializer;
import com.twitter.serial.serializer.SerializationContext;
import com.twitter.serial.serializer.Serializer;
import com.twitter.serial.SerializationTestUtils;
import com.twitter.serial.util.OptionalFieldException;
import com.twitter.serial.stream.SerializerDefs;
import com.twitter.serial.util.SerializationException;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.stream.SerializerOutput;

import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ByteBufferSerializationTests {

    @NotNull
    private final SerializationContext mContext = SerializationContext.ALWAYS_RELEASE;

    @Test
    public void testSimpleSerialization() throws Exception {
        final byte[] byteArray = new byte[1024];
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput(byteArray);
        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(byteArray);

        final byte b = 2;
        output.writeByte(b);
        assertThat(output.getPosition()).isEqualTo(2);
        assertThat(input.readByte()).isEqualTo(b);

        output.writeBoolean(true);
        assertThat(output.getPosition()).isEqualTo(3);
        assertThat(input.readBoolean()).isTrue();
        output.writeBoolean(false);
        assertThat(output.getPosition()).isEqualTo(4);
        assertThat(input.readBoolean()).isFalse();

        final int ib = 12;
        output.writeInt(ib);
        assertThat(output.getPosition()).isEqualTo(6);
        assertThat(input.readInt()).isEqualTo(ib);
        final int ibn = -12;
        output.writeInt(ibn);
        assertThat(output.getPosition()).isEqualTo(11);
        assertThat(input.readInt()).isEqualTo(ibn);
        final int is = 1234;
        output.writeInt(is);
        assertThat(output.getPosition()).isEqualTo(14);
        assertThat(input.readInt()).isEqualTo(is);
        final int ii = 12345678;
        output.writeInt(ii);
        assertThat(output.getPosition()).isEqualTo(19);
        assertThat(input.readInt()).isEqualTo(ii);

        final long li = 34567L;
        output.writeLong(li);
        assertThat(output.getPosition()).isEqualTo(22);
        assertThat(input.readLong()).isEqualTo(li);
        final long ll = 345678901234L;
        output.writeLong(ll);
        assertThat(output.getPosition()).isEqualTo(31);
        assertThat(input.readLong()).isEqualTo(ll);

        final float f = 4.5678F;
        output.writeFloat(f);
        assertThat(output.getPosition()).isEqualTo(36);
        assertThat(input.readFloat()).isEqualTo(f);

        final double d = 23.45;
        output.writeDouble(d);
        assertThat(output.getPosition()).isEqualTo(45);
        assertThat(input.readDouble()).isEqualTo(d);

        final byte[] buffer = { 1, 2, 3, 4, 5 };
        output.writeByteArray(buffer);
        assertThat(output.getPosition()).isEqualTo(52);
        assertThat(input.readByteArray()).isEqualTo(buffer);

        output.writeByte(b)
                .writeInt(ib)
                .writeInt(ibn)
                .writeInt(is)
                .writeInt(ii)
                .writeLong(li)
                .writeLong(ll)
                .writeFloat(f)
                .writeDouble(d)
                .writeByteArray(buffer);
        assertThat(input.readByte()).isEqualTo(b);
        assertThat(input.readInt()).isEqualTo(ib);
        assertThat(input.readInt()).isEqualTo(ibn);
        assertThat(input.readInt()).isEqualTo(is);
        assertThat(input.readInt()).isEqualTo(ii);
        assertThat(input.readLong()).isEqualTo(li);
        assertThat(input.readLong()).isEqualTo(ll);
        assertThat(input.readFloat()).isEqualTo(f);
        assertThat(input.readDouble()).isEqualTo(d);
        assertThat(input.readByteArray()).isEqualTo(buffer);
    }

    @Test
    public void testResizeBuffer() throws Exception {
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput(new byte[4]);

        assertThat(output.getBufferCapacity()).isEqualTo(4);
        output.writeInt(123456789);
        assertThat(output.getBufferCapacity()).isEqualTo(8);
        output.writeInt(234567890);
        assertThat(output.getBufferCapacity()).isEqualTo(16);
        output.writeLong(1234567890123L);
        assertThat(output.getBufferCapacity()).isEqualTo(32);
        output.writeFloat(123.879F);
        assertThat(output.getBufferCapacity()).isEqualTo(32);
        output.writeInt(123);
        assertThat(output.getBufferCapacity()).isEqualTo(32);

        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(output.getSerializedData());

        assertThat(input.readInt()).isEqualTo(123456789);
        assertThat(input.readInt()).isEqualTo(234567890);
        assertThat(input.readLong()).isEqualTo(1234567890123L);
        assertThat(input.readFloat()).isEqualTo(123.879F);
        assertThat(input.readInt()).isEqualTo(123);

        assertThat(output.getSerializedData()).hasSize(26);
    }

    @Test
    public void testSerializeString() throws Exception {
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput();
        final String testString1 = "this is a test";
        final String testString2 = "here are some special characters: áéíóüàèìòùäëïöü";
        final String testString3 = "この世の全てを手に入れた男、海賊王ゴールド・ロジャー。";
        final String testString4 = "And some emojis: \uD83D\uDE01\uD83D\uDE02\uD83D\uDE05\uD83D\uDE12";
        output.writeString(testString1);
        assertThat(output.getPosition()).isEqualTo(16);
        output.writeString(testString2);
        assertThat(output.getPosition()).isEqualTo(82);
        output.writeString(testString3);
        assertThat(output.getPosition()).isEqualTo(165);
        output.writeString(testString4);
        assertThat(output.getPosition()).isEqualTo(200);
        output.writeString("");
        assertThat(output.getPosition()).isEqualTo(201);

        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(output.getSerializedData());
        assertThat(input.peekType()).isEqualTo(SerializerDefs.TYPE_STRING_ASCII);
        assertThat(input.readString()).isEqualTo(testString1);
        assertThat(input.peekType()).isEqualTo(SerializerDefs.TYPE_STRING_UTF8);
        assertThat(input.readString()).isEqualTo(testString2);
        assertThat(input.peekType()).isEqualTo(SerializerDefs.TYPE_STRING_UTF8);
        assertThat(input.readString()).isEqualTo(testString3);
        assertThat(input.peekType()).isEqualTo(SerializerDefs.TYPE_STRING_UTF8);
        assertThat(input.readString()).isEqualTo(testString4);
        assertThat(input.readString()).isEmpty();

        final ByteBufferSerializerOutput longStringOutput = new ByteBufferSerializerOutput(new byte[32]);
        final String longString = "This is a long string that we are testing for serialization. The byte array will " +
                "resize so that the long string will still be serialized correctly.";
        longStringOutput.writeString(longString);
        assertThat(longStringOutput.getPosition()).isEqualTo(149);
        final ByteBufferSerializerInput longStringInput =
                new ByteBufferSerializerInput(longStringOutput.getSerializedData());
        assertThat(longStringInput.peekType()).isEqualTo(SerializerDefs.TYPE_STRING_ASCII);
        assertThat(longStringInput.readString()).isEqualTo(longString);
    }

    @Test(expected = SerializationException.class)
    public void testHeaderMismatch() throws Exception {
        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(
                new ByteBufferSerializerOutput().writeLong(34567L).getSerializedData());
        input.readString();
    }

    @Test
    public void testNull() throws Exception {
        final byte[] bytes = new ByteBufferSerializerOutput()
                .writeBoolean(true)
                .writeNull()
                .writeString("this is a test")
                .getSerializedData();

        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(bytes);
        assertThat(input.readBoolean()).isTrue();
        input.readNull();
        assertThat(input.readString()).isEqualTo("this is a test");
    }

    @Test
    public void testZeroValues() throws Exception {
        final byte[] bytes = new ByteBufferSerializerOutput()
                .writeInt(0)
                .writeInt(123)
                .writeFloat(0F)
                .writeFloat(23.45F)
                .writeString("")
                .getSerializedData();

        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(bytes);
        assertThat(bytes).hasSize(10);
        assertThat(input.readInt())
                .isZero();
        assertThat(input.readInt()).isEqualTo(123);
        assertThat(input.readFloat()).isEqualTo(0F);
        assertThat(input.readFloat()).isEqualTo(23.45F);
        assertThat(input.readString()).isEmpty();
    }

    @Test
    public void testPeekType() throws Exception {
        final byte[] bytes = new ByteBufferSerializerOutput()
                .writeBoolean(false)
                .writeNull()
                .writeString("")
                .writeInt(1)
                .writeInt(0)
                .getSerializedData();

        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(bytes);
        assertThat(input.peekType()).isEqualTo(SerializerDefs.TYPE_BOOLEAN);
        assertThat(input.readBoolean()).isFalse();
        assertThat(input.peekType()).isEqualTo(SerializerDefs.TYPE_NULL);
        input.readNull();
        assertThat(input.peekType()).isEqualTo(SerializerDefs.TYPE_STRING_ASCII);
        assertThat(input.readString())
                .isEmpty();
        assertThat(input.peekType()).isEqualTo(SerializerDefs.TYPE_INT);
        assertThat(input.readInt()).isEqualTo(1);
        assertThat(input.peekType()).isEqualTo(SerializerDefs.TYPE_INT);
        assertThat(input.readInt())
                .isZero();
    }

    @Test
    public void testPeekTypeAtPosition() throws Exception {
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput()
                .writeBoolean(false)
                .writeNull()
                .writeString("")
                .writeInt(1)
                .writeInt(0);

        assertThat(output.peekTypeAtPosition(0))
                .isEqualTo(SerializerDefs.TYPE_BOOLEAN);
        assertThat(output.peekTypeAtPosition(1))
                .isEqualTo(SerializerDefs.TYPE_NULL);
        assertThat(output.peekTypeAtPosition(2))
                .isEqualTo(SerializerDefs.TYPE_STRING_ASCII);
        assertThat(output.peekTypeAtPosition(3))
                .isEqualTo(SerializerDefs.TYPE_INT);
        assertThat(output.peekTypeAtPosition(5))
                .isEqualTo(SerializerDefs.TYPE_INT);
        assertThat(output.peekTypeAtPosition(6))
                .isEqualTo(SerializerDefs.TYPE_EOF);
    }

    @Test
    public void testTypeMismatchMessage() {
        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(
                new ByteBufferSerializerOutput().writeNull().getSerializedData());
        try {
            input.readString();
        } catch (IOException e) {
            assertThat(e).hasMessage("Expected value of type string but found null.");
        }
    }

    @Test
    public void testVersionNumbers() throws Exception {
        final SerializationTestUtils.TestObject testObject = new SerializationTestUtils.TestObject("t1", 1);
        final byte[] bytes = new ByteBufferSerializerOutput()
                .writeObject(mContext, testObject, SerializationTestUtils.TestObject.V1_SERIALIZER)
                .writeObject(mContext, testObject, SerializationTestUtils.TestObject.SERIALIZER)
                .getSerializedData();

        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(bytes);
        assertThat(
                (Object) SerializationTestUtils.TestObject.V1_SERIALIZER.deserialize(mContext, input))
                .isEqualToComparingFieldByField(new SerializationTestUtils.TestObject("t1", 2));

        assertThat(
                (Object) SerializationTestUtils.TestObject.V1_SERIALIZER.deserialize(mContext, input))
                .isEqualToComparingFieldByField(testObject);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidVersion() throws Exception {
        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(
                new ByteBufferSerializerOutput().writeObjectStart(-10).getSerializedData());
        input.readObjectStart();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVersionSerializer() throws Exception {
        new SerializationTestUtils.TestObject.TestObjectSerializer(-10);
    }

    @Test(expected = EOFException.class)
    public void testEOFException() throws Exception {
        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(
                new ByteBufferSerializerOutput().writeObjectStart(0).getSerializedData());
        TestObjectV2.SERIALIZER.deserialize(mContext, input);
    }

    private static class TestObjectV2 extends SerializationTestUtils.TestObject {
        public static final Serializer<TestObjectV2> SERIALIZER = new TestObjectV2Serializer();

        public final int val2;

        TestObjectV2(@NotNull String name, int val, int val2) {
            super(name, val);
            this.val2 = val2;
        }

        private static class TestObjectV2Serializer extends ObjectSerializer<TestObjectV2> {
            @Override
            protected void serializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerOutput output, @NotNull TestObjectV2 object)
                    throws IOException {
                SerializationTestUtils.TestObject.SERIALIZER.serialize(context, output, object);
                output.writeInt(object.val2);
            }

            @NotNull
            @Override
            protected TestObjectV2 deserializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerInput input, int versionNumber)
                    throws IOException, ClassNotFoundException {
                final SerializationTestUtils.TestObject t =
                        SerializationTestUtils.TestObject.SERIALIZER.deserialize(context, input);
                return new TestObjectV2(t.name, t.val, input.readInt());
            }
        }
    }
}
