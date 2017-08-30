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
import com.twitter.serial.stream.bytebuffer.ByteBufferSerial;
import com.twitter.serial.stream.Serial;
import com.twitter.serial.util.SerializableClass;
import com.twitter.serial.util.SerializationException;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class CoreSerializersTests {
    private Serial mSerial;

    @Before
    public void setUp() {
        mSerial = new ByteBufferSerial();
    }

    @Test
    public void testBaseSerializers() throws Exception {
        SerializationTestUtils.checkSerialization(true, CoreSerializers.BOOLEAN);
        SerializationTestUtils.checkSerialization(false, CoreSerializers.BOOLEAN);
        SerializationTestUtils.checkSerialization(123, CoreSerializers.INTEGER);
        SerializationTestUtils.checkSerialization('c', CoreSerializers.CHARACTER);
        SerializationTestUtils.checkSerialization((short) 2, CoreSerializers.SHORT);
        SerializationTestUtils.checkSerialization(45678L, CoreSerializers.LONG);
        SerializationTestUtils.checkSerialization(12.34, CoreSerializers.DOUBLE);
        SerializationTestUtils.checkSerialization(5.678F, CoreSerializers.FLOAT);
        SerializationTestUtils.checkSerialization(
                new int[] { -50, 0, 1, 1337, Integer.MAX_VALUE, Integer.MIN_VALUE },
                CoreSerializers.INT_ARRAY);
        SerializationTestUtils.checkSerialization(
                new long[] { -50L, 0L, 1L, 1337L, Long.MAX_VALUE, Long.MIN_VALUE },
                CoreSerializers.LONG_ARRAY);
        SerializationTestUtils.checkSerialization(
                new float[] { -50.0f, 0.0f, Float.MAX_VALUE, Float.MIN_VALUE },
                CoreSerializers.FLOAT_ARRAY);
        SerializationTestUtils.checkSerialization(
                new double[] { -50.0d, 0.0d, Double.MAX_VALUE, Double.MIN_VALUE },
                CoreSerializers.DOUBLE_ARRAY);
        SerializationTestUtils.checkSerialization("hello", CoreSerializers.STRING);
        SerializationTestUtils.checkSerialization(new BigDecimal(0.23), CoreSerializers.BIG_DECIMAL);
        SerializationTestUtils.checkSerialization(new BigDecimal("1.1232190473829758495647358647354354123124325233"),
                CoreSerializers.BIG_DECIMAL);
        SerializationTestUtils.checkSerialization(new BigDecimal(102392914738294732.1323432432F),
                CoreSerializers.BIG_DECIMAL);
        SerializationTestUtils.checkSerialization(new BigDecimal("123456789.1234567890"),
                CoreSerializers.BIG_DECIMAL);
    }

    @Test
    public void testSimpleObjectSerializer() throws Exception {
        SerializationTestUtils.checkSerialization("hello", CoreSerializers.SIMPLE_OBJECT);
        SerializationTestUtils.checkSerialization(123, CoreSerializers.SIMPLE_OBJECT);
        SerializationTestUtils.checkSerialization(45678L, CoreSerializers.SIMPLE_OBJECT);
        SerializationTestUtils.checkSerialization(12.34, CoreSerializers.SIMPLE_OBJECT);
        SerializationTestUtils.checkSerialization(5.678F, CoreSerializers.SIMPLE_OBJECT);
        SerializationTestUtils.checkSerialization("hello", CoreSerializers.SIMPLE_OBJECT);
    }

    @Test
    public void testSerializeEnum() throws Exception {
        SerializationTestUtils.checkSerializationComparingFieldByField(SerializationTestUtils.TestType.DEFAULT,
                CoreSerializers.getEnumSerializer(SerializationTestUtils.TestType.class));
        SerializationTestUtils.checkSerializationComparingFieldByField(SerializationTestUtils.TestType.FIRST,
                CoreSerializers.getEnumSerializer(SerializationTestUtils.TestType.class));
        SerializationTestUtils.checkSerializationComparingFieldByField(SerializationTestUtils.TestType.SECOND,
                CoreSerializers.getEnumSerializer(SerializationTestUtils.TestType.class));
    }

    @Test
    public void testReadNullWithDefaultSerializer() throws Exception {
        final SerializationTestUtils.TestObject testObject = new SerializationTestUtils.TestObject("testName", 1);
        SerializationTestUtils.checkSerializationComparingFieldByField(
                testObject, SerializationTestUtils.TestObject.SERIALIZER);
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
    public void testBaseClassSerializerWithDummyClass() throws Exception {
        final Serializer<SerializationTestUtils.BaseTestObject> baseSerializer =
                CoreSerializers.getBaseClassSerializer(
                        SerializableClass.<SerializationTestUtils.BaseTestObject>getDummy(),
                        SerializableClass.create(SerializationTestUtils.TestObject2.class,
                                SerializationTestUtils.TestObject2.SERIALIZER)
                );

        final SerializationTestUtils.TestObject2 testObject2 = new SerializationTestUtils.TestObject2(true);
        SerializationTestUtils.checkSerializationComparingFieldByField(testObject2, baseSerializer);
    }

    @Test(expected = SerializationException.class)
    public void testBaseClassSerializerThrowsExceptionWhenNoSerializerForSubclass() throws Exception {
        final Serializer<SerializationTestUtils.BaseTestObject> baseSerializer =
                CoreSerializers.getBaseClassSerializer(
                        SerializableClass.<SerializationTestUtils.BaseTestObject>getDummy(),
                        SerializableClass.create(SerializationTestUtils.TestObject2.class,
                                SerializationTestUtils.TestObject2.SERIALIZER)
                );

        final SerializationTestUtils.TestObject testObject = new SerializationTestUtils.TestObject("test name", 1);
        final byte[] testObjectByteArray = mSerial.toByteArray(testObject, baseSerializer);
        assertThat(mSerial.fromByteArray(testObjectByteArray, baseSerializer)).isNull();
    }


    @Test
    public void testSerializableSerializer() throws IOException, ClassNotFoundException {
        final Serializer<Integer> integerSerializer = CoreSerializers.getSerializableSerializer();
        SerializationTestUtils.checkSerialization(null, integerSerializer);
        SerializationTestUtils.checkSerialization(5, integerSerializer);

        final Serializer<String> stringSerializer = CoreSerializers.getSerializableSerializer();
        SerializationTestUtils.checkSerialization(null, stringSerializer);
        SerializationTestUtils.checkSerialization("", stringSerializer);
        SerializationTestUtils.checkSerialization("com/twitter/test", stringSerializer);
    }
}
