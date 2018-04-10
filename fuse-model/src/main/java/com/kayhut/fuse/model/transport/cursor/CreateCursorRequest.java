package com.kayhut.fuse.model.transport.cursor;

/**
 * Created by User on 07/03/2017.
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kayhut.fuse.model.transport.CreatePageRequest;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "cursorType")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "paths", value = CreatePathsCursorRequest.class),
        @JsonSubTypes.Type(name = "graph", value = CreateGraphCursorRequest.class),
        @JsonSubTypes.Type(name = "graphHierarchy", value = CreateGraphHierarchyCursorRequest.class),
        @JsonSubTypes.Type(name = "csv", value = CreateCsvCursorRequest.class),
})
public abstract class CreateCursorRequest {
    //region Constructors
    public CreateCursorRequest() {

    }

    public CreateCursorRequest(CreatePageRequest createPageRequest) {
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
    //endregions

    //region Fields
    private CreatePageRequest createPageRequest;
    //endregion
}
