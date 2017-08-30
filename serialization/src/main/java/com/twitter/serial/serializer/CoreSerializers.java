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

package com.twitter.serial.serializer;

import com.twitter.serial.util.InternalSerialUtils;
import com.twitter.serial.util.SerializableUtils;
import com.twitter.serial.util.SerializableClass;
import com.twitter.serial.util.SerializationException;
import com.twitter.serial.util.SerializationUtils;
import com.twitter.serial.stream.SerializerDefs;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.stream.SerializerOutput;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Main standard serializers: primitive types and basic Java types:
 * <ul>
 * <li> {@link Byte}
 * <li> {@link Boolean}
 * <li> {@link Integer}
 * <li> {@link Short}
 * <li> {@link Character}
 * <li> {@link Long}
 * <li> {@link Double}
 * <li> {@link String}
 * <li> empty object
 * <li> simple {@link Object}: primitive values, Strings, or null that are stored as an Object.
 * <li> {@link BigDecimal}
 * <li> int array
 * <li> long array
 * <li> float array
 * <li> double array
 * <li> {@link Date}
 * <li> {@link Enum}
 * <li> {@link Comparator}
 * <li> {@link Serializable}
 * </ul>
 */
public class CoreSerializers {
    public static final Serializer<Byte> BYTE = new ValueSerializer<Byte>() {
        @Override
        protected void serializeValue(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Byte object) throws IOException {
            output.writeByte(object);
        }

        @Override
        @NotNull
        protected Byte deserializeValue(@NotNull SerializationContext context,
                @NotNull SerializerInput input)
                throws IOException {
            return input.readByte();
        }
    };

    public static final Serializer<Boolean> BOOLEAN = new ValueSerializer<Boolean>() {
        @Override
        protected void serializeValue(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Boolean object) throws IOException {
            output.writeBoolean(object);
        }

        @Override
        @NotNull
        protected Boolean deserializeValue(@NotNull SerializationContext context,
                @NotNull SerializerInput input) throws IOException {
            return input.readBoolean();
        }
    };

    public static final Serializer<Integer> INTEGER = new ValueSerializer<Integer>() {
        @Override
        protected void serializeValue(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Integer object) throws IOException {
            output.writeInt(object);
        }

        @Override
        @NotNull
        protected Integer deserializeValue(@NotNull SerializationContext context,
                @NotNull SerializerInput input) throws IOException {
            return input.readInt();
        }
    };

    public static final Serializer<Short> SHORT = new ValueSerializer<Short>() {
        @Override
        protected void serializeValue(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Short object) throws IOException {
            output.writeInt(object);
        }

        @Override
        @NotNull
        protected Short deserializeValue(@NotNull SerializationContext context,
                @NotNull SerializerInput input) throws IOException {
            return (short) input.readInt();
        }
    };

    public static final Serializer<Character> CHARACTER = new ValueSerializer<Character>() {
        @Override
        protected void serializeValue(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Character object) throws IOException {
            output.writeInt(object);
        }

        @Override
        @NotNull
        protected Character deserializeValue(@NotNull SerializationContext context,
                @NotNull SerializerInput input) throws IOException {
            return (char) input.readInt();
        }
    };

    public static final Serializer<Long> LONG = new ValueSerializer<Long>() {
        @Override
        protected void serializeValue(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Long object) throws IOException {
            output.writeLong(object);
        }

        @Override
        @NotNull
        protected Long deserializeValue(@NotNull SerializationContext context,
                @NotNull SerializerInput input) throws IOException {
            return input.readLong();
        }
    };

    public static final Serializer<Float> FLOAT = new ValueSerializer<Float>() {
        @Override
        protected void serializeValue(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Float object) throws IOException {
            output.writeFloat(object);
        }

        @Override
        @NotNull
        protected Float deserializeValue(@NotNull SerializationContext context,
                @NotNull SerializerInput input) throws IOException {
            return input.readFloat();
        }
    };

