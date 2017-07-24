package com.serial.util.model;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Provides sample data for test purposes.
 */
public class SampleDataProvider {
    @NotNull
    public static Place createSamplePlace() {
        return new Place.Builder()
                .setName("San Francisco City Hall")
                .setAddress("1 Dr Carlton B Goodlett Place, San Francisco, CA 94102")
                .setBoundingBox(createSampleBoundingBox())
                .setCentroid(new Coordinate(37.779260, -122.419248))
                .setCountry("USA")
                .setCountryCode("US")
                .setHandle("SFCity_Hall")
                .setPlaceId("1")
                .setPlaceType(PlaceType.POI)
                .build();
    }

    @NotNull
    private static BoundingBox createSampleBoundingBox() {
        final Coordinate northWest = new Coordinate(37.779894, -122.419906);
        final Coordinate northEast = new Coordinate(37.779894, -122.418624);
        final Coordinate southWest = new Coordinate(37.778626, -122.419906);
        final Coordinate southEast = new Coordinate(37.778626, -122.418624);
        return new BoundingBox(Arrays.asList(northWest, northEast, southEast, southWest));
    }
}
