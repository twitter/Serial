package com.serial.util.serialization.serializer;

import com.serial.util.SerializationTestUtils;
import com.serial.util.serialization.SerializationContext;
import com.serial.util.serialization.base.SerializerInput;
import com.serial.util.serialization.base.SerializerOutput;

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
