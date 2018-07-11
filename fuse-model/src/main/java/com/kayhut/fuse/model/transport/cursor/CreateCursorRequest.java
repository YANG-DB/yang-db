package com.kayhut.fuse.model.transport.cursor;

/**
 * Created by User on 07/03/2017.
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.transport.CreatePageRequest;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "cursorType")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "paths", value = CreatePathsCursorRequest.class),
        @JsonSubTypes.Type(name = "graph", value = CreateGraphCursorRequest.class),
        @JsonSubTypes.Type(name = "graphHierarchy", value = CreateGraphHierarchyCursorRequest.class),
        @JsonSubTypes.Type(name = "csv", value = CreateCsvCursorRequest.class),
        @JsonSubTypes.Type(name = "hierarchyFlatten", value = CreateHierarchyFlattenCursorRequest.class),
        @JsonSubTypes.Type(name = "logicalModelGraphHierarchy", value = CreateLogicalGraphHierarchyCursorRequest.class)
})
public abstract class CreateCursorRequest {
    public static final String defaultTimeout = "CreateCursorRequest.@timeout";

    public static final int TIMEOUT = 60 * 1000 * 3;

    public enum Include {
        all,
        entities,
        relationships
    }

    public CreateCursorRequest() {
        this(TIMEOUT);
    }

    //region Constructors
    @Inject
    public CreateCursorRequest(@Named(defaultTimeout)
                                       long timeout) {
        this.include = Include.all;
        this.timeout = timeout;
    }

    public CreateCursorRequest(CreatePageRequest createPageRequest,@Named(defaultTimeout) long timeout) {
        this(Include.all, createPageRequest, timeout);
    }

    public CreateCursorRequest(CreatePageRequest createPageRequest) {
        this(Include.all, createPageRequest, TIMEOUT);
    }

    public CreateCursorRequest(Include include, CreatePageRequest createPageRequest) {
        this(include, createPageRequest, TIMEOUT);
    }

    public CreateCursorRequest(Include include, CreatePageRequest createPageRequest, long timeout) {
        this.include = include;
        this.createPageRequest = createPageRequest;
        this.timeout = timeout;
    }
    //endregion

    //region Properties
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public CreatePageRequest getCreatePageRequest() {
        return createPageRequest;
    }

    public void setCreatePageRequest(CreatePageRequest createPageRequest) {
        this.createPageRequest = createPageRequest;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Include getInclude() {
        return include;
    }

    public void setInclude(Include include) {
        this.include = include;
    }

    //endregions

    //region Fields
    private CreatePageRequest createPageRequest;

    private long timeout;
    private Include include;
    //endregion
}
