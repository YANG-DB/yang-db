/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package com.yangdb.fuse.executor.elasticsearch.terms.transport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yangdb.fuse.executor.elasticsearch.terms.model.Step;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreRequest.TermBoost;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A request to identify terms from a choice of field as part of a {@link Step}.
 * Optionally, a set of terms can be provided that are used as an exclusion or
 * inclusion list to filter which terms are considered.
 *
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VertexRequest {
    public static final int DEFAULT_SIZE = 5;
    public static final int DEFAULT_MIN_DOC_COUNT = 3;
    public static final int DEFAULT_SHARD_MIN_DOC_COUNT = 2;

    @JsonProperty("fieldName")
    private String fieldName;
    @JsonProperty("size")
    private int size = DEFAULT_SIZE;
    @JsonProperty("includes")
    private Map<String, TermBoost> includes;
    @JsonProperty("excludes")
    private Set<String> excludes;
    @JsonProperty("minDocCount")
    private int minDocCount = DEFAULT_MIN_DOC_COUNT;
    @JsonProperty("shardMinDocCount")
    private int shardMinDocCount = DEFAULT_SHARD_MIN_DOC_COUNT;

    @JsonProperty("fieldName")
    public String getFieldName() {
        return fieldName;
    }

    @JsonProperty("fieldName")
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @JsonProperty("size")
    public int getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(int size) {
        this.size = size;
    }

    @JsonProperty("includes")
    public Map<String, TermBoost> getIncludes() {
        return includes;
    }

    @JsonProperty("includes")
    public void setIncludes(Map<String, TermBoost> includes) {
        this.includes = includes;
    }

    @JsonProperty("excludes")
    public Set<String> getExcludes() {
        return excludes;
    }

    @JsonProperty("excludes")
    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    @JsonProperty("minDocCount")
    public int getMinDocCount() {
        return minDocCount;
    }

    @JsonProperty("minDocCount")
    public void setMinDocCount(int minDocCount) {
        this.minDocCount = minDocCount;
    }

    @JsonProperty("shardMinDocCount")
    public int getShardMinDocCount() {
        return shardMinDocCount;
    }

    @JsonProperty("shardMinDocCount")
    public void setShardMinDocCount(int shardMinDocCount) {
        this.shardMinDocCount = shardMinDocCount;
    }

    public VertexRequest() {}

    public String fieldName() {
        return fieldName;
    }

    public VertexRequest fieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public int size() {
        return size;
    }

    /**
     * @param size The maximum number of terms that should be returned from this field as part of this {@link Step}
     */
    public VertexRequest size(int size) {
        this.size = size;
        return this;
    }

    public boolean hasIncludeClauses() {
        return includes != null && includes.size() > 0;
    }

    public boolean hasExcludeClauses() {
        return excludes != null && excludes.size() > 0;
    }

    /**
     * Adds a term that should be excluded from results
     * @param term A term to be excluded
     */
    public void addExclude(String term) {
        if (includes != null) {
            throw new IllegalArgumentException("Cannot have both include and exclude clauses");
        }
        if (excludes == null) {
            excludes = new HashSet<>();
        }
        excludes.add(term);
    }

    /**
     * Adds a term to the set of allowed values - the boost defines the relative
     * importance when pursuing connections in subsequent {@link Step}s. The boost value
     * appears as part of the query.
     * @param term a required term
     * @param boost an optional boost
     */
    public void addInclude(String term, float boost) {
        if (excludes != null) {
            throw new IllegalArgumentException("Cannot have both include and exclude clauses");
        }
        if (includes == null) {
            includes = new HashMap<>();
        }
        includes.put(term, new TermBoost(term, boost));
    }

    public TermBoost[] includeValues() {
        return includes.values().toArray(new TermBoost[includes.size()]);
    }

    public String[] includeValuesAsStringArray() {
        String[] result = new String[includes.size()];
        int i = 0;
        for (TermBoost tb : includes.values()) {
            result[i++] = tb.term;
        }
        return result;
    }

    public String[] excludesAsArray() {
        return excludes.toArray(new String[excludes.size()]);
    }

    public int minDocCount() {
        return minDocCount;
    }

    /**
     * A "certainty" threshold which defines the weight-of-evidence required before
     * a term found in this field is identified as a useful connection
     *
     * @param value The minimum number of documents that contain this term found in the samples used across all shards
     */
    public VertexRequest minDocCount(int value) {
        minDocCount = value;
        return this;
    }


    public int shardMinDocCount() {
        return Math.min(shardMinDocCount, minDocCount);
    }

    /**
     * A "certainty" threshold which defines the weight-of-evidence required before
     * a term found in this field is identified as a useful connection
     *
     * @param value The minimum number of documents that contain this term found in the samples used across all shards
     */
    public VertexRequest shardMinDocCount(int value) {
        shardMinDocCount = value;
        return this;
    }

}
