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

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentFragment;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;

/**
 * A vertex in a graph response represents a single term (a field and value pair)
 * which appears in one or more documents found as part of the graph exploration.
 * <p>
 * A vertex term could be a bank account number, an email address, a hashtag or any
 * other term that appears in documents and is interesting to represent in a network.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = Vertex.Deserializer.class)
public class Vertex  {

    private String field;
    private String term;
    private double weight;
    private int depth;
    private long bg;
    private long fg;

    public Vertex() {}

    @JsonCreator
    public Vertex(String field, String term, double weight, int depth, long bg, long fg) {
        super();
        this.field = field;
        this.term = term;
        this.weight = weight;
        this.depth = depth;
        this.bg = bg;
        this.fg = fg;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, term, weight, depth, bg, fg);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vertex other = (Vertex) obj;
        return depth == other.depth &&
                weight == other.weight &&
                bg == other.bg &&
                fg == other.fg &&
                Objects.equals(field, other.field) &&
                Objects.equals(term, other.term);

    }

    /**
     * @return a {@link VertexId} object that uniquely identifies this Vertex
     */
    public VertexId getId() {
        return createId(field, term);
    }

    /**
     * A convenience method for creating a {@link VertexId}
     *
     * @param field the field
     * @param term  the term
     * @return a {@link VertexId} that can be used for looking up vertices
     */
    public static VertexId createId(String field, String term) {
        return new VertexId(field, term);
    }

    @Override
    public String toString() {
        return getId().toString();
    }

    public String getField() {
        return field;
    }

    public String getTerm() {
        return term;
    }

    /**
     * The weight of a vertex is an accumulation of all of the {@link Edge}s
     * that are linked to this {@link Vertex} as part of a graph exploration.
     * It is used internally to identify the most interesting vertices to be returned.
     *
     * @return a measure of the {@link Vertex}'s relative importance.
     */
    public double getWeight() {
        return weight;
    }

    public void setWeight(final double weight) {
        this.weight = weight;
    }

    /**
     * If the {@link GraphExploreRequest#useSignificance(boolean)} is true (the default)
     * this statistic is available.
     *
     * @return the number of documents in the index that contain this term (see bg_count in
     * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-significantterms-aggregation.html">
     * the significant_terms aggregation</a>)
     */
    public long getBg() {
        return bg;
    }

    /**
     * If the {@link GraphExploreRequest#useSignificance(boolean)} is true (the default)
     * this statistic is available.
     * Together with {@link #getBg()} these numbers are used to derive the significance of a term.
     *
     * @return the number of documents in the sample of best matching documents that contain this term (see fg_count in
     * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-significantterms-aggregation.html">
     * the significant_terms aggregation</a>)
     */
    public long getFg() {
        return fg;
    }

    public void setFg(final long fg) {
        this.fg = fg;
    }

    /**
     * @return the sequence number in the series of hops where this Vertex term was first encountered
     */
    public int getStepsDepth() {
        return depth;
    }

    /**
     * An identifier (implements hashcode and equals) that represents a
     * unique key for a {@link Vertex}
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(keyUsing = VertexId.Serializer.class)
    public static class VertexId {
        @JsonProperty("field")
        private final String field;
        @JsonProperty("term")
        private final String term;

        public VertexId(String field, String term) {
            this.field = field;
            this.term = term;
        }

        public String getField() {
            return field;
        }

        public String getTerm() {
            return term;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            VertexId vertexId = (VertexId) o;

            if (field != null ? !field.equals(vertexId.field) : vertexId.field != null)
                return false;
            if (term != null ? !term.equals(vertexId.term) : vertexId.term != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = field != null ? field.hashCode() : 0;
            result = 31 * result + (term != null ? term.hashCode() : 0);
            return result;
        }

        @Override
        @JsonValue
        public String toString() {
            return field + ":" + term;
        }

        public static class Serializer extends JsonSerializer<VertexId> {
            private ObjectMapper mapper = new ObjectMapper();

            @Override
            public void serialize(Vertex.VertexId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                StringWriter writer = new StringWriter();
                mapper.writeValue(writer, value);
                gen.writeFieldName(writer.toString());
            }
        }
    }

    public static class Deserializer extends JsonDeserializer<Vertex> {
        public Deserializer() {
        }

        @Override
        public Vertex deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (p.isExpectedStartArrayToken()) {
                p.nextToken();//move past the start object array token
                return deserializeArray(p, ctxt);
            }
            throw ctxt.mappingException("No appropriate content was found to parse accordingly ");
        }

        private Vertex deserializeArray(JsonParser p, DeserializationContext ctxt) throws IOException {
            String field = extractString(p, ctxt, false);
            String term = extractString(p, ctxt, false);
            double weight = extractDouble(p, ctxt, false);
            int depth = (int) extractLong(p, ctxt, false);
            long bg = extractLong(p, ctxt, false);
            long fg = extractLong(p, ctxt, false);
            if (p.hasCurrentToken() && p.getCurrentToken() != JsonToken.END_ARRAY) {
                p.nextToken();
            }
            return new Vertex(field, term, weight, depth, bg, fg);
        }

        private long extractLong(JsonParser jp, DeserializationContext ctxt, boolean optional) throws IOException {
            JsonToken token = jp.nextToken();
            if (token == null) {
                if (optional)
                    return 0;
                else
                    throw ctxt.mappingException("Unexpected end-of-input when binding data into Vertex");
            } else {
                if (token.equals(JsonToken.FIELD_NAME)) {
                    token = jp.nextToken();
                    switch (token) {
                        case END_ARRAY:
                            if (optional)
                                return 0;
                            else
                                throw ctxt.mappingException("Unexpected end-of-input when binding data into Vertex");
                        case VALUE_NUMBER_INT:
                            return jp.getLongValue();
                        default:
                            throw ctxt.mappingException("Unexpected token (" + token.name()
                                    + ") when binding data into Vertex ");
                    }
                }
            }
            throw ctxt.mappingException("Unexpected token (" + token.name()
                    + ") when binding data into Vertex ");
        }


        private String extractString(JsonParser jp, DeserializationContext ctxt, boolean optional) throws IOException {
            JsonToken token = jp.nextToken();
            if (token == null) {
                if (optional)
                    return "";
                else
                    throw ctxt.mappingException("Unexpected end-of-input when binding data into Vertex");
            } else {
                if (token.equals(JsonToken.FIELD_NAME)) {
                    token = jp.nextToken();
                    switch (token) {
                        case END_ARRAY:
                            if (optional)
                                return "0";
                            else
                                throw ctxt.mappingException("Unexpected end-of-input when binding data into Vertex");
                        case VALUE_STRING:
                            return jp.getText();
                        default:
                            throw ctxt.mappingException("Unexpected token (" + token.name()
                                    + ") when binding data into Vertex ");
                    }
                }
            }
            throw ctxt.mappingException("Unexpected token (" + token.name()
                    + ") when binding data into Vertex ");
        }

        private double extractDouble(JsonParser jp, DeserializationContext ctxt, boolean optional)
                throws JsonParseException, IOException {
            JsonToken token = jp.nextToken();
            if (token == null) {
                if (optional)
                    return Double.NaN;
                else
                    throw ctxt.mappingException("Unexpected end-of-input when binding data into Vertex");
            } else {
                if (token.equals(JsonToken.FIELD_NAME)) {
                    token = jp.nextToken();
                    switch (token) {
                        case END_ARRAY:
                            if (optional)
                                return Double.NaN;
                            else
                                throw ctxt.mappingException("Unexpected end-of-input when binding data into Vertex");
                        case VALUE_NUMBER_FLOAT:
                            return jp.getDoubleValue();
                        case VALUE_NUMBER_INT:
                            return jp.getLongValue();
                        default:
                            throw ctxt.mappingException("Unexpected token (" + token.name()
                                    + ") when binding data into Vertex ");
                    }
                }
            }
            throw ctxt.mappingException("Unexpected token (" + token.name()
                    + ") when binding data into Vertex ");
        }
    }
}
