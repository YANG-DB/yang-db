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
import com.yangdb.fuse.executor.elasticsearch.terms.model.Step;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.IndicesRequest;
import org.elasticsearch.action.ValidateActions;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.bucket.sampler.SamplerAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds the criteria required to guide the exploration of connected terms which
 * can be returned as a graph.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphExploreRequest implements IndicesRequest.Replaceable {

    public static final String NO_STEPS_ERROR_MESSAGE = "Graph explore request must have at least one step";
    public static final String NO_VERTICES_ERROR_MESSAGE = "Graph explore steps must have at least one VertexRequest";

    @JsonProperty("indicesOptions")
    private IndicesOptions indicesOptions = IndicesOptions.fromOptions(false, false, true, false);

    @JsonProperty("indices")
    private String[] indices = Strings.EMPTY_ARRAY;

    @JsonProperty("routing")
    private String routing;

    @JsonProperty("timeout")
    private TimeValue timeout;

    @JsonProperty("sampleSize")
    private int sampleSize = SamplerAggregationBuilder.DEFAULT_SHARD_SAMPLE_SIZE;

    @JsonProperty("useSignificance")
    private boolean useSignificance = true;
    @JsonProperty("returnDetailedInfo")
    private boolean returnDetailedInfo = true;

    @JsonProperty("sampleDiversityField")
    private String sampleDiversityField;
    @JsonProperty("maxDocsPerDiversityValue")
    private int maxDocsPerDiversityValue;

    @JsonProperty("steps")
    private List<Step> steps = new ArrayList<>();

    public GraphExploreRequest() {}

    /**
     * Constructs a new graph request to run against the provided indices. No
     * indices means it will run against all indices.
     */
    public GraphExploreRequest(String... indices) {
        this.indices = indices;
    }

    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = null;
        if (steps.size() == 0) {
            validationException = ValidateActions.addValidationError(NO_STEPS_ERROR_MESSAGE, validationException);
        }
        for (Step step : steps) {
            validationException = step.validate(validationException);
        }
        return validationException;
    }

    @Override
    public String[] indices() {
        return this.indices;
    }

    @Override
    public GraphExploreRequest indices(String... indices) {
        this.indices = indices;
        return this;
    }

    @Override
    public IndicesOptions indicesOptions() {
        return indicesOptions;
    }

    public GraphExploreRequest indicesOptions(IndicesOptions indicesOptions) {
        if (indicesOptions == null) {
            throw new IllegalArgumentException("IndicesOptions must not be null");
        }
        this.indicesOptions = indicesOptions;
        return this;
    }


    @JsonProperty("indices")
    public String[] getIndices() {
        return indices;
    }

    @JsonProperty("indices")
    public void setIndices(String[] indices) {
        this.indices = indices;
    }

    @JsonProperty("indicesOptions")
    public IndicesOptions getIndicesOptions() {
        return indicesOptions;
    }

    @JsonProperty("indicesOptions")
    public void setIndicesOptions(IndicesOptions indicesOptions) {
        this.indicesOptions = indicesOptions;
    }

    @JsonProperty("routing")
    public String getRouting() {
        return routing;
    }

    @JsonProperty("routing")
    public void setRouting(String routing) {
        this.routing = routing;
    }

    @JsonProperty("timeout")
    public TimeValue getTimeout() {
        return timeout;
    }

    @JsonProperty("timeout")
    public void setTimeout(TimeValue timeout) {
        this.timeout = timeout;
    }

    @JsonProperty("sampleSize")
    public int getSampleSize() {
        return sampleSize;
    }

    @JsonProperty("sampleSize")
    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    @JsonProperty("sampleDiversityField")
    public String getSampleDiversityField() {
        return sampleDiversityField;
    }

    @JsonProperty("sampleDiversityField")
    public void setSampleDiversityField(String sampleDiversityField) {
        this.sampleDiversityField = sampleDiversityField;
    }

    @JsonProperty("maxDocsPerDiversityValue")
    public int getMaxDocsPerDiversityValue() {
        return maxDocsPerDiversityValue;
    }

    @JsonProperty("maxDocsPerDiversityValue")
    public void setMaxDocsPerDiversityValue(int maxDocsPerDiversityValue) {
        this.maxDocsPerDiversityValue = maxDocsPerDiversityValue;
    }

    @JsonProperty("useSignificance")
    public boolean isUseSignificance() {
        return useSignificance;
    }

    @JsonProperty("useSignificance")
    public void setUseSignificance(boolean useSignificance) {
        this.useSignificance = useSignificance;
    }

    @JsonProperty("returnDetailedInfo")
    public boolean isReturnDetailedInfo() {
        return returnDetailedInfo;
    }

    @JsonProperty("returnDetailedInfo")
    public void setReturnDetailedInfo(boolean returnDetailedInfo) {
        this.returnDetailedInfo = returnDetailedInfo;
    }

    @JsonProperty("steps")
    public List<Step> getSteps() {
        return steps;
    }

    @JsonProperty("steps")
    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public String routing() {
        return this.routing;
    }

    public GraphExploreRequest routing(String routing) {
        this.routing = routing;
        return this;
    }

    public GraphExploreRequest routing(String... routings) {
        this.routing = Strings.arrayToCommaDelimitedString(routings);
        return this;
    }

    public TimeValue timeout() {
        return timeout;
    }

    /**
     * Graph exploration can be set to timeout after the given period. Search
     * operations involved in each hop are limited to the remaining time
     * available but can still overrun due to the nature of their "best efforts"
     * timeout support. When a timeout occurs partial results are returned.
     *
     * @param timeout
     *            a {@link TimeValue} object which determines the maximum length
     *            of time to spend exploring
     */
    public GraphExploreRequest timeout(TimeValue timeout) {
        if (timeout == null) {
            throw new IllegalArgumentException("timeout must not be null");
        }
        this.timeout = timeout;
        return this;
    }

    public GraphExploreRequest timeout(String timeout) {
        timeout(TimeValue.parseTimeValue(timeout, null, getClass().getSimpleName() + ".timeout"));
        return this;
    }

    @Override
    public String toString() {
        return "graph explore [" + Arrays.toString(indices) + "]";
    }

    /**
     * The number of top-matching documents that are considered during each hop
     * (default is {@link SamplerAggregationBuilder#DEFAULT_SHARD_SAMPLE_SIZE}
     * Very small values (less than 50) may not provide sufficient
     * weight-of-evidence to identify significant connections between terms.
     * <p>
     * Very large values (many thousands) are not recommended with loosely
     * defined queries (fuzzy queries or those with many OR clauses). This is
     * because any useful signals in the best documents are diluted with
     * irrelevant noise from low-quality matches. Performance is also typically
     * better with smaller samples as there are less look-ups required for
     * background frequencies of terms found in the documents
     * </p>
     *
     * @param maxNumberOfDocsPerSteps
     *            shard-level sample size in documents
     */
    public void sampleSize(int maxNumberOfDocsPerSteps) {
        sampleSize = maxNumberOfDocsPerSteps;
    }

    public int sampleSize() {
        return sampleSize;
    }

    /**
     * Optional choice of single-value field on which to diversify sampled
     * search results
     */
    public void sampleDiversityField(String name) {
        sampleDiversityField = name;
    }

    public String sampleDiversityField() {
        return sampleDiversityField;
    }

    /**
     * Optional number of permitted docs with same value in sampled search
     * results. Must also declare which field using sampleDiversityField
     */
    public void maxDocsPerDiversityValue(int maxDocs) {
        this.maxDocsPerDiversityValue = maxDocs;
    }

    public int maxDocsPerDiversityValue() {
        return maxDocsPerDiversityValue;
    }

    /**
     * Controls the choice of algorithm used to select interesting terms. The
     * default value is true which means terms are selected based on
     * significance (see the {@link SignificantTerms} aggregation) rather than
     * popularity (using the {@link TermsAggregator}).
     *
     * @param value
     *            true if the significant_terms algorithm should be used.
     */
    public void useSignificance(boolean value) {
        this.useSignificance = value;
    }

    public boolean useSignificance() {
        return useSignificance;
    }

    /**
     * Return detailed information about vertex frequencies as part of JSON
     * results - defaults to false
     *
     * @param value
     *            true if detailed information is required in JSON responses
     */
    public void returnDetailedInfo(boolean value) {
        this.returnDetailedInfo = value;
    }

    public boolean returnDetailedInfo() {
        return returnDetailedInfo;
    }

    /**
     * Add a stage in the graph exploration. Each hop represents a stage of
     * querying elasticsearch to identify terms which can then be connnected to
     * other terms in a subsequent hop.
     *
     * @param guidingQuery
     *            optional choice of query which influences which documents are
     *            considered in this stage
     * @return a {@link Step} object that holds settings for a stage in the graph
     *         exploration
     */
    public Step createNextStep(QueryBuilder guidingQuery) {
        Step parent = null;
        if (steps.size() > 0) {
            parent = steps.get(steps.size() - 1);
        }
        Step newStep = new Step(parent);
        newStep.guidingQuery = guidingQuery;
        steps.add(newStep);
        return newStep;
    }

    public int getStepNumbers() {
        return steps.size();
    }

    public Step getStep(int stepNumber) {
        return steps.get(stepNumber);
    }

    public static class TermBoost {
        public String term;
        public float boost;

        public TermBoost(String term, float boost) {
            super();
            this.term = term;
            if (boost <= 0) {
                throw new IllegalArgumentException("Boosts must be a positive non-zero number");
            }
            this.boost = boost;
        }

        public TermBoost() {}

        public String getTerm() {
            return term;
        }

        public float getBoost() {
            return boost;
        }

    }
}
