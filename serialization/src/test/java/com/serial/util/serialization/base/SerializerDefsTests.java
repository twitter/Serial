package com.serial.util.serialization.base;

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
