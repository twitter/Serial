package com.serial.util.serialization.base;

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
