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
import com.twitter.serial.util.InternalSerialUtils;

abstract class ByteBufferSerializerDefs extends SerializerDefs {
    public static final int DEFAULT_BUFFER_SIZE = InternalSerialUtils.KB_BYTES;

    public static final int SIZE_BYTE = 1;
    public static final int SIZE_SHORT = Short.SIZE / Byte.SIZE;
    public static final int SIZE_INT = Integer.SIZE / Byte.SIZE;
    public static final int SIZE_LONG = Long.SIZE / Byte.SIZE;
    public static final int SIZE_DOUBLE = Double.SIZE / Byte.SIZE;
    public static final int SIZE_FLOAT = Float.SIZE / Byte.SIZE;

    public static final byte SUBTYPE_UNDEFINED = 0;
    public static final byte SUBTYPE_DEFAULT = 1;
    public static final byte SUBTYPE_BYTE = 2;
    public static final byte SUBTYPE_SHORT = 3;
    public static final byte SUBTYPE_INT = 4;
    public static final byte SUBTYPE_LONG = 5;

    public static final byte HEADER_BYTE = makeHeader(TYPE_BYTE, SUBTYPE_UNDEFINED);
    public static final byte HEADER_BYTE_ZERO = makeHeader(TYPE_BYTE, SUBTYPE_DEFAULT);

    public static final byte HEADER_BOOLEAN_FALSE = makeHeader(TYPE_BOOLEAN, SUBTYPE_UNDEFINED);
    public static final byte HEADER_BOOLEAN_TRUE = makeHeader(TYPE_BOOLEAN, SUBTYPE_DEFAULT);

    public static final byte HEADER_FLOAT = makeHeader(TYPE_FLOAT, SUBTYPE_UNDEFINED);
    public static final byte HEADER_FLOAT_ZERO = makeHeader(TYPE_FLOAT, SUBTYPE_DEFAULT);

    public static final byte HEADER_DOUBLE = makeHeader(TYPE_DOUBLE, SUBTYPE_UNDEFINED);
    public static final byte HEADER_DOUBLE_ZERO = makeHeader(TYPE_DOUBLE, SUBTYPE_DEFAULT);

    public static final byte HEADER_NULL = makeHeader(TYPE_NULL, SUBTYPE_UNDEFINED);

    public static final byte HEADER_STRING_EMPTY = makeHeader(TYPE_STRING_ASCII, SUBTYPE_DEFAULT);

    public static final byte HEADER_BYTE_ARRAY_EMPTY = makeHeader(TYPE_BYTE_ARRAY, SUBTYPE_UNDEFINED);

    public static final byte HEADER_END_OBJECT = makeHeader(TYPE_END_OBJECT, SUBTYPE_UNDEFINED);

    private static final int TYPE_BITS = 5;
    private static final int TYPE_MASK = (1 << TYPE_BITS) - 1;
    private static final int SUBTYPE_BITS = Byte.SIZE - TYPE_BITS;
    private static final int SUBTYPE_MASK = (1 << SUBTYPE_BITS) - 1;

    private ByteBufferSerializerDefs() {
    }

    public static byte makeHeader(byte type, byte subtype) {
        return (byte) ((type << SUBTYPE_BITS) | subtype);
    }

    public static byte getHeaderType(byte header) {
        return (byte) ((header >> SUBTYPE_BITS) & TYPE_MASK);
    }

    public static byte getHeaderSubtype(byte header) {
        return (byte) (header & SUBTYPE_MASK);
    }
}
