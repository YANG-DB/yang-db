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

/**
 * Visitor to handle all different types of {@link GeoJsonObject}.
 * 
 * @param <T>
 *            return type of the visitor.
 */
public interface GeoJsonObjectVisitor<T> {

	T visit(GeometryCollection geoJsonObject);

	T visit(FeatureCollection geoJsonObject);

	T visit(Point geoJsonObject);

	T visit(Feature geoJsonObject);

	T visit(MultiLineString geoJsonObject);

	T visit(Polygon geoJsonObject);

	T visit(MultiPolygon geoJsonObject);

	T visit(MultiPoint geoJsonObject);

	T visit(LineString geoJsonObject);

	T visit(Circle geoJsonObject);

	T visit(Envelope geoJsonObject);

	/**
	 * An abstract adapter class for visiting GeoJson objects.
	 * The methods in this class are empty.
	 * This class exists as convenience for creating listener objects.
	 *
	 * @param <T> Return type of the visitor
   */
	class Adapter<T> implements GeoJsonObjectVisitor<T> {

		@Override
		public T visit(GeometryCollection geoJsonObject) {
			return null;
		}

		@Override
		public T visit(FeatureCollection geoJsonObject) {
			return null;
		}

		@Override
		public T visit(Point geoJsonObject) {
			return null;
		}

		@Override
		public T visit(Feature geoJsonObject) {
			return null;
		}

		@Override
		public T visit(MultiLineString geoJsonObject) {
			return null;
		}

		@Override
		public T visit(Polygon geoJsonObject) {
			return null;
		}

		@Override
		public T visit(MultiPolygon geoJsonObject) {
			return null;
		}

		@Override
		public T visit(MultiPoint geoJsonObject) {
			return null;
		}

		@Override
		public T visit(LineString geoJsonObject) {
			return null;
		}

		@Override
		public T visit(Circle geoJsonObject) { return null; }

		@Override
		public T visit(Envelope geoJsonObject) { return null; }
	}
}
