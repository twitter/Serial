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

import org.jetbrains.annotations.NotNull;

public abstract class SerializerDefs {
    public static final byte TYPE_UNKNOWN = 0;
    public static final byte TYPE_BYTE = 1;
    public static final byte TYPE_INT = 2;
    public static final byte TYPE_LONG = 3;
    public static final byte TYPE_FLOAT = 4;
    public static final byte TYPE_DOUBLE = 5;
    public static final byte TYPE_BOOLEAN = 6;
    public static final byte TYPE_NULL = 7;
    public static final byte TYPE_STRING_UTF8 = 8;
    public static final byte TYPE_START_OBJECT = 9;
    public static final byte TYPE_START_OBJECT_DEBUG = 10;
    public static final byte TYPE_END_OBJECT = 11;
    public static final byte TYPE_EOF = 12;
    public static final byte TYPE_STRING_ASCII = 13;
    public static final byte TYPE_BYTE_ARRAY = 14;

    @NotNull
    public static String getTypeName(byte type) {
        switch (type) {
            case TYPE_BYTE: {
                return "byte";
            }
            case TYPE_BOOLEAN: {
                return "boolean";
            }
            case TYPE_INT: {
                return "int";
            }
            case TYPE_LONG: {
                return "long";
            }
            case TYPE_FLOAT: {
                return "float";
            }
            case TYPE_DOUBLE: {
                return "double";
            }
            case TYPE_STRING_ASCII:
            case TYPE_STRING_UTF8: {
                return "string";
            }
            case TYPE_NULL: {
                return "null";
            }
            case TYPE_START_OBJECT:
            case TYPE_START_OBJECT_DEBUG: {
                return "start_object";
            }
            case TYPE_END_OBJECT: {
                return "end_object";
            }
            case TYPE_EOF: {
                return "eof";
            }
            case TYPE_BYTE_ARRAY: {
                return "byte_array";
            }
            default: {
                return "unknown (" + type + ")";
            }
        }
    }
}
