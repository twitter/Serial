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

import com.twitter.serial.SerializationTestUtils;
import com.twitter.serial.stream.Serial;
import com.twitter.serial.stream.SerializerDefs;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.stream.SerializerOutput;
import com.twitter.serial.stream.bytebuffer.ByteBufferSerial;
import com.twitter.serial.stream.bytebuffer.ByteBufferSerializerInput;
import com.twitter.serial.stream.bytebuffer.ByteBufferSerializerOutput;
import com.twitter.serial.stream.legacy.LegacySerializerInput;
import com.twitter.serial.stream.legacy.LegacySerializerOutput;
import com.twitter.serial.model.Place;
import com.twitter.serial.model.SampleDataProvider;
import com.twitter.serial.serializer.CoreSerializers;
import com.twitter.serial.serializer.Serializer;

import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class SerializationUtilsTests {
    private Serial mSerial;

    @Before
    public void setUp() {
        mSerial = new ByteBufferSerial();
    }

    @Test
    public void testSerializeByteArray() throws IOException, ClassNotFoundException {
        final SerializationTestUtils.TestObject source = new SerializationTestUtils.TestObject("23", 59);
        final byte[] bytes = mSerial.toByteArray(source, SerializationTestUtils.TestObject.SERIALIZER);
        final SerializationTestUtils.TestObject dest =
                mSerial.fromByteArray(bytes, SerializationTestUtils.TestObject.SERIALIZER);
        assertThat(dest).isEqualTo(source);
    }

    @Test
    public void testValidateSerializedData() throws IOException {
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput();
        output.writeByte((byte) 2)
                .writeInt(0)
                .writeInt(3)
                .writeInt(444)
                .writeInt(555555555)
                .writeLong(0L)
                .writeLong(6L)
                .writeLong(777L)
                .writeLong(888888888L)
                .writeLong(9999999999999L)
                .writeFloat(0.0f)
                .writeFloat(5.3f)
                .writeDouble(0.0d)
                .writeDouble(6.5d)
                .writeBoolean(true)
                .writeBoolean(false)
                .writeNull()
                .writeString("")
                .writeString("com/twitter/test")
                .writeObjectStart(5)
                .writeLong(99L)
                .writeFloat(1.0f)
                .writeObjectEnd();
        SerializationUtils.validateSerializedData(output.getSerializedData());
    }

    @Test(expected = SerializationException.class)
    public void testValidateSerializedData_UnknownType() throws IOException {
        SerializationUtils.validateSerializedData(new byte[]{(byte) 0xFF});
    }

    @Test(expected = SerializationException.class)
    public void testValidateSerializedData_UnmatchedObjectStart() throws IOException {
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput();
        output.writeObjectStart(0)
                .writeObjectStart(5)
                .writeObjectEnd()
                .writeObjectStart(3)
                .writeLong(99L)
                .writeObjectEnd();
        SerializationUtils.validateSerializedData(output.getSerializedData());
    }

    @Test(expected = SerializationException.class)
    public void testValidateSerializedData_UnmatchedObjectEnd() throws IOException {
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput();
        output.writeObjectStart(5)
                .writeObjectEnd()
                .writeObjectStart(3)
                .writeLong(99L)
                .writeObjectEnd()
                .writeObjectEnd()
                .writeObjectStart(0);
        SerializationUtils.validateSerializedData(output.getSerializedData());
    }

    @Test
    public void testPool() throws Exception {
        // uninitialized pool
        final SerializationTestUtils.TestObject testObject = new SerializationTestUtils.TestObject("t1", 1);
        final byte[] serializedObj1 = mSerial.toByteArray(
                testObject, SerializationTestUtils.TestObject.SERIALIZER);
        assertThat(mSerial.fromByteArray(
                serializedObj1, SerializationTestUtils.TestObject.SERIALIZER)).isEqualTo(testObject);

        // empty pool
        final Pools.SynchronizedPool<byte[]> arrayPool = new Pools.SynchronizedPool<>(1);
        SerializationUtils.initializePool(arrayPool);
        final byte[] serializedObj2 = mSerial.toByteArray(
                testObject, SerializationTestUtils.TestObject.SERIALIZER);
        assertThat(mSerial.fromByteArray(serializedObj2, SerializationTestUtils.TestObject.SERIALIZER))
                .isEqualTo(testObject);

        // available
        arrayPool.release(new byte[1024]);
        SerializationUtils.initializePool(arrayPool);
        final byte[] serializedObj3 = mSerial.toByteArray(
                testObject, SerializationTestUtils.TestObject.SERIALIZER);
        assertThat(mSerial.fromByteArray(serializedObj3, SerializationTestUtils.TestObject.SERIALIZER))
                .isEqualTo(testObject);
    }

    @Test
    public void testNullWithPeek() throws IOException {
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput();
        assertThat(SerializationUtils.writeNullIndicator(output, null))
                .isTrue();
        assertThat(SerializationUtils.writeNullIndicator(output, 5))
                .isFalse();
        output.writeInt(5);

        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(output.getSerializedData());
        assertThat(SerializationUtils.readNullIndicator(input))
                .isTrue();
        assertThat(SerializationUtils.readNullIndicator(input))
                .isFalse();
        assertThat(input.readInt())
                .isEqualTo(5);
        assertThat(input.peekType())
                .isEqualTo(SerializerDefs.TYPE_EOF);
    }

    @Test
    public void testNullWithoutPeek() throws IOException {
        final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        final ObjectOutput objectOutput = new ObjectOutputStream(byteOutputStream);
        final SerializerOutput output = new LegacySerializerOutput(objectOutput);
        assertThat(SerializationUtils.writeNullIndicator(output, null))
                .isTrue();
        assertThat(SerializationUtils.writeNullIndicator(output, 5))
                .isFalse();
        output.writeInt(5);
        objectOutput.close();

        final SerializerInput input = new LegacySerializerInput(
                new ObjectInputStream(new ByteArrayInputStream(byteOutputStream.toByteArray())));
        assertThat(SerializationUtils.readNullIndicator(input))
                .isTrue();
        assertThat(SerializationUtils.readNullIndicator(input))
                .isFalse();
        assertThat(input.readInt())
                .isEqualTo(5);
    }

    @Test
    public void testDumpSerializedData() throws IOException {
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput();
        output.writeByte((byte) 2)
                .writeInt(3)
                .writeInt(444)
                .writeLong(6L)
                .writeFloat(5.3f)
                .writeDouble(6.5d)
                .writeBoolean(true)
                .writeNull()
                .writeString("")
                .writeString("test with" + InternalSerialUtils.lineSeparator() + "a linebreak.");
        assertThat(SerializationUtils.dumpSerializedData(output.getSerializedData()))
                .isEqualTo("{\n" +
                        "    Byte: 2\n" +
                        "    Integer: 3\n" +
                        "    Integer: 444\n" +
                        "    Long: 6\n" +
                        "    Float: 5.3\n" +
                        "    Double: 6.5\n" +
                        "    Boolean: true\n" +
                        "    null\n" +
                        "    String: \"\"\n" +
                        "    String: \"test with\\na linebreak.\"\n" +
                        "}");
        assertThat(SerializationUtils.dumpSerializedData(output.getSerializedData(), -1, /* includeValues= */ false))
                .isEqualTo("{\n" +
                        "    Byte\n" +
                        "    Integer\n" +
                        "    Integer\n" +
                        "    Long\n" +
                        "    Float\n" +
                        "    Double\n" +
                        "    Boolean\n" +
                        "    null\n" +
                        "    String (0)\n" +
                        "    String (22)\n" +
                        "}");
        assertThat(SerializationUtils.dumpSerializedData(output.getSerializedData(), 10, /* includeValues= */ true))
                .isEqualTo("{\n" +
                        "    Byte: 2\n" +
                        "    Integer: 3\n" +
                        "    Integer: 444\n" +
                        "    Long: 6\n" +
                        "    Float: 5.3 <<<\n" +
                        "    Double: 6.5\n" +
                        "    Boolean: true\n" +
                        "    null\n" +
                        "    String: \"\"\n" +
                        "    String: \"test with\\na linebreak.\"\n" +
                        "}");
        assertThat(SerializationUtils.dumpSerializedData(output.getSerializedData(), 30, /* includeValues= */ false))
                .isEqualTo("{\n" +
                        "    Byte\n" +
                        "    Integer\n" +
                        "    Integer\n" +
                        "    Long\n" +
                        "    Float\n" +
                        "    Double\n" +
                        "    Boolean\n" +
                        "    null\n" +
                        "    String (0)\n" +
                        "    String (22) <<<\n" +
                        "}");
    }

    @Test
    public void testDumpSerializedData_Objects() throws IOException {
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput();
        output.writeObjectStart(0, "MyObject");
        output.writeByte((byte) 2)
                .writeInt(3)
                .writeObjectStart(1)
                .writeInt(444)
                .writeLong(6L)
                .writeFloat(5.3f)
                .writeObjectEnd()
                .writeDouble(6.5d)
                .writeBoolean(true)
                .writeObjectEnd()
                .writeNull();
        assertThat(SerializationUtils.dumpSerializedData(output.getSerializedData()))
                .isEqualTo("{\n" +
                        "    Object: MyObject, v0 {\n" +
                        "        Byte: 2\n" +
                        "        Integer: 3\n" +
                        "        Object: Unknown type, v1 {\n" +
                        "            Integer: 444\n" +
                        "            Long: 6\n" +
                        "            Float: 5.3\n" +
                        "        }\n" +
                        "        Double: 6.5\n" +
                        "        Boolean: true\n" +
                        "    }\n" +
                        "    null\n" +
                        "}");
    }

    @Test
    public void testDumpSerializedData_UnmatchedObjectStart() throws IOException {
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput();
        output.writeObjectStart(0)
                .writeObjectStart(5)
                .writeObjectEnd()
                .writeObjectStart(3)
                .writeLong(99L)
                .writeObjectEnd();
        assertThat(SerializationUtils.dumpSerializedData(output.getSerializedData()))
                .isEqualTo("{\n" +
                        "    Object: Unknown type, v0 {\n" +
                        "        Object: Unknown type, v5 {\n" +
                        "        }\n" +
                        "        Object: Unknown type, v3 {\n" +
                        "            Long: 99\n" +
                        "        }\n" +
                        "ERROR: com.twitter.serial.util.SerializationException: " +
                        "Object start with no matching object end.");
    }

    @Test
    public void testSkipObjectInDeserialization() throws Exception {
        final ByteBufferSerializerOutput output = new ByteBufferSerializerOutput();
        output.writeBoolean(false)
                .writeObjectStart(0)
                .writeInt(1)
                .writeString("test string")
                .writeObjectStart(0)
                .writeFloat(2F)
                .writeString("inner obj")
                .writeObjectEnd()
                .writeInt(3)
                .writeObjectEnd()
                .writeObjectStart(0)
                .writeObjectEnd()
                .writeNull()
                .writeLong(10L);
        final ByteBufferSerializerInput input = new ByteBufferSerializerInput(output.getSerializedData());
        assertThat(input.readBoolean()).isFalse();
        SerializationUtils.skipObject(input);
        assertThat(input.readObjectStart())
                .isZero();
        input.readObjectEnd();
        SerializationUtils.skipObject(input);
        assertThat(input.readLong()).isEqualTo(10L);
    }

    @Test
    public void testBaseClassSerializer() throws Exception {
        final Serializer<SerializationTestUtils.BaseTestObject> baseSerializer =
                CoreSerializers.getBaseClassSerializer(
                        SerializableClass.create(SerializationTestUtils.TestObject.class,
                                new SerializationTestUtils.TestObject.TestObjectSerializer()),
                        SerializableClass.create(SerializationTestUtils.TestObject2.class,
                                SerializationTestUtils.TestObject2.SERIALIZER)
                );
        final SerializationTestUtils.TestObject testObject = new SerializationTestUtils.TestObject("test name", 1);
        final SerializationTestUtils.TestObject2 testObject2 = new SerializationTestUtils.TestObject2(true);

        SerializationTestUtils.checkSerializationComparingFieldByField(testObject, baseSerializer);
        SerializationTestUtils.checkSerializationComparingFieldByField(testObject2, baseSerializer);
    }

    @Test
    public void testComplexClassSerialization() throws Exception {
        final Place place = SampleDataProvider.createSamplePlace();

        final byte[] bytes = mSerial.toByteArray(place, Place.SERIALIZER);
        final Place restoredPlace = mSerial.fromByteArray(bytes, Place.SERIALIZER);

        assertThat(restoredPlace).isEqualTo(place);
    }
}
