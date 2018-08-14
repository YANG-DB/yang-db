package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.query.QueryRef;
import com.kayhut.fuse.model.query.properties.constraint.NamedParameter;

import java.util.Collection;

public class ExecuteStoredQueryRequest extends CreateQueryRequest{

    private final Collection<NamedParameter> parameters;

    public ExecuteStoredQueryRequest(String id, String name, Collection<NamedParameter> parameters) {
        super(id, name, new QueryRef(name));
        this.parameters = parameters;
    }

    public Collection<NamedParameter> getParameters() {
        return parameters;
    }
}
