package com.yangdb.fuse.model.transport.cursor;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

/*-
 *
 * CreateCursorRequest.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

/**
 * Created by lior.perry on 07/03/2017.
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.transport.CreatePageRequest;

public abstract class CreateCursorRequest {
    /**
     * current default cursor request
     * @return
     */
    public static final String getDefaultCursorRequestType() {
        return CreateForwardOnlyPathTraversalCursorRequest.CursorType;
    }

    //default max execution time
    public static final int DEFAULT_EXECUTION_TIME = 10 * 60 * 1000;

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

    public CreateCursorRequest(String ontology,String cursorType, Include include, CreatePageRequest createPageRequest) {
        this.ontology = ontology;
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

    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    public String getOntology() {
        return ontology;
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
    public CreateCursorRequest with(String ontology) {
        setOntology(ontology);
        return this;
    }

    public void setMaxExecutionTime(long maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
    }

    public boolean isProfile() {
        return profile;
    }

    public CreateCursorRequest withProfile(boolean profile) {
        this.profile = profile;
        return this;
    }
    //endregions

    @Override
    public String toString() {
        return "CreateCursorRequest{" +
                "ontology='" + ontology + '\'' +
                "cursorType='" + cursorType + '\'' +
                ", createPageRequest=" + (createPageRequest!=null ? createPageRequest.toString() : "None") +
                '}';
    }

    //region Fields
    private long maxExecutionTime = DEFAULT_EXECUTION_TIME;
    private boolean profile = false;
    private String cursorType;
    private String ontology;
    private CreatePageRequest createPageRequest;
    private Include include;
    //endregion
}
