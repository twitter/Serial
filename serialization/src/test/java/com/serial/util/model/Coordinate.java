package com.serial.util.model;

import com.serial.util.object.ObjectUtils;
import com.serial.util.serialization.SerializationContext;
import com.serial.util.serialization.base.SerializerInput;
import com.serial.util.serialization.base.SerializerOutput;
import com.serial.util.serialization.serializer.ObjectSerializer;
import com.serial.util.serialization.serializer.Serializer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Coordinate {
    @NotNull
    public static final Serializer<Coordinate> SERIALIZER = new CoordinateSerializer();

    public final double latitude;
    public final double longitude;

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate) o;

        return Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(latitude, longitude);
    }

    /**
     * Serializer for {@link Coordinate}.
     */
    private static class CoordinateSerializer extends ObjectSerializer<Coordinate> {
        @Override
        protected void serializeObject(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Coordinate coordinate)
                throws IOException {
            output.writeDouble(coordinate.latitude)
                    .writeDouble(coordinate.longitude);
        }

        @NotNull
        @Override
        protected Coordinate deserializeObject(@NotNull SerializationContext context, @NotNull SerializerInput input, int versionNumber)
                throws IOException, ClassNotFoundException {
            final double latitude = input.readDouble();
            final double longitude = input.readDouble();
            return new Coordinate(latitude, longitude);
        }
    }
}
