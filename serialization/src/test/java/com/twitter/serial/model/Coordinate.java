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

package com.twitter.serial.model;

import com.twitter.serial.util.InternalSerialUtils;
import com.twitter.serial.serializer.SerializationContext;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.stream.SerializerOutput;
import com.twitter.serial.serializer.ObjectSerializer;
import com.twitter.serial.serializer.Serializer;

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
        return InternalSerialUtils.hashCode(latitude, longitude);
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