    public static final Serializer<Double> DOUBLE = new ValueSerializer<Double>() {
        @Override
        protected void serializeValue(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Double object) throws IOException {
            output.writeDouble(object);
        }

        @Override
        @NotNull
        protected Double deserializeValue(@NotNull SerializationContext context,
                @NotNull SerializerInput input) throws IOException {
            return input.readDouble();
        }
    };

    public static final Serializer<String> STRING = new ValueSerializer<String>() {
        @Override
        protected void serializeValue(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull String object) throws IOException {
            output.writeString(object);
        }

        @Override
        @NotNull
        protected String deserializeValue(@NotNull SerializationContext context,
                @NotNull SerializerInput input) throws IOException {
            return input.readNotNullString();
        }
    };

    /**
     * Empty serializer used as a placeholder
     */
    public static final Serializer<Object> EMPTY = new Serializer<Object>() {
        @Override
        public void serialize(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @Nullable Object object) throws IOException {
        }

        @Nullable
        @Override
        public Object deserialize(@NotNull SerializationContext context,
                @NotNull SerializerInput input) throws IOException, ClassNotFoundException {
            return null;
        }
    };

    /**
     * Serializes primitive values, Strings, or null that are stored as an Object.
     */
    public static final Serializer<Object> SIMPLE_OBJECT = new ObjectSerializer<Object>() {
        @Override
        protected void serializeObject(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Object object) throws IOException {
            if (object instanceof String) {
                output.writeString((String) object);
            } else if (object instanceof Integer) {
                output.writeInt((Integer) object);
            } else if (object instanceof Boolean) {
                output.writeBoolean((Boolean) object);
            } else if (object instanceof Double) {
                output.writeDouble((Double) object);
            } else if (object instanceof Float) {
                output.writeFloat((Float) object);
            } else if (object instanceof Long) {
                output.writeLong((Long) object);
            } else {
                throw new IllegalArgumentException(
                        "Simple object serializer does not support objects of type "
                                + object.getClass());
            }
        }

        @NotNull
        @Override
        protected Object deserializeObject(@NotNull SerializationContext context,
                @NotNull SerializerInput input, int versionNumber)
                throws IOException {
            final byte type = input.peekType();
            switch (type) {
                case SerializerDefs.TYPE_STRING_UTF8:
                case SerializerDefs.TYPE_STRING_ASCII: {
                    return input.readNotNullString();
                }
                case SerializerDefs.TYPE_INT: {
                    return input.readInt();
                }
                case SerializerDefs.TYPE_BOOLEAN: {
                    return input.readBoolean();
                }
                case SerializerDefs.TYPE_DOUBLE: {
                    return input.readDouble();
                }
                case SerializerDefs.TYPE_FLOAT: {
                    return input.readFloat();
                }
                case SerializerDefs.TYPE_LONG: {
                    return input.readLong();
                }
                default: {
                    throw new SerializationException(
                            "Unexpected type found in simple object deserialization: " + type);
                }
            }
        }
    };

    @NotNull
    public static final Serializer<BigDecimal> BIG_DECIMAL = new ValueSerializer<BigDecimal>() {
        @Override
        protected void serializeValue(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull BigDecimal decimal)
                throws IOException {
            output.writeString(decimal.toString());
        }

        @NotNull
        @Override
        protected BigDecimal deserializeValue(@NotNull SerializationContext context,
                @NotNull SerializerInput input)
                throws IOException {
            return new BigDecimal(input.readNotNullString());
        }
    };

    @NotNull
    public static final Serializer<int[]> INT_ARRAY = new ObjectSerializer<int[]>() {
        @Override
        protected void serializeObject(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull int[] arr)
                throws IOException {
            output.writeInt(arr.length);
            for (int val : arr) {
                output.writeInt(val);
            }
        }

        @NotNull
        @Override
        protected int[] deserializeObject(@NotNull SerializationContext context,
                @NotNull SerializerInput input, int versionNumber)
                throws IOException {
            final int length = input.readInt();
            final int[] result = new int[length];
            for (int i = 0; i < length; i += 1) {
                result[i] = input.readInt();
            }
            return result;
        }
    };

