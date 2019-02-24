package com.kayhut.fuse.model.query.properties.constraint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryNamedParameter extends NamedParameter {
    private String query;

    public QueryNamedParameter() {
    }

    public QueryNamedParameter(String query,String name, Object value) {
        super(name, value);
        this.query = query;
    }

    public QueryNamedParameter(String query,String name) {
        super(name);
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
