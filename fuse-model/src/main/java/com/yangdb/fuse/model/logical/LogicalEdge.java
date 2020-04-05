package com.yangdb.fuse.model.logical;

/*-
 * #%L
 * fuse-model
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

import com.fasterxml.jackson.annotation.*;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.results.Property;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * //example
 * {
 * "id": 101,
 * "source": "0",
 * "target": "1",
 * "metadata": {
 * "label": "called",
 * "user-defined": "values"
 * },
 * "properties":{
 * "date":"01/01/2000",
 * "duration":"120",
 * "medium": "cellular"
 * "sourceLocation": "40.06,-71.34"
 * "sourceTarget": "41.12,-70.9"
 * }
 * }
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogicalEdge implements Edge {
    public static final String EDGE = "Edge";
    @JsonProperty("id")
    private String id;
    @JsonProperty("label")
    private String label = EDGE;
    @JsonProperty("source")
    private String source;
    @JsonProperty("target")
    private String target;
    @JsonProperty("direction")
    private boolean direction;
    @JsonProperty("metadata")
    private EdgeMetadata metadata = new EdgeMetadata();
    @JsonProperty("properties")
    private EdgeProperties properties = new EdgeProperties();

    public LogicalEdge() {
    }

    public LogicalEdge(String id, String label, String source, String target, boolean direction) {
        this.id = id;
        this.label = label;
        this.source = source;
        this.target = target;
        this.direction = direction;
    }

    public LogicalEdge withMetadata(Collection<Property> properties) {
        properties.forEach(p -> this.metadata.addProperties(p.getpType(), p.getValue()));
        return this;
    }

    public LogicalEdge withProperty(String property, Object value) {
        properties.addProperties(property, value);
        return this;
    }

    @JsonIgnore
    public LogicalEdge withProperty(RelationshipType type, String property, Object value) {
        if (type.containsProperty(property)) {
            properties.addProperties(property, value);
        } else {
            //add property with _underscore so it can be ignored if needed
            properties.addProperties(String.format("_%s", property), value);
        }
        return this;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    @JsonProperty("target")
    public String getTarget() {
        return target;
    }

    @JsonProperty("direction")
    public boolean isDirection() {
        return direction;
    }

    @JsonProperty("metadata")
    public EdgeMetadata getMetadata() {
        return metadata;
    }

    @JsonProperty("properties")
    public EdgeProperties getProperties() {
        return properties;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("source")
    public void setSource(String source) {
        this.source = source;
    }

    @JsonProperty("target")
    public void setTarget(String target) {
        this.target = target;
    }

    @JsonProperty("direction")
    public void setDirection(boolean direction) {
        this.direction = direction;
    }

    @JsonProperty("metadata")
    public void setMetadata(EdgeMetadata metadata) {
        this.metadata = metadata;
    }

    @JsonProperty("properties")
    public void setProperties(EdgeProperties properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                "label='" + label + '\'' +
                ", source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", direction=" + direction +
                ", metadata=" + metadata +
                ", properties=" + properties +
                '}';
    }

    @Override
    @JsonProperty("id")
    public String id() {
        return getId() != null ? id : String.format("%s.%s", source, target);
    }

    @Override
    @JsonProperty("label")
    public String label() {
        return getLabel();
    }

    @Override
    @JsonProperty("metadata")
    public Map<String, Object> metadata() {
        return getMetadata().getProperties();
    }

    @Override
    @JsonIgnore
    public Map<String, Object> fields() {
        return getProperties().getProperties();
    }

    @Override
    @JsonProperty("source")
    public String source() {
        return getSource();
    }

    @Override
    @JsonProperty("target")
    public String target() {
        return getTarget();
    }

    @JsonIgnore
    public Object getProperty(String partition) {
        return getProperties().properties.get(partition);
    }

    public LogicalEdge withMetadata(RelationshipType type, List<Property> properties) {
        properties.addAll(properties.stream().filter(p -> type.containsProperty(p.getpType())).collect(Collectors.toSet()));
        return this;
    }

    public static class EdgeMetadata implements PropertyFields<EdgeMetadata> {
        private Map<String, Object> properties;

        public EdgeMetadata() {
            this.properties = new HashMap<>();
        }

        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JsonAnySetter
        public EdgeMetadata addProperties(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    ", properties=" + properties +
                    '}';
        }
    }

    public static class EdgeProperties implements PropertyFields<EdgeProperties> {
        private Map<String, Object> properties = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JsonAnySetter
        public EdgeProperties addProperties(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "properties=" + properties +
                    '}';
        }
    }
}
