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
import com.twitter.serial.serializer.CollectionSerializers;
import com.twitter.serial.serializer.ObjectSerializer;
import com.twitter.serial.serializer.Serializer;

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
        return InternalSerialUtils.hashCode(coordinates);
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
