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

import com.twitter.serial.util.OptionalFieldException;
import com.twitter.serial.util.SerializationException;
import com.twitter.serial.stream.SerializerDefs;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.util.DebugClassDescriptor;
import com.twitter.serial.util.InternalSerialUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Custom deserialization for objects serialized by {@link ByteBufferSerializerOutput}
 */
public class ByteBufferSerializerInput extends SerializerInput {
    @NotNull private final ByteBuffer mByteBuffer;

    public ByteBufferSerializerInput(@NotNull byte[] byteArray) {
        mByteBuffer = ByteBuffer.wrap(byteArray);
    }

    @Override
    public boolean isPeekSupported() {
        return true;
    }

    @Override
    public int getPosition() {
        return mByteBuffer.position();
    }

    @Override
    public byte readByte() throws IOException {
        try {
            final byte subtype = readHeader(SerializerDefs.TYPE_BYTE);
            if (subtype == ByteBufferSerializerDefs.SUBTYPE_DEFAULT) {
                return 0;
            }
            return mByteBuffer.get();
        } catch (BufferUnderflowException ignore) {
            throw new EOFException();
        }
    }

    @Override
    public boolean readBoolean() throws IOException {
        return readHeader(SerializerDefs.TYPE_BOOLEAN) == ByteBufferSerializerDefs.SUBTYPE_DEFAULT;
    }

    @Override
    public int readInt() throws IOException {
        return readIntHeader(SerializerDefs.TYPE_INT);
    }

    @Override
    public long readLong() throws IOException {
        return readLongHeader(SerializerDefs.TYPE_LONG);
    }

    @Override
    public float readFloat() throws IOException {
        try {
            final byte subtype = readHeader(SerializerDefs.TYPE_FLOAT);
            if (subtype == ByteBufferSerializerDefs.SUBTYPE_DEFAULT) {
                return 0f;
            }
            return mByteBuffer.getFloat();
        } catch (BufferUnderflowException ignore) {
            throw new EOFException();
        }
    }

    @Override
    public double readDouble() throws IOException {
        try {
            final byte subtype = readHeader(SerializerDefs.TYPE_DOUBLE);
            if (subtype == ByteBufferSerializerDefs.SUBTYPE_DEFAULT) {
                return 0d;
            }
            return mByteBuffer.getDouble();
        } catch (BufferUnderflowException ignore) {
            throw new EOFException();
        }
    }

    @Override
    @Nullable
    public String readString() throws IOException {
        final byte type = peekType();
        if (type == SerializerDefs.TYPE_NULL) {
            readNull();
            return null;
        }
        if (type != SerializerDefs.TYPE_STRING_UTF8 && type != SerializerDefs.TYPE_STRING_ASCII) {
            reportUnexpectedHeader(SerializerDefs.TYPE_STRING_UTF8, type);
        }
        final int length = readIntHeader(type);
        if (length < 0) {
            throw new SerializationException("String length is negative: " + length + ".");
        } else if (length == 0) {
            return "";
        }
        return type == SerializerDefs.TYPE_STRING_UTF8 ? decodeUtf8String(length) : decodeAsciiString(length);
    }

    @Nullable
    @Override
    public byte[] readByteArray() throws IOException {
        final byte type = peekType();
        if (type == SerializerDefs.TYPE_NULL) {
            readNull();
            return null;
        }
        if (type != SerializerDefs.TYPE_BYTE_ARRAY) {
            reportUnexpectedHeader(SerializerDefs.TYPE_BYTE_ARRAY, type);
        }
        final int length = readIntHeader(type);
        if (length < 0) {
            throw new SerializationException("byte[] length is negative: " + length + ".");
        } else if (length == 0) {
            return new byte[0];
        }
        final byte[] data = new byte[length];
        mByteBuffer.get(data);
        return data;
    }

    @Override
    public int readObjectStart() throws IOException {
        final int versionNumber;
        if (peekType() == SerializerDefs.TYPE_START_OBJECT_DEBUG) {
            versionNumber = readIntHeader(SerializerDefs.TYPE_START_OBJECT_DEBUG);
            // Skip the class name.
            readString();
        } else {
            versionNumber = readIntHeader(SerializerDefs.TYPE_START_OBJECT);
        }
        if (versionNumber < 0) {
            throw new SerializationException("Invalid version number found (" + versionNumber + "). Valid versions" +
                    " must be greater than 0.");
        }
        return versionNumber;
    }

    @Override
    @NotNull
    public DebugClassDescriptor readDebugObjectStart() throws IOException {
        final int versionNumber = readIntHeader(SerializerDefs.TYPE_START_OBJECT_DEBUG);
        final String className = InternalSerialUtils.checkIsNotNull(readString());
        return new DebugClassDescriptor(versionNumber, className);
    }

    @Override
    public void readObjectEnd() throws IOException {
        readHeader(SerializerDefs.TYPE_END_OBJECT);
    }

    @Override
    public void readNull() throws IOException {
        readHeader(SerializerDefs.TYPE_NULL);
    }

    @Override
    public byte peekType() {
        if (mByteBuffer.remaining() == 0) {
            return SerializerDefs.TYPE_EOF;
        } else {
            final byte peekedHeader = mByteBuffer.get(mByteBuffer.position());
            return ByteBufferSerializerDefs.getHeaderType(peekedHeader);
        }
    }

