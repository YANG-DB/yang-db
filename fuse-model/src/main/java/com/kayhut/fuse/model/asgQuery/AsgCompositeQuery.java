package com.kayhut.fuse.model.asgQuery;

import java.util.ArrayList;
import java.util.List;

public class AsgCompositeQuery extends AsgQuery {
    private List<AsgQuery> queryChain = new ArrayList<>();

    public AsgCompositeQuery with(AsgQuery query) {
        queryChain.add(query);
        return this;
    }

    public List<AsgQuery> getQueryChain() {
        return queryChain;
    }
}
