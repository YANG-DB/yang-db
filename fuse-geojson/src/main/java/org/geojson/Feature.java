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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

public class Feature extends GeoJsonObject {

	@JsonInclude(JsonInclude.Include.ALWAYS)
	private Map<String, Object> properties = new HashMap<String, Object>();
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private GeoJsonObject geometry;
	private String id;

	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T getProperty(String key) {
		return (T)properties.get(key);
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public GeoJsonObject getGeometry() {
		return geometry;
	}

	public void setGeometry(GeoJsonObject geometry) {
		this.geometry = geometry;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public <T> T accept(GeoJsonObjectVisitor<T> geoJsonObjectVisitor) {
		return geoJsonObjectVisitor.visit(this);
	}

	@Override public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		Feature feature = (Feature)o;
		if (properties != null ? !properties.equals(feature.properties) : feature.properties != null)
			return false;
		if (geometry != null ? !geometry.equals(feature.geometry) : feature.geometry != null)
			return false;
		return !(id != null ? !id.equals(feature.id) : feature.id != null);
	}

	@Override public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (properties != null ? properties.hashCode() : 0);
		result = 31 * result + (geometry != null ? geometry.hashCode() : 0);
		result = 31 * result + (id != null ? id.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Feature{properties=" + properties + ", geometry=" + geometry + ", id='" + id + "'}";
	}
}
