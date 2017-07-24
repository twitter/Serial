package com.serial.util.serialization.serializer;

import com.serial.util.internal.InternalSerialUtils;
import com.serial.util.serialization.SerializationContext;
import com.serial.util.serialization.base.SerializationUtils;
import com.serial.util.serialization.base.SerializerInput;
import com.serial.util.serialization.base.SerializerOutput;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Serializers of collection objects: {@link List}, {@link Set}, {@link Map}.
 */
public class CollectionSerializers {
    /**
     * @param itemSerializer of the {@code T}
     * @param <T>            the object in the list.
     * @return a {@link Serializer} for list T.
     */
    @NotNull
    public static <T> Serializer<List<T>> getListSerializer(@NotNull final Serializer<T> itemSerializer) {
        return new ObjectSerializer<List<T>>() {
            @Override
            protected void serializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerOutput output, @NotNull List<T> list) throws IOException {
                serializeList(context, output, list, itemSerializer);
            }

            @NotNull
            @Override
            protected List<T> deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber)
                    throws IOException, ClassNotFoundException {
                return InternalSerialUtils.checkIsNotNull(deserializeList(context, input, itemSerializer));
            }
        };
    }

    /**
     * @param itemSerializer of the {@code T}
     * @param <T> the object in the set.
     * @return a {@link Serializer} for set T.
     */
    @NotNull
    public static <T> Serializer<Set<T>> getSetSerializer(@NotNull final Serializer<T> itemSerializer) {
        return new ObjectSerializer<Set<T>>() {
            @Override
            protected void serializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerOutput output, @NotNull Set<T> object)
                    throws IOException {
                serializeSet(context, output, object, itemSerializer);
            }

            @NotNull
            @Override
            protected Set<T> deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber)
                    throws IOException, ClassNotFoundException {
                return InternalSerialUtils.checkIsNotNull(deserializeSet(context, input, itemSerializer));
            }
        };
    }

    /**
     * @param keySerializer   serializer for the key
     * @param valueSerializer serializer for the value
     * @return a {@link Serializer} for the map.
     */

    @NotNull
    public static <K, V> Serializer<Map<K, V>> getMapSerializer(@NotNull final Serializer<K> keySerializer,
                                                                @NotNull final Serializer<V> valueSerializer) {
        return new ObjectSerializer<Map<K, V>>() {
            @Override
            protected void serializeObject(@NotNull SerializationContext context,
                    @NotNull SerializerOutput output, @NotNull Map<K, V> map)
                    throws IOException {
                serializeMap(context, output, map, keySerializer, valueSerializer);
            }

            @NotNull
            @Override
            protected Map<K, V> deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber)
                    throws IOException, ClassNotFoundException {
                final Map<K, V> map = deserializeMap(context, input, keySerializer, valueSerializer);
                return InternalSerialUtils.checkIsNotNull(map);
            }
        };
    }

    private static <T> void serializeList(@NotNull SerializationContext context,
            @NotNull SerializerOutput output, @Nullable List<T> list,
            @NotNull Serializer<T> serializer) throws IOException {
        if (!SerializationUtils.writeNullIndicator(output, list)) {
            output.writeInt(list.size());
            for (T item : list) {
                serializer.serialize(context, output, item);
            }
        }
    }

    @Nullable
    private static <T> List<T> deserializeList(@NotNull SerializationContext context,
            @NotNull SerializerInput input, @NotNull Serializer<T> serializer)
            throws IOException, ClassNotFoundException {
        if (SerializationUtils.readNullIndicator(input)) {
            return null;
        }
        final int size = input.readInt();
        final List<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(serializer.deserialize(context, input));
        }
        return list;
    }

    private static <T> void serializeSet(@NotNull SerializationContext context,
            @NotNull SerializerOutput output, @Nullable Set<T> set,
            @NotNull Serializer<T> itemSerializer) throws IOException {
        if (!SerializationUtils.writeNullIndicator(output, set)) {
            output.writeInt(set.size());
            for (T item : set) {
                itemSerializer.serialize(context, output, item);
            }
        }
    }

    @Nullable
    private static <T> Set<T> deserializeSet(@NotNull SerializationContext context,
            @NotNull SerializerInput input, @NotNull Serializer<T> itemSerializer)
            throws IOException, ClassNotFoundException {
        if (SerializationUtils.readNullIndicator(input)) {
            return null;
        }
        final int size = input.readInt();
        Set<T> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            set.add(itemSerializer.deserialize(context, input));
        }
        return set;
    }

    private static <K, V> void serializeMap(@NotNull SerializationContext context,
            @NotNull SerializerOutput output, @Nullable Map<K, V> map,
            @NotNull Serializer<K> keySerializer, @NotNull Serializer<V> valueSerializer)
            throws IOException {
        if (!SerializationUtils.writeNullIndicator(output, map)) {
            output.writeInt(map.size());
            for (Map.Entry<K, V> entry : map.entrySet()) {
                keySerializer.serialize(context, output, entry.getKey());
                valueSerializer.serialize(context, output, entry.getValue());
            }
        }
    }

    @Nullable
    private static <K, V> Map<K, V> deserializeMap(@NotNull SerializationContext context,
            @NotNull SerializerInput input, @NotNull Serializer<K> keySerializer,
            @NotNull Serializer<V> valueSerializer)
            throws IOException, ClassNotFoundException {
        if (SerializationUtils.readNullIndicator(input)) {
            return null;
        }
        final int size = input.readInt();
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            final K key = SerializationUtils.readNullIndicator(input) ? null : input.readObject(
                    context, keySerializer);
            final V value = SerializationUtils.readNullIndicator(input) ? null : input.readObject(
                    context, valueSerializer);
            map.put(key, value);
        }
        return map;
    }
}
