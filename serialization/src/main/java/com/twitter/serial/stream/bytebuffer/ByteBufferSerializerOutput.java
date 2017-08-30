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

import com.twitter.serial.stream.SerializerDefs;
import com.twitter.serial.stream.SerializerOutput;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Custom serialization class to serialize primitive types and Strings. Objects can be serialized recursively as a
 * series of these types. Write methods for each type write the values to a {@link ByteBuffer} that will grow
 * automatically as needed. Use {@link ByteBufferSerializerOutput#getSerializedData()} to get the full byte array.
 */
public final class ByteBufferSerializerOutput extends SerializerOutput<ByteBufferSerializerOutput> {
    @NotNull private ByteBuffer mByteBuffer;

    public ByteBufferSerializerOutput() {
        this(new byte[ByteBufferSerializerDefs.DEFAULT_BUFFER_SIZE]);
    }

    public ByteBufferSerializerOutput(@Nullable byte[] byteArray) {
        if (byteArray == null) {
            byteArray = new byte[ByteBufferSerializerDefs.DEFAULT_BUFFER_SIZE];
        } else if (byteArray.length == 0) {
            throw new IllegalArgumentException("The byte buffer must be non empty.");
        }
        mByteBuffer = ByteBuffer.wrap(byteArray);
    }

    /**
     * Gets a copy of the array that's the exact size of the serialized content, starting from the beginning of the
     * array to the last serialized value.
     * @return exact sized copy of the array
     */
    @NotNull
    public byte[] getSerializedData() {
        final byte[] arrayCopy = new byte[mByteBuffer.position()];
        mByteBuffer.rewind();
        mByteBuffer.get(arrayCopy);
        return arrayCopy;
    }

    @Override
    public boolean isPeekSupported() {
        return true;
    }

    public int getPosition() {
        return mByteBuffer.position();
    }

    public byte peekTypeAtPosition(int position) {
        return position < mByteBuffer.position() ?
                ByteBufferSerializerDefs.getHeaderType(mByteBuffer.get(position)) : SerializerDefs.TYPE_EOF;
    }

    public int getBufferCapacity() {
        return mByteBuffer.capacity();
    }

    @Override
    @NotNull
    public ByteBufferSerializerOutput writeByte(byte val) {
        if (val == 0) {
            writeHeader(ByteBufferSerializerDefs.HEADER_BYTE_ZERO);
        } else {
            writeHeader(ByteBufferSerializerDefs.HEADER_BYTE);
            ensureCapacity(ByteBufferSerializerDefs.SIZE_BYTE);
            mByteBuffer.put(val);
        }
        return this;
    }

    @Override
    @NotNull
    public ByteBufferSerializerOutput writeBoolean(boolean val) {
        if (val) {
            writeHeader(ByteBufferSerializerDefs.HEADER_BOOLEAN_TRUE);
        } else {
            writeHeader(ByteBufferSerializerDefs.HEADER_BOOLEAN_FALSE);
        }
        return this;
    }

    @Override
    @NotNull
    public ByteBufferSerializerOutput writeInt(int val) {
        writeIntHeader(SerializerDefs.TYPE_INT, val);
        return this;
    }

    @Override
    @NotNull
    public ByteBufferSerializerOutput writeLong(long val) {
        writeLongHeader(SerializerDefs.TYPE_LONG, val);
        return this;
    }

    @Override
    @NotNull
    public ByteBufferSerializerOutput writeFloat(float val) {
        if (val == 0f) {
            writeHeader(ByteBufferSerializerDefs.HEADER_FLOAT_ZERO);
        } else {
            writeHeader(ByteBufferSerializerDefs.HEADER_FLOAT);
            ensureCapacity(ByteBufferSerializerDefs.SIZE_FLOAT);
            mByteBuffer.putFloat(val);
        }
        return this;
    }

    @Override
    @NotNull
    public ByteBufferSerializerOutput writeDouble(double val) {
        if (val == 0d) {
            writeHeader(ByteBufferSerializerDefs.HEADER_DOUBLE_ZERO);
        } else {
            writeHeader(ByteBufferSerializerDefs.HEADER_DOUBLE);
            ensureCapacity(ByteBufferSerializerDefs.SIZE_DOUBLE);
            mByteBuffer.putDouble(val);
        }
        return this;
    }

    @Override
    @NotNull
    public ByteBufferSerializerOutput writeString(@Nullable String val) {
        if (val == null) {
            writeNull();
        } else if (val.isEmpty()) {
            writeHeader(ByteBufferSerializerDefs.HEADER_STRING_EMPTY);
        } else {
            encodeString(val);
        }
        return this;
    }

    @NotNull
    @Override
    public ByteBufferSerializerOutput writeByteArray(@Nullable byte[] val) throws IOException {
        if (val == null) {
            writeNull();
        } else if (val.length == 0) {
            writeHeader(ByteBufferSerializerDefs.HEADER_BYTE_ARRAY_EMPTY);
        } else {
            writeIntHeader(SerializerDefs.TYPE_BYTE_ARRAY, val.length);
            ensureCapacity(val.length);
            mByteBuffer.put(val);
        }
        return this;
    }

    @Override
    @NotNull
    public ByteBufferSerializerOutput writeNull() {
        writeHeader(ByteBufferSerializerDefs.HEADER_NULL);
        return this;
    }

    @Override
    @NotNull
    public ByteBufferSerializerOutput writeObjectStart(int versionNumber) {
        if (versionNumber < 0) {
            throw new IllegalArgumentException("The version number is negative: " + versionNumber + ".");
        }
        writeIntHeader(SerializerDefs.TYPE_START_OBJECT, versionNumber);
        return this;
    }

    @NotNull
    @Override
    public ByteBufferSerializerOutput writeObjectStart(int versionNumber, @NotNull String className) {
        if (versionNumber < 0) {
            throw new IllegalArgumentException("The version number is negative: " + versionNumber + ".");
        }
        writeIntHeader(SerializerDefs.TYPE_START_OBJECT_DEBUG, versionNumber);
        writeString(className);
        return this;
    }

    @Override
    @NotNull
    public ByteBufferSerializerOutput writeObjectEnd() {
        writeHeader(ByteBufferSerializerDefs.HEADER_END_OBJECT);
        return this;
    }

    private void writeHeader(byte headerType) {
        ensureCapacity(ByteBufferSerializerDefs.SIZE_BYTE);
        mByteBuffer.put(headerType);
    }

    private void writeIntHeader(byte type, int val) {
        if (val == 0) {
            writeHeader(ByteBufferSerializerDefs.makeHeader(type, ByteBufferSerializerDefs.SUBTYPE_DEFAULT));
        } else if ((val & 0xFFFFFF00) == 0) {
            writeHeader(ByteBufferSerializerDefs.makeHeader(type, ByteBufferSerializerDefs.SUBTYPE_BYTE));
            ensureCapacity(ByteBufferSerializerDefs.SIZE_BYTE);
            mByteBuffer.put((byte) val);
        } else if ((val & 0xFFFF0000) == 0) {
            writeHeader(ByteBufferSerializerDefs.makeHeader(type, ByteBufferSerializerDefs.SUBTYPE_SHORT));
            ensureCapacity(ByteBufferSerializerDefs.SIZE_SHORT);
            mByteBuffer.putShort((short) val);
        } else {
            writeHeader(ByteBufferSerializerDefs.makeHeader(type, ByteBufferSerializerDefs.SUBTYPE_INT));
            ensureCapacity(ByteBufferSerializerDefs.SIZE_INT);
            mByteBuffer.putInt(val);
        }
    }

    private void writeLongHeader(byte type, long val) {
        if ((val & 0xFFFFFFFF00000000L) == 0) {
            writeIntHeader(type, (int) val);
        } else {
            writeHeader(ByteBufferSerializerDefs.makeHeader(type, ByteBufferSerializerDefs.SUBTYPE_LONG));
            ensureCapacity(ByteBufferSerializerDefs.SIZE_LONG);
            mByteBuffer.putLong(val);
        }
    }

    private void ensureCapacity(int sizeNeeded) {
        if (mByteBuffer.remaining() < sizeNeeded) {
            final int position = mByteBuffer.position();
            final byte[] bufferContents = mByteBuffer.array();
            final byte[] newBufferContents = new byte[2 * mByteBuffer.capacity()];
            System.arraycopy(bufferContents, 0, newBufferContents, 0, position);
            final ByteBuffer newBuffer = ByteBuffer.wrap(newBufferContents);
            newBuffer.position(position);
            mByteBuffer = newBuffer;
            ensureCapacity(sizeNeeded);
        }
    }

    /**
     * Encodes the string into the buffer using UTF-8.
     */
    private void encodeString(@NotNull String string) {
        final int length = string.length();
        final int headerPosition = mByteBuffer.position();
        writeIntHeader(SerializerDefs.TYPE_STRING_ASCII, length);
        boolean isAscii = true;
        for (int i = 0; i < length; ++i) {
            final int ch = (int) string.charAt(i);
            if (ch < 0x80) {
                ensureCapacity(ByteBufferSerializerDefs.SIZE_BYTE);
                mByteBuffer.put((byte) ch);
            } else {
                isAscii = false;
                if (ch < 0x800) {
                    ensureCapacity(2 * ByteBufferSerializerDefs.SIZE_BYTE);
                    mByteBuffer.put((byte) ((ch >> 6) | 0xc0));
                    mByteBuffer.put((byte) ((ch & 0x3f) | 0x80));
                } else if (isSurrogate(ch)) {
                    // A supplementary character.
                    final int low = i + 1 != length ? string.charAt(i + 1) : 0;
                    if (!isSurrogateLead(ch) || !isSurrogate(low) || !isSurrogateTrail(low)) {
                        ensureCapacity(ByteBufferSerializerDefs.SIZE_BYTE);
                        mByteBuffer.put((byte) '?');
                    } else {
                        // Now we know we have a *valid* surrogate pair, we can consume the low surrogate.
                        //noinspection AssignmentToForLoopParameter
                        ++i;
                        ensureCapacity(4 * ByteBufferSerializerDefs.SIZE_BYTE);
                        final int supplementary = getSupplementary(ch, low);
                        mByteBuffer.put((byte) ((supplementary >> 18) | 0xf0));
                        mByteBuffer.put((byte) (((supplementary >> 12) & 0x3f) | 0x80));
                        mByteBuffer.put((byte) (((supplementary >> 6) & 0x3f) | 0x80));
                        mByteBuffer.put((byte) ((supplementary & 0x3f) | 0x80));
                    }
                } else {
                    ensureCapacity(3 * ByteBufferSerializerDefs.SIZE_BYTE);
                    mByteBuffer.put((byte) ((ch >> 12) | 0xe0));
                    mByteBuffer.put((byte) (((ch >> 6) & 0x3f) | 0x80));
                    mByteBuffer.put((byte) ((ch & 0x3f) | 0x80));
                }
            }
        }

        // If the string is not ASCII, update the header.
        if (!isAscii) {
            final int currentPosition = mByteBuffer.position();
            mByteBuffer.position(headerPosition);
            writeIntHeader(SerializerDefs.TYPE_STRING_UTF8, length);
            mByteBuffer.position(currentPosition);
        }
    }

    private static boolean isSurrogate(int ch) {
        return (ch & 0xfffff800) == 0xd800;
    }

    private static boolean isSurrogateLead(int ch) {
        return (ch & 0x400) == 0;
    }

    private static boolean isSurrogateTrail(int ch) {
        return (ch & 0x400) != 0;
    }

    private static int getSupplementary(int high, int low) {
        final int offset = (0xd800 << 10) + 0xdc00 - 0x10000;
        return (high << 10) + low - offset;
    }
}
