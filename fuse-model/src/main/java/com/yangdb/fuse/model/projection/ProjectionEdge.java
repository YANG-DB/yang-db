package com.yangdb.fuse.model.projection;

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
import com.yangdb.fuse.model.logical.Edge;
import com.yangdb.fuse.model.logical.PropertyFields;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.results.Property;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectionEdge implements Edge {
    public static final String EDGE = "Edge";
    @JsonProperty("id")
    private String id;
    @JsonProperty("label")
    private String tag;
    @JsonProperty("tag")
    private String label = EDGE;
    @JsonProperty("target")
    private String targetLabel;
    @JsonProperty("targetId")
    private String targetId;
    @JsonProperty("direction")
    private boolean direction;
    @JsonProperty("metadata")
    private EdgeMetadata metadata = new EdgeMetadata();
    @JsonProperty("properties")
    private EdgeProperties properties = new EdgeProperties();

    public ProjectionEdge() {
    }

    public ProjectionEdge(String id, String label, String targetLabel, String targetId, boolean direction) {
        this(id,null,label,targetLabel,targetId,direction);
    }

    public ProjectionEdge(String id, String tag, String label, String targetLabel, String targetId, boolean directional) {
        this.id = id;
        this.tag = tag;
        this.label = label;
        this.targetLabel = targetLabel;
        this.targetId = targetId;
        this.direction = directional;

    }

    public ProjectionEdge withMetadata(Collection<Property> properties) {
        properties.forEach(p -> this.metadata.addProperties(p.getpType(), p.getValue()));
        return this;
    }

    public ProjectionEdge withProperty(String property, Object value) {
        properties.addProperties(property, value);
        return this;
    }

    @JsonIgnore
    public ProjectionEdge withProperty(RelationshipType type, String property, Object value) {
        if (type.containsProperty(property)) {
            properties.addProperties(property, value);
        } else {
            //add property with _underscore so it can be ignored if needed
            properties.addProperties(String.format("_%s", property), value);
        }
        return this;
    }

    @JsonProperty("tag")
    public String getTag() {
        return tag;
    }

    @JsonProperty("tag")
    public void setTag(String tag) {
        this.tag = tag;
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("target")
    public String getTargetLabel() {
        return targetLabel;
    }

    @JsonProperty("targetId")
    public String getTargetId() {
        return targetId;
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

    @JsonProperty("target")
    public void setTargetLabel(String targetLabel) {
        this.targetLabel = targetLabel;
    }

    @JsonProperty("targetId")
    public void setTargetId(String targetId) {
        this.targetId = targetId;
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
                "tag='" + tag + '\'' +
                ", target='" + targetLabel + '\'' +
                ", targetId='" + targetId + '\'' +
                ", direction=" + direction +
                ", metadata=" + metadata +
                ", properties=" + properties +
                '}';
    }

    @Override
    @JsonProperty("id")
    public String id() {
        return getId();
    }

    @Override
    public String tag() {
        return getTag();
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
        return "";
    }

    @Override
    @JsonProperty("target")
    public String target() {
        return getTargetLabel();
    }

    @JsonIgnore
    public Object getProperty(String partition) {
        return getProperties().properties.get(partition);
    }

    public ProjectionEdge withMetadata(RelationshipType type, List<Property> properties) {
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
