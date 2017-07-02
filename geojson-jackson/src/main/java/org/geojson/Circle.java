package org.geojson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Circle extends GeoJsonObject {

    private LngLatAlt coordinates;
    private double radius;

    @JsonCreator
    public Circle(@JsonProperty("coordinates") LngLatAlt center, @JsonProperty("radius") double radius) {
        this.coordinates = center;
        this.radius = radius;
    }

    @Override
    public <T> T accept(GeoJsonObjectVisitor<T> geoJsonObjectVisitor) {
        return geoJsonObjectVisitor.visit(this);
    }

    public LngLatAlt getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LngLatAlt coordinates) {
        this.coordinates = coordinates;
    }

    public double getRadius() {
        return this.radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "Cirlce{" + "coordinates=" + coordinates + ", radius=" + radius + "} " + super.toString();
    }
}
