package com.kayhut.fuse.model.query;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QueryRef extends Query {
    public QueryRef(String name) {
        super();
        setName(name);
    }

    @Override
    public List<EBase> getElements() {
        return Collections.emptyList();
    }

    @Override
    public List<List<String>> getNonidentical() {
        return Collections.emptyList();
    }
}
