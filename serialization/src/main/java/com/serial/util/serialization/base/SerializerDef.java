package com.serial.util.serialization.base;

import org.jetbrains.annotations.NotNull;

/**
 * Defines the location of the serializer. {@link #type()} defines the class the serializer instance is in, and the
 * {@link #fieldName()} defines the name of the field holding the static instance.
 */
public @interface SerializerDef {
    String DEFAULT_SERIALIZER_NAME = "SERIALIZER";

    /**
     * @return the serializer class to look for the serializer instance in; only valid if the return type is deemed
     * serializable.
     */
    @NotNull Class<?> type();

    /**
     * @return the name of the serializer instance field; default "SERIALIZER". Only valid if the return type is deemed
     * serializable.
     */
    @NotNull String fieldName() default DEFAULT_SERIALIZER_NAME;
}
