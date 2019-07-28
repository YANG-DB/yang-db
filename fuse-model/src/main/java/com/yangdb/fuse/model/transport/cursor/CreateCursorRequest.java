package com.yangdb.fuse.model.transport.cursor;

/*-
 * #%L
 * CreateCursorRequest.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Created by lior.perry on 07/03/2017.
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.transport.CreatePageRequest;

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

    public CreateCursorRequest maxExecutionTime(long time) {
        this.maxExecutionTime = time;
        return this;
    }
    //endregion

    //region Properties

    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }

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

    public CreateCursorRequest with(CreatePageRequest createPageRequest) {
        setCreatePageRequest(createPageRequest);
        return this;
    }
    //endregions

    @Override
    public String toString() {
        return "CreateCursorRequest{" +
                "cursorType='" + cursorType + '\'' +
                ", createPageRequest=" + (createPageRequest!=null ? createPageRequest.toString() : "None") +
                '}';
    }

    //region Fields
    private long maxExecutionTime = 10* 60 * 1000;
    private String cursorType;
    private CreatePageRequest createPageRequest;
    private Include include;
    //endregion
}