package com.yangdb.fuse.model.logical;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

    private String id;
    private String label = NODE;
    private NodeMetadata metadata = new NodeMetadata();
    private NodeProperties properties = new NodeProperties(); ;

    public LogicalNode() {}

    public LogicalNode(String id,String label) {
        this.id = id;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getId() {
        return id;
    }

    public NodeMetadata getMetadata() {
        return metadata;
    }

    public NodeProperties getProperties() {
        return properties;
    }

    @Override
    public String id() {
        return getId();
    }

    @Override
    public String label() {
        return getLabel();
    }

    @Override
    public Map<String, Object> metadata() {
        return getMetadata().getProperties();
    }

    @Override
    public Map<String, Object> fields() {
        return getProperties().getProperties();
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

    public static class NodeMetadata {
        private Map<String,Object> properties = new HashMap<>();


        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JsonAnySetter
        public void addProperties(String key, Object value) {
            this.properties.put(key,value);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName()+"{" +
                    ", properties=" + properties +
                    '}';
        }
    }

    public static class NodeProperties{
        private Map<String,Object> properties = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JsonAnySetter
        public void addProperties(String key, Object value) {
            this.properties.put(key,value);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName()+"{" +
                    "properties=" + properties +
                    '}';
        }
    }
}
