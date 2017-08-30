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

import com.twitter.serial.stream.SerializerDefs;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.stream.SerializerOutput;
import com.twitter.serial.stream.bytebuffer.ByteBufferSerializerInput;
import com.twitter.serial.serializer.ObjectSerializer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Some methods here are helpful for debugging.
 * Such as: {@link #dumpSerializedData}, {@link #validateSerializedData}
 */
public class SerializationUtils {
    private static final byte MAX_LEN_TO_DUMP = 32;

    @Nullable
    private static volatile Pools.SynchronizedPool<byte[]> sBufferPool;

    public static void initializePool(@NotNull Pools.SynchronizedPool<byte[]> pool) {
        sBufferPool = pool;
    }

    @Contract("_, null -> true")
    public static boolean writeNullIndicator(@NotNull SerializerOutput output, @Nullable Object object)
            throws IOException {
        if (object == null) {
            if (output.isPeekSupported()) {
                output.writeNull();
            } else {
                output.writeByte(ObjectSerializer.NULL_OBJECT);
            }
            return true;
        } else {
            if (!output.isPeekSupported()) {
                output.writeByte(ObjectSerializer.NOT_NULL_OBJECT);
            }
            return false;
        }
    }

    public static boolean readNullIndicator(@NotNull SerializerInput input) throws IOException {
        if (input.isPeekSupported()) {
            final boolean nullObject = input.peekType() == SerializerDefs.TYPE_NULL;
            if (nullObject) {
                input.readNull();
            }
            return nullObject;
        } else {
            final byte nullIndicator = input.readByte();
            if (nullIndicator == ObjectSerializer.NULL_OBJECT) {
                return true;
            } else if (nullIndicator == ObjectSerializer.NOT_NULL_OBJECT) {
                return false;
            } else {
                throw new SerializationException("Invalid null indicator found: " + nullIndicator);
            }
        }
    }

    /**
     * Checks whether the given array contains a well-formed stream of serialized data that can be
     * read using {@link ByteBufferSerializerInput}.
     */
    public static void validateSerializedData(@NotNull byte[] bytes) throws IOException {
        validateSerializedData(new ByteBufferSerializerInput(bytes));
    }

    /**
     * Checks whether the given stream contains well-formed serialized data.
     */
    public static void validateSerializedData(@NotNull SerializerInput input) throws IOException {
        readStream(input, false);
    }

    /**
     * Skip over object during deserialization
     */
    public static void skipObject(@NotNull SerializerInput input) throws IOException {
        readStream(input, true);
    }

    /**
     * Deserialize a stream of serialized data as read using {@link ByteBufferSerializerInput} into a
     * displayable string. In the case of error, it will return a partial result.
     */
    @NotNull
    public static String dumpSerializedData(@NotNull byte[] bytes) {
        return dumpSerializedData(bytes, -1, true);
    }

    /**
     * Deserialize a stream of serialized data as read using {@link ByteBufferSerializerInput} into a
     * displayable string. In the case of error, it will return a partial result.
     *
     * @param position an optional position in the input that should be flagged with a marker in the output.
     * @param includeValues whether the values should be included in the output (eg. for privacy concerns).
     */
    @NotNull
    public static String dumpSerializedData(@NotNull byte[] bytes, int position, boolean includeValues) {
        return dumpSerializedData(new ByteBufferSerializerInput(bytes), position, includeValues);
    }

    /**
     * Deserialize a stream of serialized data into a displayable string. In the case of error, it will
     * return a partial result.
     *
     * @param position an optional position in the input that should be flagged with a marker in the output.
     * @param includeValues whether the values should be included in the output (eg. for privacy concerns).
     */
    @NotNull
    public static String dumpSerializedData(@NotNull SerializerInput input, int position, boolean includeValues) {
        final StringBuilder builder = new StringBuilder().append('{').append(InternalSerialUtils.lineSeparator());
        try {
            int objectNesting = 0;
            String indentation = "    ";
            boolean addPositionMarker = position >= 0;
            byte type;
            while ((type = input.peekType()) != SerializerDefs.TYPE_EOF) {
                if (type == SerializerDefs.TYPE_END_OBJECT) {
                    --objectNesting;
                    if (objectNesting < 0) {
                        throw new SerializationException("Object end with no matching object start.");
                    }
                    indentation = InternalSerialUtils.repeat("    ", objectNesting + 1);
                    input.readObjectEnd();
                    builder.append(indentation).append('}');
                } else {
                    builder.append(indentation);
                    switch (type) {
                        case SerializerDefs.TYPE_BYTE: {
                            final byte b = input.readByte();
                            if (includeValues) {
                                builder.append("Byte: ").append(b);
                            } else {
                                builder.append("Byte");
                            }
                            break;
                        }
                        case SerializerDefs.TYPE_INT: {
                            final int i = input.readInt();
                            if (includeValues) {
                                builder.append("Integer: ").append(i);
                            } else {
                                builder.append("Integer");
                            }
                            break;
                        }
                        case SerializerDefs.TYPE_LONG: {
                            final long l = input.readLong();
                            if (includeValues) {
                                builder.append("Long: ").append(l);
                            } else {
                                builder.append("Long");
                            }
                            break;
                        }
                        case SerializerDefs.TYPE_FLOAT: {
                            final float f = input.readFloat();
                            if (includeValues) {
                                builder.append("Float: ").append(f);
                            } else {
                                builder.append("Float");
                            }
                            break;
                        }
                        case SerializerDefs.TYPE_DOUBLE: {
                            final double d = input.readDouble();
                            if (includeValues) {
                                builder.append("Double: ").append(d);
                            } else {
                                builder.append("Double");
                            }
                            break;
                        }
                        case SerializerDefs.TYPE_BOOLEAN: {
                            final boolean b = input.readBoolean();
                            if (includeValues) {
                                builder.append("Boolean: ").append(b);
                            } else {
                                builder.append("Boolean");
                            }
                            break;
                        }
                        case SerializerDefs.TYPE_NULL: {
                            input.readNull();
                            builder.append("null");
                            break;
                        }
                        case SerializerDefs.TYPE_STRING_ASCII:
                        case SerializerDefs.TYPE_STRING_UTF8: {
                            final String string = input.readNotNullString();
                            if (includeValues) {
                                builder.append("String: \"")
                                        .append(string.replace(InternalSerialUtils.lineSeparator(), "\\n")).append('"');
                            } else {
                                builder.append("String (").append(string.length()).append(')');
                            }
                            break;
                        }
                        case SerializerDefs.TYPE_BYTE_ARRAY: {
                            final byte[] buffer = input.readNotNullByteArray();
                            if (includeValues) {
                                final int writeLen = buffer.length > MAX_LEN_TO_DUMP ? MAX_LEN_TO_DUMP : buffer.length;
                                builder.append("byte[]: \"")
                                        .append(InternalSerialUtils.toHex(buffer, 0, writeLen));
                                final int diff = buffer.length - writeLen;
                                if (diff > 0) {
                                    builder.append("... ").append(diff).append(" more bytes");
                                }
                                builder.append('"');
                            } else {
                                builder.append("byte[] (").append(buffer.length).append(')');
                            }
                            break;
                        }
                        case SerializerDefs.TYPE_START_OBJECT: {
                            final int version = input.readObjectStart();
                            builder.append("Object: Unknown type, v").append(version).append(" {");
                            ++objectNesting;
                            indentation = InternalSerialUtils.repeat("    ", objectNesting + 1);
                            break;
                        }
                        case SerializerDefs.TYPE_START_OBJECT_DEBUG: {
                            final DebugClassDescriptor objectInfo = input.readDebugObjectStart();
                            builder.append("Object: ").append(objectInfo.className)
                                    .append(", v").append(objectInfo.version).append(" {");
                            ++objectNesting;
                            indentation = InternalSerialUtils.repeat("    ", objectNesting + 1);
                            break;
                        }
                        default: {
                            throw new SerializationException("Unknown type: " + SerializerDefs.getTypeName(type) + '.');
                        }
                    }
                }
                if (addPositionMarker && position < input.getPosition()) {
                    builder.append(" <<<");
                    addPositionMarker = false;
                }
                builder.append(InternalSerialUtils.lineSeparator());
            }
            if (objectNesting > 0) {
                throw new SerializationException("Object start with no matching object end.");
            }
        } catch (IOException e) {
            return builder.append("ERROR: ").append(e).toString();
        }
        return builder.append('}').toString();
    }

    private static void readStream(@NotNull SerializerInput input, boolean singleObject) throws IOException {
        int objectNesting = 0;
        byte type;
        if (singleObject) {
            if (readNullIndicator(input)) {
                return;
            }
            type = input.peekType();
            if (type != SerializerDefs.TYPE_START_OBJECT && type != SerializerDefs.TYPE_START_OBJECT_DEBUG) {
                throw new SerializationException(
                        "Method skipObject can only be used to skip Objects in deserialization," +
                                " expected start object header but found " + SerializerDefs.getTypeName(type));
            }
        }
        while ((type = input.peekType()) != SerializerDefs.TYPE_EOF) {
            switch (type) {
                case SerializerDefs.TYPE_BYTE: {
                    input.readByte();
                    break;
                }
                case SerializerDefs.TYPE_INT: {
                    input.readInt();
                    break;
                }
                case SerializerDefs.TYPE_LONG: {
                    input.readLong();
                    break;
                }
                case SerializerDefs.TYPE_FLOAT: {
                    input.readFloat();
                    break;
                }
                case SerializerDefs.TYPE_DOUBLE: {
                    input.readDouble();
                    break;
                }
                case SerializerDefs.TYPE_BOOLEAN: {
                    input.readBoolean();
                    break;
                }
                case SerializerDefs.TYPE_NULL: {
                    input.readNull();
                    break;
                }
                case SerializerDefs.TYPE_STRING_ASCII:
                case SerializerDefs.TYPE_STRING_UTF8: {
                    input.readString();
                    break;
                }
                case SerializerDefs.TYPE_BYTE_ARRAY: {
                    input.readByteArray();
                    break;
                }
                case SerializerDefs.TYPE_START_OBJECT:
                case SerializerDefs.TYPE_START_OBJECT_DEBUG: {
                    input.readObjectStart();
                    ++objectNesting;
                    break;
                }
                case SerializerDefs.TYPE_END_OBJECT: {
                    --objectNesting;
                    input.readObjectEnd();
                    if (singleObject && objectNesting == 0) {
                        return;
                    }
                    if (objectNesting < 0) {
                        throw new SerializationException("Object end with no matching object start.");
                    }
                    break;
                }
                default: {
                    throw new SerializationException("Unknown type: " + SerializerDefs.getTypeName(type) + '.');
                }
            }
        }
        if (objectNesting > 0) {
            throw new SerializationException("Object start with no matching object end.");
        }
    }
}
