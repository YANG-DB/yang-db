/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package com.yangdb.fuse.executor.elasticsearch.terms.model;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;

/**
 * A Edge links exactly two {@link Vertex} objects. The basis of a
 * connection is one or more documents have been found that contain
 * this pair of terms and the strength of the connection is recorded
 * as a weight.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = Edge.Deserializer.class)
public class Edge {
    private Vertex from;
    private Vertex to;
    private double weight;
    private long docCount;

    public Edge() {}

    public Edge(Vertex from, Vertex to, double weight, long docCount) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.docCount = docCount;
    }

    public EdgeId getId() {
        return new EdgeId(from.getId(), to.getId());
    }

    public Vertex getFrom() {
        return from;
    }

    public Vertex getTo() {
        return to;
    }

    /**
     * @return a measure of the relative connectedness between a pair of {@link Vertex} objects
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @return the number of documents in the sampled set that contained this
     * pair of {@link Vertex} objects.
     */
    public long getDocCount() {
        return docCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Edge other = (Edge) obj;
        return docCount == other.docCount &&
                weight == other.weight &&
                Objects.equals(to, other.to) &&
                Objects.equals(from, other.from);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docCount, weight, from, to);
    }

    /**
     * An identifier (implements hashcode and equals) that represents a
     * unique key for a {@link Edge}
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(keyUsing = EdgeId.Serializer.class)
    public static class EdgeId {
        @JsonProperty("source")
        private final Vertex.VertexId source;
        @JsonProperty("target")
        private final Vertex.VertexId target;

        public EdgeId(Vertex.VertexId source, Vertex.VertexId target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            EdgeId vertexId = (EdgeId) o;

            if (source != null ? !source.equals(vertexId.source) : vertexId.source != null)
                return false;
            if (target != null ? !target.equals(vertexId.target) : vertexId.target != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = source != null ? source.hashCode() : 0;
            result = 31 * result + (target != null ? target.hashCode() : 0);
            return result;
        }

        public Vertex.VertexId getSource() {
            return source;
        }

        public Vertex.VertexId getTarget() {
            return target;
        }

        @Override
        @JsonValue
        public String toString() {
            return getSource() + "->" + getTarget();
        }

        public static class Serializer extends JsonSerializer<EdgeId> {
            private ObjectMapper mapper = new ObjectMapper();

            @Override
            public void serialize(EdgeId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                StringWriter writer = new StringWriter();
                mapper.writeValue(writer, value);
                gen.writeFieldName(writer.toString());
            }
        }
    }

    @JsonDeserialize
    public static class Deserializer extends JsonDeserializer<Edge> {

        @Override
        public Edge deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return null;
        }
    }
}
