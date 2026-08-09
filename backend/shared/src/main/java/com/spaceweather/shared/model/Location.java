package com.spaceweather.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class Location {
    private final double latitude;
    private final double longitude;

    @JsonCreator
    public Location(
            @JsonProperty("latitude") double latitude,
            @JsonProperty("longitude") double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(latitude, location.latitude) == 0 &&
               Double.compare(longitude, location.longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return String.format("(%.2f°, %.2f°)", latitude, longitude);
    }
}
