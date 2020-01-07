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
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.results.CsvQueryResult;
import com.yangdb.fuse.model.results.Property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * //example
 *             {
 *                 "id": "0",
 *                 "label": "person",
 *                 "metadata": {
 *                     "user-defined": "values"
 *                 }
 *                 "properties":{
 *                     "fName": "first name",
 *                     "lName":"last name",
 *                     "born": "12/12/2000",
 *                     "age": "19",
 *                     "email": "myName@fuse.com",
 *                     "address": {
 *                             "state": "my state",
 *                             "street": "my street",
 *                             "city": "my city",
 *                             "zip": "gZip"
 *                     }
 *                 }
 *             }
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogicalNode implements Vertex {
    public static final String NODE = "Node";

    @JsonProperty("label")
    private String label = NODE;
    @JsonProperty("id")
    private String id;

    @JsonProperty("metadata")
    private NodeMetadata metadata = new NodeMetadata();
    @JsonProperty("properties")
    private NodeProperties properties = new NodeProperties(); ;

    public LogicalNode() {}

    public LogicalNode(String id,String label) {
        this.id = id;
        this.label = label;
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
        return getClass().getSimpleName()+"{" +
                "id='" + id + '\'' +
                "label='" + label + '\'' +
                ", metadata=" + metadata +
                ", properties=" + properties +
                '}';
    }

    @JsonIgnore
    public LogicalNode withMetadata(Collection<Property> properties) {
        properties.forEach(p->this.metadata.addProperties(p.getpType(),p.getValue()));
        return this;
    }

    @JsonIgnore
    public LogicalNode withProperty(String property, Object value) {
        properties.addProperties(property,value);
        return this;
    }

    @JsonIgnore
    public Optional<Object> getPropertyValue(String name) {
        return properties.properties.containsKey(name) ? Optional.of(properties.properties.get(name)) : Optional.empty();
    }

    public static class NodeMetadata implements PropertyFields<NodeMetadata> {
        private Map<String,Object> properties = new HashMap<>();


        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JsonAnySetter
        public NodeMetadata addProperties(String key, Object value) {
            this.properties.put(key,value);
            return this;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName()+"{" +
                    ", properties=" + properties +
                    '}';
        }
    }

    public static class NodeProperties implements PropertyFields<NodeProperties>{
        private Map<String,Object> properties = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JsonAnySetter
        public NodeProperties addProperties(String key, Object value) {
            this.properties.put(key,value);
            return this;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName()+"{" +
                    "properties=" + properties +
                    '}';
        }
    }
}
