package com.serial.util.serialization.base;

import com.serial.util.SerializationTestUtils;
import com.serial.util.internal.Pools;

import org.assertj.core.api.Java6Assertions;
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
        Java6Assertions.assertThat(serialWithNoPool.fromByteArray(serializedObj1, SerializationTestUtils.TestObject.SERIALIZER))
                .isEqualTo(testObject);

        // empty pool
        final Pools.SynchronizedPool<byte[]> emptyPool = new Pools.SynchronizedPool<>(1);
        final Serial serialWithEmptyPool = new ByteBufferSerial(emptyPool);
        final byte[] serializedObj2 = serialWithEmptyPool.toByteArray(testObject,
                SerializationTestUtils.TestObject.SERIALIZER);
        Java6Assertions.assertThat(serialWithEmptyPool.fromByteArray(serializedObj2, SerializationTestUtils.TestObject.SERIALIZER))
                .isEqualTo(testObject);

        // available
        final Pools.SynchronizedPool<byte[]> singletonPool = new Pools.SynchronizedPool<>(1);
        emptyPool.release(new byte[1024]);
        final Serial serialWithOneEntry = new ByteBufferSerial(singletonPool);
        final byte[] serializedObj3 = serialWithOneEntry.toByteArray(
                testObject, SerializationTestUtils.TestObject.SERIALIZER);
        Java6Assertions.assertThat(serialWithOneEntry.fromByteArray(serializedObj3, SerializationTestUtils.TestObject.SERIALIZER))
                .isEqualTo(testObject);
    }
}
