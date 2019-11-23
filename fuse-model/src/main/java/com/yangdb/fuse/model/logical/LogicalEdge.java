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
import com.yangdb.fuse.model.results.Property;

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
public class LogicalEdge implements Edge {
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
        return getClass().getSimpleName()+"{" +
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
    public String id() {
        return getId()!=null ? id : String.format("%s.%s",source,target);
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
    public String source() {
        return getSource();
    }

    @Override
    public String target() {
        return getTarget();
    }

    @JsonIgnore
    public Object getProperty(String partition) {
        return getProperties().properties.get(partition);
    }

    public static class EdgeMetadata implements PropertyFields<EdgeMetadata> {
        private Map<String,Object> properties;

        public EdgeMetadata() {
            this.properties = new HashMap<>();
        }

        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JsonAnySetter
        public EdgeMetadata addProperties(String key, Object value) {
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

    public static class EdgeProperties implements PropertyFields<EdgeProperties>{
        private Map<String,Object> properties = new HashMap<>();

        @JsonAnyGetter
        public Map<String, Object> getProperties() {
            return properties;
        }

        @JsonAnySetter
        public EdgeProperties addProperties(String key, Object value) {
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
