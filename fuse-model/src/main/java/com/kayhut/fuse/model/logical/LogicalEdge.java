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
 *                 "id": 101,
 *                 "source": "0",
 *                 "target": "1",
 *                 "metadata": {
 *                     "label": "called",
 *                     "user-defined": "values"
 *                 },
 *                 "properties":{
 *                      "date":"01/01/2000",
 *                      "duration":"120",
 *                      "medium": "cellular"
 *                      "sourceLocation": "40.06,-71.34"
 *                      "sourceTarget": "41.12,-70.9"
 *                 }
 *             }
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogicalEdge {
    private String id;
    private String source;
    private String target;
    private boolean direction;
    private EdgeMetadata metadata;
    private EdgeProperties properties;

    public LogicalEdge() {}

    public LogicalEdge(String id,String source, String target,boolean direction) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.direction = direction;
        this.metadata = new EdgeMetadata();
        this.properties = new EdgeProperties();
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public boolean isDirection() {
        return direction;
    }

    public EdgeMetadata getMetadata() {
        return metadata;
    }

    public EdgeProperties getProperties() {
        return properties;
    }

    public static class EdgeMetadata {
        private String label;
        private Map<String,Object> properties;

        public EdgeMetadata() {
            this.properties = new HashMap<>();
        }

        public EdgeMetadata(String label) {
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
    }

    public static class EdgeProperties{
        private Map<String,Object> properties;

        public EdgeProperties() {
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
    }
}
