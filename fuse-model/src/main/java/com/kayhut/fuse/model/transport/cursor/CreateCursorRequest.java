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
        @JsonSubTypes.Type(name = "hierarchyFlatten", value = CreateHierarchyFlattenCursorRequest.class)
})
public abstract class CreateCursorRequest {
    public enum Include {
        all,
        entities,
        relationships
    }

    //region Constructors
    public CreateCursorRequest() {
        this.include = Include.all;
    }

    public CreateCursorRequest(CreatePageRequest createPageRequest) {
        this(Include.all, createPageRequest);
    }

    public CreateCursorRequest(Include include, CreatePageRequest createPageRequest) {
        this.include = include;
        this.createPageRequest = createPageRequest;
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
    private Include include;
    //endregion
}
