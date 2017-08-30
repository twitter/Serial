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

import com.twitter.serial.stream.bytebuffer.ByteBufferSerial;
import com.twitter.serial.SerializationTestUtils;
import com.twitter.serial.util.Pools;

import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class SerialTests {
    @Test
    public void testSerializeByteArray() throws IOException, ClassNotFoundException {
        final Serial serial = new ByteBufferSerial();
        final SerializationTestUtils.TestObject source = new SerializationTestUtils.TestObject("23", 59);
        final byte[] bytes = serial.toByteArray(source, SerializationTestUtils.TestObject.SERIALIZER);
        final SerializationTestUtils.TestObject dest =
                serial.fromByteArray(bytes, SerializationTestUtils.TestObject.SERIALIZER);
        assertThat(dest).isEqualTo(source);
    }

    @Test
    public void testPool() throws Exception {
        // uninitialized pool
        final Serial serialWithNoPool = new ByteBufferSerial();
        final SerializationTestUtils.TestObject testObject = new SerializationTestUtils.TestObject("t1", 1);
        final byte[] serializedObj1 = serialWithNoPool.toByteArray(testObject,
                SerializationTestUtils.TestObject.SERIALIZER);
        assertThat(serialWithNoPool.fromByteArray(serializedObj1, SerializationTestUtils.TestObject.SERIALIZER))
                .isEqualTo(testObject);

        // empty pool
        final Pools.SynchronizedPool<byte[]> emptyPool = new Pools.SynchronizedPool<>(1);
        final Serial serialWithEmptyPool = new ByteBufferSerial(emptyPool);
        final byte[] serializedObj2 = serialWithEmptyPool.toByteArray(testObject,
                SerializationTestUtils.TestObject.SERIALIZER);
        assertThat(serialWithEmptyPool.fromByteArray(serializedObj2, SerializationTestUtils.TestObject.SERIALIZER))
                .isEqualTo(testObject);

        // available
        final Pools.SynchronizedPool<byte[]> singletonPool = new Pools.SynchronizedPool<>(1);
        emptyPool.release(new byte[1024]);
        final Serial serialWithOneEntry = new ByteBufferSerial(singletonPool);
        final byte[] serializedObj3 = serialWithOneEntry.toByteArray(
                testObject, SerializationTestUtils.TestObject.SERIALIZER);
        assertThat(serialWithOneEntry.fromByteArray(serializedObj3, SerializationTestUtils.TestObject.SERIALIZER))
                .isEqualTo(testObject);
    }
}
