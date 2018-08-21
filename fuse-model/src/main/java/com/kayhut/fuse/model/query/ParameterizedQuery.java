package com.kayhut.fuse.model.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.properties.constraint.NamedParameter;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ParameterizedQuery extends Query {
    private Collection<NamedParameter> params;

    public ParameterizedQuery(Query query,Collection<NamedParameter> params) {
        this.params = params;
        this.setElements(query.getElements());
        this.setName(query.getName());
        this.setOnt(query.getOnt());
        this.setNonidentical(query.getNonidentical());
    }

    public Collection<NamedParameter> getParams() {
        return params;
    }
}
