package com.kayhut.fuse.model.logical;

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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.results.Property;

import java.util.Collection;
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
    public static final String EDGE = "Edge";
    private String id;
    private String label = EDGE;
    private String source;
    private String target;
    private boolean direction;
    private EdgeMetadata metadata = new EdgeMetadata();
    private EdgeProperties properties = new EdgeProperties();

    public LogicalEdge() {}

    public LogicalEdge(String id,String label,String source, String target,boolean direction) {
        this.id = id;
        this.label = label;
        this.source = source;
        this.target = target;
        this.direction = direction;
    }

    public LogicalEdge withMetadata(Collection<Property> properties) {
        properties.forEach(p->this.metadata.addProperties(p.getpType(),p.getValue()));
        return this;
    }

    public LogicalEdge withProperty(String property, Object value) {
        properties.addProperties(property,value);
        return this;
    }

    public String getLabel() {
        return label;
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

    @Override
    public String toString() {
        return "LogicalEdge{" +
                "id='" + id + '\'' +
                "label='" + label + '\'' +
                ", source='" + source + '\'' +
                ", target='" + target + '\'' +
                ", direction=" + direction +
                ", metadata=" + metadata +
                ", properties=" + properties +
                '}';
    }

    public static class EdgeMetadata {
        private Map<String,Object> properties;

        public EdgeMetadata() {
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
            return "EdgeMetadata{" +
                    ", properties=" + properties +
                    '}';
        }
    }

    public static class EdgeProperties{
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
            return "EdgeProperties{" +
                    "properties=" + properties +
                    '}';
        }
    }
}