    @NotNull
    public static final Serializer<long[]> LONG_ARRAY = new ObjectSerializer<long[]>() {
        @Override
        protected void serializeObject(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull long[] arr)
                throws IOException {
            output.writeInt(arr.length);
            for (long val : arr) {
                output.writeLong(val);
            }
        }

        @NotNull
        @Override
        protected long[] deserializeObject(@NotNull SerializationContext context,
                @NotNull SerializerInput input, int versionNumber)
                throws IOException {
            final int length = input.readInt();
            final long[] result = new long[length];
            for (int i = 0; i < length; i += 1) {
                result[i] = input.readLong();
            }
            return result;
        }
    };

    @NotNull
    public static final Serializer<float[]> FLOAT_ARRAY = new ObjectSerializer<float[]>() {
        @Override
        protected void serializeObject(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull float[] arr)
                throws IOException {
            output.writeInt(arr.length);
            for (float val : arr) {
                output.writeFloat(val);
            }
        }

        @NotNull
        @Override
        protected float[] deserializeObject(@NotNull SerializationContext context,
                @NotNull SerializerInput input, int versionNumber)
                throws IOException {
            final int length = input.readInt();
            final float[] result = new float[length];
            for (int i = 0; i < length; i += 1) {
                result[i] = input.readFloat();
            }
            return result;
        }
    };

    @NotNull
    public static final Serializer<double[]> DOUBLE_ARRAY = new ObjectSerializer<double[]>() {
        @Override
        protected void serializeObject(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull double[] arr)
                throws IOException {
            output.writeInt(arr.length);
            for (double val : arr) {
                output.writeDouble(val);
            }
        }

        @NotNull
        @Override
        protected double[] deserializeObject(@NotNull SerializationContext context,
                @NotNull SerializerInput input, int versionNumber)
                throws IOException {
            final int length = input.readInt();
            final double[] result = new double[length];
            for (int i = 0; i < length; i += 1) {
                result[i] = input.readDouble();
            }
            return result;
        }
    };

    @NotNull
    public static final Serializer<Date> DATE = new ValueSerializer<Date>() {
        @Override
        protected void serializeValue(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Date object) throws IOException {
            output.writeLong(object.getTime());
        }

        @NotNull
        @Override
        protected Date deserializeValue(@NotNull SerializationContext context,
                @NotNull SerializerInput input)
                throws IOException, ClassNotFoundException {
            return new Date(input.readLong());
        }
    };

