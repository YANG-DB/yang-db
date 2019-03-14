package com.kayhut.fuse.model.logical;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

/**
 * //example
 *             {
 *                 "id": "0",
 *                 "metadata": {
 *                     "label": "person",
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
public class LogicalNode {
    private String id;
    private NodeMetadata metadata;
    private NodeProperties properties;

    public LogicalNode() {}

    public LogicalNode(String id) {
        this.id = id;
        this.metadata = new NodeMetadata();
        this.properties = new NodeProperties();
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
    public String toString() {
        return "LogicalNode{" +
                "id='" + id + '\'' +
                ", metadata=" + metadata +
                ", properties=" + properties +
                '}';
    }

    public static class NodeMetadata {
        private String label;
        private Map<String,Object> properties;

        public NodeMetadata() {
            this.properties = new HashMap<>();
        }

        public NodeMetadata(String label) {
            this();
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

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
            return "NodeMetadata{" +
                    "label='" + label + '\'' +
                    ", properties=" + properties +
                    '}';
        }
    }

    public static class NodeProperties{
        private Map<String,Object> properties;

        public NodeProperties() {
            this.properties = new HashMap<>();
        }

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
            return "NodeProperties{" +
                    "properties=" + properties +
                    '}';
        }
    }
}
