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
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.results.Property;

import java.util.*;

/**
 * //example
 * {
 * "id": "0",
 * "label": "person",
 * "metadata": {
 * "user-defined": "values"
 * }
 * "properties":{
 * "fName": "first name",
 * "lName":"last name",
 * "born": "12/12/2000",
 * "age": "19",
 * "email": "myName@fuse.com",
 * "address": {
 * "state": "my state",
 * "street": "my street",
 * "city": "my city",
 * "zip": "gZip"
 * }
 * }
 * }
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogicalNode implements Vertex<LogicalNode> {
    public static final String NODE = "Node";

    @JsonProperty("label")
    private String label = NODE;
    @JsonProperty("id")
    private String id;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("metadata")
    private NodeMetadata metadata = new NodeMetadata();
    @JsonProperty("properties")
    private NodeProperties properties = new NodeProperties();

    public LogicalNode() {}

    public LogicalNode(String id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override
    @JsonProperty("label")
    public LogicalNode label(String label) {
        setLabel(label);
        return this;
    }

    @Override
    public LogicalNode tag(String tag) {
        setTag(tag);
        return this;
    }

    @Override
    @JsonProperty("tag")
    public String tag() {
        return getTag();
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

    @JsonProperty("metadata")
    public NodeMetadata getMetadata() {
        return metadata;
    }

    @JsonProperty("properties")
    public NodeProperties getProperties() {
        return properties;
    }

    @Override
    @JsonProperty("id")
    public String id() {
        return getId();
    }

    @Override
    @JsonProperty("label")
    public String label() {
        return getLabel();
    }

    @Override
    public LogicalNode merge(LogicalNode entity) {
        //todo merge
        return this;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("metadata")
    public void setMetadata(NodeMetadata metadata) {
        this.metadata = metadata;
    }

    @JsonProperty("properties")
    public void setProperties(NodeProperties properties) {
        this.properties = properties;
    }

    @Override
    @JsonProperty("metadata")
    public Map<String, Object> metadata() {
        return getMetadata().getProperties();
    }

    @Override
    @JsonProperty("properties")
    public Map<String, Object> fields() {
        return getProperties().getProperties();
    }

    @JsonIgnore
    public Object getProperty(String partition) {
        return getProperties().properties.get(partition);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                "label='" + label + '\'' +
                ", metadata=" + metadata +  '\'' +
                ", tag=" + tag +  '\'' +
                ", properties=" + properties +
                '}';
    }

    @JsonIgnore
    public LogicalNode withMetadata(Collection<Property> properties) {
        properties.forEach(p -> this.metadata.addProperties(p.getpType(), p.getValue()));
        return this;
    }

    @JsonIgnore
    public LogicalNode withProperty(String property, Object value) {
        properties.addProperties(property, value);
        return this;
    }

    @JsonIgnore
    public LogicalNode withProperty(EntityType type, String property, Object value) {
        if (type.containsProperty(property)) {
            properties.addProperties(property, value);
        } else {
            //add property with _underscore so it can be ignored if needed
            properties.addProperties(String.format("_%s", property), value);
        }
        return this;
    }

    @JsonIgnore
    public Optional<Object> getPropertyValue(String name) {
        return properties.properties.containsKey(name) ? Optional.of(properties.properties.get(name)) : Optional.empty();
    }

    public LogicalNode withMetadata(EntityType type, Collection<Property> properties) {
        properties.stream().filter(p -> type.containsProperty(p.getpType()))
                .forEach(p -> this.properties.addProperties(p.getpType(), p.getValue()));
        return this;
    }

    public LogicalNode withProperties(List<Property> properties) {
        properties.forEach(p -> this.properties.addProperties(p.getpType(), p.getValue()));
        return this;
    }

    public LogicalNode withTag(String tag) {
        this.tag = tag;
        return this;
    }


    public static class NodeMetadata implements PropertyFields<NodeMetadata> {
        private Map<String, Object> properties = new HashMap<>();


        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JsonAnySetter
        public NodeMetadata addProperties(String key, Object value) {
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

    public static class NodeProperties implements PropertyFields<NodeProperties> {
        private Map<String, Object> properties = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JsonAnySetter
        public NodeProperties addProperties(String key, Object value) {
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
