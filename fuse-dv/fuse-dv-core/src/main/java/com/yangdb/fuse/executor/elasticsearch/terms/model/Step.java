/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package com.yangdb.fuse.executor.elasticsearch.terms.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreRequest;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.VertexRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ValidateActions;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContentFragment;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Step represents one of potentially many stages in a graph exploration.
 * Each Step identifies one or more fields in which it will attempt to find
 * terms that are significantly connected to the previous Step. Each field is identified
 * using a {@link VertexRequest}
 *
 * <p>An example series of Hops on webserver logs would be:
 * <ol>
 * <li>an initial Step to find
 * the top ten IPAddresses trying to access urls containing the word "admin"</li>
 * <li>a secondary Step to see which other URLs those IPAddresses were trying to access</li>
 * </ol>
 *
 * <p>
 * Optionally, each hop can contain a "guiding query" that further limits the set of documents considered.
 * In our weblog example above we might choose to constrain the second hop to only look at log records that
 * had a response code of 404.
 * </p>
 * <p>
 * If absent, the list of {@link VertexRequest}s is inherited from the prior Step's list to avoid repeating
 * the fields that will be examined at each stage.
 * </p>
 *
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Step {
    @JsonProperty("parentStep")
    public final Step parentStep;
    @JsonProperty("vertices")
    public List<VertexRequest> vertices = null;
    public QueryBuilder guidingQuery = null;

    public Step(Step parent) {
        this.parentStep = parent;
    }

    @JsonProperty("vertices")
    public List<VertexRequest> getVertices() {
        return vertices;
    }

    @JsonProperty("vertices")
    public void setVertices(List<VertexRequest> vertices) {
        this.vertices = vertices;
    }

    public ActionRequestValidationException validate(ActionRequestValidationException validationException) {

        if (getEffectiveVertexRequests().size() == 0) {
            validationException = ValidateActions.addValidationError(GraphExploreRequest.NO_VERTICES_ERROR_MESSAGE, validationException);
        }
        return validationException;

    }

    public Step getParentStep() {
        return parentStep;
    }


    public QueryBuilder guidingQuery() {
        if (guidingQuery != null) {
            return guidingQuery;
        }
        return QueryBuilders.matchAllQuery();
    }

    /**
     * Add a field in which this {@link Step} will look for terms that are highly linked to
     * previous hops and optionally the guiding query.
     *
     * @param fieldName a field in the chosen index
     */
    public VertexRequest addVertexRequest(String fieldName) {
        if (vertices == null) {
            vertices = new ArrayList<>();
        }
        VertexRequest vr = new VertexRequest();
        vr.fieldName(fieldName);
        vertices.add(vr);
        return vr;
    }

    /**
     * An optional parameter that focuses the exploration on documents that
     * match the given query.
     *
     * @param queryBuilder any query
     */
    public void guidingQuery(QueryBuilder queryBuilder) {
        guidingQuery = queryBuilder;
    }

    protected List<VertexRequest> getEffectiveVertexRequests() {
        if (vertices != null) {
            return vertices;
        }
        if (parentStep == null) {
            return Collections.emptyList();
        }
        // otherwise inherit settings from parent
        return parentStep.getEffectiveVertexRequests();
    }

    public int getNumberVertexRequests() {
        return getEffectiveVertexRequests().size();
    }

    public VertexRequest getVertexRequest(int requestNumber) {
        return getEffectiveVertexRequests().get(requestNumber);
    }

}
