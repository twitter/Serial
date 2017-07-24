package com.serial.util.model;

import com.serial.util.internal.InternalSerialUtils;
import com.serial.util.object.ObjectUtils;
import com.serial.util.serialization.SerializationContext;
import com.serial.util.serialization.base.SerializerInput;
import com.serial.util.serialization.base.SerializerOutput;
import com.serial.util.serialization.serializer.CollectionSerializers;
import com.serial.util.serialization.serializer.ObjectSerializer;
import com.serial.util.serialization.serializer.Serializer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BoundingBox {
    @NotNull
    public static final Serializer<BoundingBox> SERIALIZER = new BoundingBoxSerializer();
    @NotNull
    public final List<Coordinate> coordinates;

    public BoundingBox(@NotNull List<Coordinate> coordinates) {
        this.coordinates = new ArrayList<>(coordinates);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoundingBox that = (BoundingBox) o;

        return coordinates.equals(that.coordinates);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(coordinates);
    }

    /**
     * Serializer for {@link BoundingBox}.
     */
    private static class BoundingBoxSerializer extends ObjectSerializer<BoundingBox> {
        @Override
        protected void serializeObject(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull BoundingBox boundingBox)
                throws IOException {
            CollectionSerializers.getListSerializer(Coordinate.SERIALIZER)
                    .serialize(context, output, boundingBox.coordinates);
        }

        @NotNull
        @Override
        protected BoundingBox deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber)
                throws IOException, ClassNotFoundException {
            final List<Coordinate> coordinates = CollectionSerializers.getListSerializer(Coordinate.SERIALIZER)
                    .deserialize(context, input);
            return new BoundingBox(InternalSerialUtils.checkIsNotNull(coordinates));
        }
    }

}
