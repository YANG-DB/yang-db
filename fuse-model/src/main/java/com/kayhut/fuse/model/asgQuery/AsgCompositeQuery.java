package com.kayhut.fuse.model.asgQuery;

import java.util.ArrayList;
import java.util.List;

public class AsgCompositeQuery extends AsgQuery {
    private List<AsgQuery> queryChain = new ArrayList<>();

    public AsgCompositeQuery() {}

    public AsgCompositeQuery(AsgQuery asgQuery) {
        this.setName(asgQuery.getName());
        this.setOnt(asgQuery.getOnt());
        this.setParameters(asgQuery.getParameters());
        this.setStart(asgQuery.getStart());
        this.setElements(asgQuery.getElements());
    }

    public AsgCompositeQuery with(AsgQuery query) {
        queryChain.add(query);
        return this;
    }

    public List<AsgQuery> getQueryChain() {
        return queryChain;
    }
}
