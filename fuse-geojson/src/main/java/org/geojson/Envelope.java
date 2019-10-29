package org.geojson;

/*-
 * #%L
 * fuse-geojson
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Envelope extends Geometry<LngLatAlt> {
    @JsonCreator
    public Envelope(@JsonProperty("coordinates") List<LngLatAlt> coordinates) {
        this(coordinates.get(0), coordinates.get(1));
    }

    public Envelope(LngLatAlt point1, LngLatAlt point2) {
        double minLon = Math.min(point1.getLongitude(), point2.getLongitude());
        double maxLon = Math.max(point1.getLongitude(), point2.getLongitude());
        double minLat = Math.min(point1.getLatitude(), point2.getLatitude());
        double maxLat = Math.max(point1.getLatitude(), point2.getLatitude());

        this.coordinates.add(new LngLatAlt(minLon, minLat));
        this.coordinates.add(new LngLatAlt(maxLon, maxLat));
    }

    @Override
    public <T> T accept(GeoJsonObjectVisitor<T> geoJsonObjectVisitor) {
        return geoJsonObjectVisitor.visit(this);
    }

    @Override
    public String toString() {
        return "Envelope{} " + super.toString();
    }
}
