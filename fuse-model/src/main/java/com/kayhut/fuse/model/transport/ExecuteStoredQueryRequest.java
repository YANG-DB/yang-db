package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.query.QueryRef;
import com.kayhut.fuse.model.query.properties.constraint.NamedParameter;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;

import java.util.Collection;
import java.util.Collections;

public class ExecuteStoredQueryRequest extends CreateQueryRequest {

    private final Collection<NamedParameter> parameters;
    private final Collection<NamedParameter> executionParams;

    public ExecuteStoredQueryRequest() {
        this.parameters = Collections.EMPTY_LIST;
        this.executionParams = Collections.EMPTY_LIST;
    }

    public ExecuteStoredQueryRequest(String id, String name, Collection<NamedParameter> parameters,Collection<NamedParameter> executionParams) {
        super(id, name, new QueryRef(name));
        this.parameters = parameters;
        this.executionParams = executionParams;
    }

    public ExecuteStoredQueryRequest(String id, String name, CreateCursorRequest createCursorRequest, Collection<NamedParameter> parameters,Collection<NamedParameter> executionParams) {
        super(id, name, new QueryRef(name), new PlanTraceOptions(), createCursorRequest);
        this.parameters = parameters;
        this.executionParams = executionParams;
    }

    public Collection<NamedParameter> getExecutionParams() {
        return executionParams;
    }

    public Collection<NamedParameter> getParameters() {
        return parameters;
    }

    public CreatePageRequest getPageCursorRequest() {
        return (getCreateCursorRequest() != null ? getCreateCursorRequest().getCreatePageRequest() : null);
    }
}
