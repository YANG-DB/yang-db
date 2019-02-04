package com.kayhut.fuse.model.query.properties.constraint;

import com.kayhut.fuse.model.query.Query;

public class InnerQueryConstraint {

    private Query innerQuery;
    private String[] projectedFields;

    public InnerQueryConstraint(Query innerQuery, String ... projectedFields) {
        this.innerQuery = innerQuery;
        this.projectedFields = projectedFields;
    }

    public Query getInnerQuery() {
        return innerQuery;
    }

    public String[] getProjectedFields() {
        return projectedFields;
    }
}
