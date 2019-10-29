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

import java.util.ArrayList;
import java.util.List;

public abstract class Geometry<T> extends GeoJsonObject {

	protected List<T> coordinates = new ArrayList<T>();

	public Geometry() {
	}

	public Geometry(T... elements) {
		for (T coordinate : elements) {
			coordinates.add(coordinate);
		}
	}

	public Geometry<T> add(T elements) {
		coordinates.add(elements);
		return this;
	}

	public List<T> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<T> coordinates) {
		this.coordinates = coordinates;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Geometry)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		Geometry geometry = (Geometry)o;
		return !(coordinates != null ? !coordinates.equals(geometry.coordinates) : geometry.coordinates != null);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (coordinates != null ? coordinates.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Geometry{" + "coordinates=" + coordinates + "} " + super.toString();
	}
}
