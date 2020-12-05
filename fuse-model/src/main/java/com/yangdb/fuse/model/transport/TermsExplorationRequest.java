package com.yangdb.fuse.model.transport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yangdb.fuse.model.query.quant.QuantType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transport class for calling the terms exploration API request
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TermsExplorationRequest {
    public static final int DEFAULT_SIZE = 5;
    public static final int DEFAULT_MIN_DOC_COUNT = 3;
    public static final int DEFAULT_SHARD_MIN_DOC_COUNT = 2;
    @JsonProperty("indices")
    private String[] indices = new String[]{};

    @JsonProperty("routing")
    private String routing;

    @JsonProperty("timeout")
    private long timeout;

    @JsonProperty("steps")
    private List<Steps> steps;


    public TermsExplorationRequest() {
    }

    /**
     *
     * @param indices
     * @param routing
     * @param timeout
     */
    public TermsExplorationRequest(String[] indices, String routing, long timeout) {
        this.indices = indices;
        this.routing = routing;
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "TermsExplorationRequest{" +
                "indices=" + Arrays.toString(indices) +
                ", routing='" + routing + '\'' +
                ", timeout=" + timeout +
                ", steps=" + steps +
                '}';
    }

    @JsonProperty("steps")
    public List<Steps> getSteps() {
        return steps;
    }

    @JsonProperty("steps")
    public void setSteps(List<Steps> steps) {
        this.steps = steps;
    }

    @JsonProperty("indices")
    public String[] getIndices() {
        return indices;
    }

    @JsonProperty("indices")
    public void setIndices(String[] indices) {
        this.indices = indices;
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
    public long getTimeout() {
        return timeout;
    }

    @JsonProperty("timeout")
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Steps {
        @JsonProperty("initialTerms")
        private InitialTerms initialTerms;

        @JsonProperty("nodes")
        private List<Nodes> nodes;

        public Steps() {}

        public Steps(List<Nodes> nodes) {
            this.nodes = nodes;
        }

        @JsonProperty("initialTerms")
        public InitialTerms getInitialTerms() {
            return initialTerms;
        }

        @JsonProperty("initialTerms")
        public void setInitialTerms(InitialTerms initialTerms) {
            this.initialTerms = initialTerms;
        }

        @JsonProperty("nodes")
        public List<Nodes> getNodes() {
            return nodes;
        }

        @JsonProperty("nodes")
        public void setNodes(List<Nodes> nodes) {
            this.nodes = nodes;
        }

        @Override
        public String toString() {
            return "Steps{" +
                    "initialTerms=" + initialTerms +
                    ", nodes=" + nodes +
                    '}';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InitialTerms {
        private List<String> terms;
        private String field;
        private QuantType quantType;

        public InitialTerms() {
        }

        public InitialTerms(String field, List<String> terms, QuantType quantType) {
            this.field = field;
            this.terms = terms;
            this.quantType = quantType;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public List<String> getTerms() {
            return terms;
        }

        public void setTerms(List<String> terms) {
            this.terms = terms;
        }

        public QuantType getQuantType() {
            return quantType;
        }

        public void setQuantType(QuantType quantType) {
            this.quantType = quantType;
        }

        @Override
        public String toString() {
            return "InitialTerms{" +
                    "terms=" + terms +
                    ", field='" + field + '\'' +
                    ", quantType=" + quantType +
                    '}';
        }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Nodes {
        @JsonProperty("fieldName")
        private String fieldName;
        @JsonProperty("size")
        private int size = DEFAULT_SIZE;
        @JsonProperty("includes")
        private Map<String, Float> includes;
        @JsonProperty("excludes")
        private Set<String> excludes;
        @JsonProperty("minDocCount")
        private int minDocCount = DEFAULT_MIN_DOC_COUNT;
        @JsonProperty("shardMinDocCount")
        private int shardMinDocCount = DEFAULT_SHARD_MIN_DOC_COUNT;

        public Nodes() {
        }

        public Nodes(String fieldName, int size, Map<String, Float> includes, Set<String> excludes, int minDocCount, int shardMinDocCount) {
            this.fieldName = fieldName;
            this.size = size;
            this.includes = includes;
            this.excludes = excludes;
            this.minDocCount = minDocCount;
            this.shardMinDocCount = shardMinDocCount;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public Map<String, Float> getIncludes() {
            return includes;
        }

        public void setIncludes(Map<String, Float> includes) {
            this.includes = includes;
        }

        public Set<String> getExcludes() {
            return excludes;
        }

        public void setExcludes(Set<String> excludes) {
            this.excludes = excludes;
        }

        public int getMinDocCount() {
            return minDocCount;
        }

        public void setMinDocCount(int minDocCount) {
            this.minDocCount = minDocCount;
        }

        public int getShardMinDocCount() {
            return shardMinDocCount;
        }

        public void setShardMinDocCount(int shardMinDocCount) {
            this.shardMinDocCount = shardMinDocCount;
        }

        @Override
        public String toString() {
            return "Nodes{" +
                    "fieldName='" + fieldName + '\'' +
                    ", size=" + size +
                    ", includes=" + includes +
                    ", excludes=" + excludes +
                    ", minDocCount=" + minDocCount +
                    ", shardMinDocCount=" + shardMinDocCount +
                    '}';
        }
    }

}
