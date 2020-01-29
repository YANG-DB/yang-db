package com.yangdb.fuse.model.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QueryInfo<Query> {
    private Query query;
    private String queryName;
    private String queryType;
    private String ontology;

    public QueryInfo(Query query,String queryName,String queryType, String ontology) {
        this.query = query;
        this.queryName = queryName;
        this.queryType = queryType;
        this.ontology = ontology;
    }

    public String getQueryType() {
        return queryType;
    }

    public String getQueryName() {
        return queryName;
    }

    public Query getQuery() {
        return query;
    }

    public String getOntology() {
        return ontology;
    }
}