    /**
     * Reads a headers and verifies its type.
     *
     * @return the header subtype.
     */
    private byte readHeader(byte expectedType) throws IOException {
        try {
            final byte header = mByteBuffer.get();
            final byte actualType = ByteBufferSerializerDefs.getHeaderType(header);
            if (actualType != expectedType) {
                mByteBuffer.position(mByteBuffer.position() - 1);
                return reportUnexpectedHeader(expectedType, actualType);
            }
            return ByteBufferSerializerDefs.getHeaderSubtype(header);
        } catch (BufferUnderflowException ignore) {
            throw new EOFException();
        }
    }

    private static byte reportUnexpectedHeader(byte expectedType, byte actualType)
            throws OptionalFieldException, SerializationException {
        if (actualType == SerializerDefs.TYPE_END_OBJECT) {
            throw new OptionalFieldException("Expected object field of type " +
                    SerializerDefs.getTypeName(expectedType) + "but found the end of the object.");
        }
        throw new SerializationException("Expected value of type " + SerializerDefs.getTypeName(expectedType) +
                " but found " + SerializerDefs.getTypeName(actualType) + ".");
    }

    /**
     * Reads a header followed by an int value that's configured by the header subtype.
     */
    private int readIntHeader(byte expectedType) throws IOException {
        final byte subtype = readHeader(expectedType);
        return readIntValue(subtype);
    }

    private int readIntValue(byte subtype) throws IOException {
        try {
            if (subtype == ByteBufferSerializerDefs.SUBTYPE_DEFAULT) {
                return 0;
            } else if (subtype == ByteBufferSerializerDefs.SUBTYPE_BYTE) {
                return mByteBuffer.get() & 0xFF;
            } else if (subtype == ByteBufferSerializerDefs.SUBTYPE_SHORT) {
                return mByteBuffer.getShort() & 0xFFFF;
            } else {
                return mByteBuffer.getInt();
            }
        } catch (BufferUnderflowException ignore) {
            throw new EOFException();
        }
    }

    /**
     * Reads a header followed by an long value that's configured by the header subtype.
     */
    private long readLongHeader(byte expectedType) throws IOException {
        try {
            final byte subtype = readHeader(expectedType);
            if (subtype == ByteBufferSerializerDefs.SUBTYPE_LONG) {
                return mByteBuffer.getLong();
            }
            return readIntValue(subtype) & 0xFFFFFFFFL;
        } catch (BufferUnderflowException ignore) {
            throw new EOFException();
        }
    }

    @NotNull
    private String decodeUtf8String(int length) throws IOException {
        try {
            final ByteBuffer buffer = mByteBuffer;
            final StringBuilder builder = new StringBuilder(length);
            for (int i = 0; i < length; ++i) {
                final int b1 = buffer.get();
                if ((b1 & 0x80) == 0) {
                    builder.append((char) b1);
                } else if ((b1 & 0xE0) == 0xC0) {
                    final int b2 = buffer.get();
                    builder.append((char) (((b1 << 6) ^ b2) ^ 0x0f80));
                } else if ((b1 & 0xF0) == 0xE0) {
                    final int b2 = buffer.get();
                    final int b3 = buffer.get();
                    builder.append((char) (((b1 << 12) ^ (b2 << 6) ^ b3) ^ 0x1f80));
                } else if ((b1 & 0xF8) == 0xF0) {
                    final int b2 = buffer.get();
                    final int b3 = buffer.get();
                    final int b4 = buffer.get();
                    final int code = ((b1 & 0x07) << 18) | ((b2 & 0x3f) << 12) | ((b3 & 0x3f) << 6) | (b4 & 0x3f);
                    builder.append(Surrogate.highSurrogate(code));
                    builder.append(Surrogate.lowSurrogate(code));
                    //noinspection AssignmentToForLoopParameter
                    ++i;
                } else {
                    throw new SerializationException("Serialized string is malformed.");
                }
            }
            return builder.toString();
        } catch (BufferUnderflowException ignore) {
            throw new EOFException();
        }
    }

    @NotNull
    private String decodeAsciiString(int length) throws IOException {
        if (mByteBuffer.remaining() < length) {
            throw new EOFException();
        }
        final int position = mByteBuffer.position();
        final int end = position + length;
        mByteBuffer.position(end);

        final byte[] bytes = mByteBuffer.array();
        final char[] chars = new char[length];
        for (int i = 0; i < length; ++i) {
            chars[i] = (char) bytes[position + i];
        }
        return new String(chars);
    }

    /**
     * These methods are copied from Character, since they are only available in API 19+.
     */
    private static class Surrogate {
        private static final int MIN_HIGH_SURROGATE = '\uD800';
        private static final int MIN_LOW_SURROGATE  = '\uDC00';
        private static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x010000;

        public static char highSurrogate(int codePoint) {
            return (char) ((codePoint >>> 10) + (MIN_HIGH_SURROGATE - (MIN_SUPPLEMENTARY_CODE_POINT >>> 10)));
        }

        public static char lowSurrogate(int codePoint) {
            return (char) ((codePoint & 0x3ff) + MIN_LOW_SURROGATE);
        }
    }
}
