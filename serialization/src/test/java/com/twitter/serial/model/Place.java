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
import com.twitter.serial.object.ObjectBuilder;
import com.twitter.serial.serializer.SerializationContext;
import com.twitter.serial.stream.SerializerInput;
import com.twitter.serial.stream.SerializerOutput;
import com.twitter.serial.serializer.BuilderSerializer;
import com.twitter.serial.serializer.CoreSerializers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Place {
    @NotNull
    public static final BuilderSerializer<Place, Builder> SERIALIZER = new TwitterPlaceSerializer();
    @NotNull
    public final String placeId;
    @NotNull
    public final PlaceType placeType;
    @NotNull
    public final String name;
    @Nullable
    public final BoundingBox boundingBox;
    @Nullable
    public final Coordinate centroid;
    @Nullable
    public final String country;
    @Nullable
    public final String countryCode;
    @Nullable
    public final String address;
    @Nullable
    public final String handle;

    private Place(@NotNull Builder builder) {
        this.placeId = builder.mPlaceId;
        this.placeType = builder.mPlaceType;
        this.name = builder.mName;
        this.boundingBox = builder.mBoundingBox;
        this.centroid = builder.mCentroid;
        this.country = builder.mCountry;
        this.countryCode = builder.mCountryCode;
        this.address = builder.mAddress;
        this.handle = builder.mHandle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Place place = (Place) o;

        return placeId.equals(place.placeId)
                && placeType == place.placeType
                && name.equals(place.name)
                && InternalSerialUtils.equals(boundingBox, place.boundingBox)
                && InternalSerialUtils.equals(centroid, place.centroid)
                && InternalSerialUtils.equals(country, place.country)
                && InternalSerialUtils.equals(countryCode, place.countryCode)
                && InternalSerialUtils.equals(address, place.address)
                && InternalSerialUtils.equals(handle, place.handle);
    }

    @Override
    public int hashCode() {
        return InternalSerialUtils.hashCode(placeId, placeType, name, boundingBox, centroid, country,
                countryCode, address, handle);
    }

    /**
     * Builder for {@link Place}.
     */
    public static final class Builder extends ObjectBuilder<Place> {
        @NotNull
        String mPlaceId = "";
        @NotNull
        PlaceType mPlaceType = PlaceType.UNKNOWN;
        @NotNull
        String mName = "";
        @Nullable
        BoundingBox mBoundingBox;
        @Nullable
        Coordinate mCentroid;
        @Nullable
        String mCountry;
        @Nullable
        String mCountryCode;
        @Nullable
        String mAddress;
        @Nullable
        String mHandle;

        @NotNull
        public Builder setPlaceId(@Nullable String placeId) {
            mPlaceId = InternalSerialUtils.getOrDefault(placeId, mPlaceId);
            return this;
        }

        @NotNull
        public Builder setPlaceType(@NotNull PlaceType placeType) {
            mPlaceType = InternalSerialUtils.getOrDefault(placeType, mPlaceType);
            return this;
        }

        @NotNull
        public Builder setName(@Nullable String name) {
            mName = InternalSerialUtils.getOrEmpty(name);
            return this;
        }

        @NotNull
        public Builder setBoundingBox(@Nullable BoundingBox boundingBox) {
            mBoundingBox = boundingBox;
            return this;
        }

        @NotNull
        public Builder setCentroid(@Nullable Coordinate centroid) {
            mCentroid = centroid;
            return this;
        }

        @NotNull
        public Builder setCountry(@Nullable String country) {
            mCountry = country;
            return this;
        }

        @NotNull
        public Builder setCountryCode(@Nullable String countryCode) {
            mCountryCode = countryCode;
            return this;
        }

        @NotNull
        public Builder setAddress(@Nullable String address) {
            mAddress = address;
            return this;
        }

        @NotNull
        public Builder setHandle(@Nullable String handle) {
            mHandle = handle;
            return this;
        }

        @NotNull
        @Override
        protected Place buildObject() {
            return new Place(this);
        }
    }

    /**
     * Serializer for {@link Builder}.
     */
    private static class TwitterPlaceSerializer extends BuilderSerializer<Place, Builder> {
        @Override
        protected void serializeObject(@NotNull SerializationContext context,
                @NotNull SerializerOutput output, @NotNull Place place)
                throws IOException {
            output.writeString(place.placeId)
                    .writeObject(context,
                            place.placeType, CoreSerializers.getEnumSerializer(PlaceType.class))
                    .writeString(place.name)
                    .writeObject(context, place.boundingBox, BoundingBox.SERIALIZER)
                    .writeObject(context, place.centroid, Coordinate.SERIALIZER)
                    .writeString(place.country)
                    .writeString(place.countryCode)
                    .writeString(place.address)
                    .writeString(place.handle);
        }

        @NotNull
        @Override
        protected Builder createBuilder() {
            return new Builder();
        }

        @Override
        protected void deserializeToBuilder(@NotNull SerializationContext context,
                @NotNull SerializerInput input, @NotNull Builder builder, int versionNumber)
                throws IOException, ClassNotFoundException {
            builder.setPlaceId(input.readString())
                    .setPlaceType(CoreSerializers.getEnumSerializer(PlaceType.class)
                            .deserializeNotNull(context, input))
                    .setName(input.readString())
                    .setBoundingBox(BoundingBox.SERIALIZER.deserialize(context, input))
                    .setCentroid(Coordinate.SERIALIZER.deserialize(context, input))
                    .setCountry(input.readString())
                    .setCountryCode(input.readString())
                    .setAddress(input.readString())
                    .setHandle(input.readString());
        }
    }
}
