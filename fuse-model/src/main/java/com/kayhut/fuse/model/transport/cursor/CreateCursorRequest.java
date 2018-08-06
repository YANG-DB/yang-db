package com.kayhut.fuse.model.transport.cursor;

/**
 * Created by User on 07/03/2017.
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.kayhut.fuse.model.transport.CreatePageRequest;

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

    public CreateCursorRequest(String cursorType) {
        this(cursorType, null);
    }

    public CreateCursorRequest(String cursorType, CreatePageRequest createPageRequest) {
        this(cursorType, Include.all, createPageRequest);
    }

    public CreateCursorRequest(String cursorType, Include include, CreatePageRequest createPageRequest) {
        this.cursorType = cursorType;
        this.include = include;
        this.createPageRequest = createPageRequest;
    }
    //endregion

    //region Properties
    public String getCursorType() {
        return cursorType;
    }

    public void setCursorType(String cursorType) {
        this.cursorType = cursorType;
    }

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

    @Override
    public String toString() {
        return "CreateCursorRequest{" +
                "cursorType='" + cursorType + '\'' +
                ", createPageRequest=" + createPageRequest!=null ? createPageRequest.toString() : "None" +
                '}';
    }

    //region Fields
    private String cursorType;
    private CreatePageRequest createPageRequest;
    private Include include;
    //endregion
}
