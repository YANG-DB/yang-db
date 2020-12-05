/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package com.yangdb.fuse.executor.elasticsearch.terms.transport;

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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yangdb.fuse.executor.elasticsearch.terms.model.Edge;
import com.yangdb.fuse.executor.elasticsearch.terms.model.Edge.EdgeId;
import com.yangdb.fuse.executor.elasticsearch.terms.model.Vertex;
import com.yangdb.fuse.executor.elasticsearch.terms.model.Vertex.VertexId;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.unit.TimeValue;

import java.util.Collection;
import java.util.Map;

/**
 * Graph explore response holds a graph of {@link Vertex} and {@link Edge} objects
 * (nodes and edges in common graph parlance).
 *
 * @see GraphExploreRequest
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphExploreResponse {
    public static final String RETURN_DETAILED_INFO_PARAM = "returnDetailedInfo";

    @JsonProperty("tookInMillis")
    private long tookInMillis;
    @JsonProperty("timedOut")
    private boolean timedOut = false;
    @JsonProperty("shardFailures")
    private ShardOperationFailedException[] shardFailures = ShardSearchFailure.EMPTY_ARRAY;

    @JsonProperty("vertices")
    @JsonSerialize(keyUsing = VertexId.Serializer.class)
    @JsonDeserialize(using = Vertex.Deserializer.class)
    private Map<VertexId, Vertex> vertices;

    @JsonProperty("edgesMap")
    @JsonSerialize(keyUsing = EdgeId.Serializer.class)
    @JsonDeserialize(using = Edge.Deserializer.class)
    private Map<EdgeId, Edge> edgesMap;

    @JsonProperty("returnDetailedInfo")
    private boolean returnDetailedInfo;

    public GraphExploreResponse() {}

    public GraphExploreResponse(long tookInMillis, boolean timedOut, ShardOperationFailedException[] shardFailures,
                                Map<VertexId, Vertex> vertices, Map<EdgeId, Edge> edgesMap, boolean returnDetailedInfo) {
        this.tookInMillis = tookInMillis;
        this.timedOut = timedOut;
        this.shardFailures = shardFailures;
        this.vertices = vertices;
        this.edgesMap = edgesMap;
        this.returnDetailedInfo = returnDetailedInfo;
    }


    @JsonProperty("tookInMillis")
    public void setTookInMillis(long tookInMillis) {
        this.tookInMillis = tookInMillis;
    }

    @JsonProperty("timedOut")
    public void setTimedOut(boolean timedOut) {
        this.timedOut = timedOut;
    }

    @JsonProperty("shardFailures")
    public void setShardFailures(ShardOperationFailedException[] shardFailures) {
        this.shardFailures = shardFailures;
    }

    @JsonProperty("vertices")
    public void setVertices(Map<VertexId, Vertex> vertices) {
        this.vertices = vertices;
    }

    @JsonProperty("edgesMap")
    public void setEdgesMap(Map<EdgeId, Edge> edgesMap) {
        this.edgesMap = edgesMap;
    }

    @JsonProperty("returnDetailedInfo")
    public boolean isReturnDetailedInfo() {
        return returnDetailedInfo;
    }

    @JsonProperty("returnDetailedInfo")
    public void setReturnDetailedInfo(boolean returnDetailedInfo) {
        this.returnDetailedInfo = returnDetailedInfo;
    }

    @JsonProperty("tookInMillis")
    public long getTookInMillis() {
        return tookInMillis;
    }

    /**
     * @return true if the time stated in {@link GraphExploreRequest#timeout(TimeValue)} was exceeded
     * (not all hops may have been completed in this case)
     */
    @JsonProperty("timedOut")
    public boolean isTimedOut() {
        return this.timedOut;
    }

    @JsonProperty("shardFailures")
    public ShardOperationFailedException[] getShardFailures() {
        return shardFailures;
    }

    @JsonProperty("edgesMap")
    public Collection<Edge> getEdgesMap() {
        return edgesMap.values();
    }

    @JsonProperty("vertices")
    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    public TimeValue getTook() {
        return new TimeValue(tookInMillis);
    }

    public Vertex getVertex(VertexId id) {
        return vertices.get(id);
    }

}