    @NotNull
    public static <T extends Enum<T>> Serializer<T> getEnumSerializer(
            @NotNull final Class<T> enumType) {
        return new ObjectSerializer<T>() {
            @Override
            protected void serializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerOutput output, @NotNull T value) throws IOException {
                serializeEnum(context, output, value);
            }

            @NotNull
            @Override
            protected T deserializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerInput input, int versionNumber)
                    throws IOException, ClassNotFoundException {
                return deserializeEnum(input, enumType);
            }
        };
    }

    public static <T extends Enum<T>> void serializeEnum(@NotNull SerializationContext context,
            @NotNull SerializerOutput output,
            @NotNull T value)
            throws IOException {
        output.writeString(value.name());
    }

    @NotNull
    @SafeVarargs
    public static <B> Serializer<B> getBaseClassSerializer(
            @NotNull SerializableClass<? extends B>... classes) {
        return getBaseClassSerializer(Arrays.asList(classes));
    }

    @NotNull
    public static <B> Serializer<B> getBaseClassSerializer(
            @NotNull final List<SerializableClass<? extends B>> subclasses) {
        return new ObjectSerializer<B>() {
            private boolean mNeedToCheckBaseClassSerializer = true;

            private void checkBaseClassSerializer(@NotNull SerializationContext context) {
                if (mNeedToCheckBaseClassSerializer && context.isDebug()) {
                    mNeedToCheckBaseClassSerializer = false;
                    final int len = subclasses.size();
                    for (int i = 1; i < len; ++i) {
                        final SerializableClass<B> subclass = InternalSerialUtils.cast(subclasses.get(i));
                        if (!SerializableClass.isDummy(subclass)) {
                            for (int p = 0; p < i; ++p) {
                                if (!SerializableClass.isDummy(subclasses.get(p))) {
                                    subclasses.get(p).klass.isAssignableFrom(subclass.klass);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            protected void serializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerOutput output, @NotNull B object) throws IOException {
                checkBaseClassSerializer(context);

                final int len = subclasses.size();
                for (int i = 0; i < len; i++) {
                    final SerializableClass<B> subclass = InternalSerialUtils.cast(subclasses.get(i));
                    if (!SerializableClass.isDummy(subclass)) {
                        if (object.getClass().equals(subclass.klass)) {
                            output.writeInt(i)
                                    .writeObject(context,
                                            subclass.klass.cast(object), subclass.serializer);
                            return;
                        }
                    }
                }
                throw new SerializationException(
                        "Serializer not defined for base class serialization for : " +
                                object.getClass().getSimpleName());
            }

            @Nullable
            @Override
            protected B deserializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerInput input, int versionNumber)
                    throws IOException, ClassNotFoundException {
                checkBaseClassSerializer(context);

                final int type = input.readInt();
                if (type > subclasses.size()) {
                    throw new SerializationException(
                            "Invalid type found in base class deserialization: " + type);
                }
                final SerializableClass<? extends B> subclass = subclasses.get(type);
                if (SerializableClass.isDummy(subclass)) {
                    SerializationUtils.skipObject(input);
                    return null;
                }
                return InternalSerialUtils.cast(subclass.serializer.deserializeNotNull(context, input));
            }
        };
    }

    /**
     * Returns a serializer that uses Java serialization.
     */
    @NotNull
    public static <T extends Serializable> Serializer<T> getSerializableSerializer() {
        return new ObjectSerializer<T>() {
            @Override
            protected void serializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerOutput output, @NotNull T object) throws IOException {
                //noinspection BlacklistedMethod
                output.writeByteArray(SerializableUtils.toByteArray(object));
            }

            @Nullable
            @Override
            protected T deserializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerInput input, int versionNumber)
                    throws IOException, ClassNotFoundException {
                //noinspection BlacklistedMethod
                return SerializableUtils.fromByteArray(input.readByteArray());
            }
        };
    }

    @NotNull
    private static <T extends Enum<T>> T deserializeEnum(@NotNull SerializerInput input,
            @NotNull Class<T> enumType)
            throws IOException {
        return Enum.valueOf(enumType, input.readNotNullString());
    }

    /**
     * Serializes a stateless object. This method just writes the class name into the stream, and assumes that a call
     * into a default constructor can recreate the object. Use {@link #deserializeStatelessObject(SerializerInput)}
     * to deserialize.
     */
    private static void serializeStatelessObject(@NotNull SerializationContext context,
            @NotNull SerializerOutput output, @NotNull Object object)
            throws IOException {
        if (context.isDebug()) {
            try {
                object.getClass().getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(
                        "Class has no default constructor: " + object.getClass());
            }
        }
        output.writeString(object.getClass().getName());
    }

    /**
     * Deserialize an object serialized with {@link #serializeStatelessObject(SerializationContext, SerializerOutput, Object)}.
     */
    @NotNull
    private static Object deserializeStatelessObject(@NotNull SerializerInput input)
            throws IOException, ClassNotFoundException {
        final Class<?> klass = Class.forName(input.readNotNullString());
        //noinspection TryWithIdenticalCatches
        try {
            return klass.newInstance();
        } catch (InstantiationException ignore) {
        } catch (IllegalAccessException ignore) {
        }
        throw new IllegalStateException("Object has no default constructor: " + klass);
    }
}
