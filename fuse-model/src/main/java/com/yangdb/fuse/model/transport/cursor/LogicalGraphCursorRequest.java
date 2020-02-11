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

import com.yangdb.fuse.model.transport.CreatePageRequest;

import java.util.Collections;

public class LogicalGraphCursorRequest extends CreateGraphHierarchyCursorRequest {
    public static final String CursorType = "LogicalGraphCursorRequest";
    public String ontology;

    //region Constructors
    public LogicalGraphCursorRequest() {
        this.setCursorType(CursorType);
    }

    public LogicalGraphCursorRequest(String ontology) {
        super();
        this.ontology = ontology;
        this.setCursorType(CursorType);
    }

    public LogicalGraphCursorRequest(String ontology,Iterable<String> countTags) {
        super(countTags);
        this.ontology = ontology;
        this.setCursorType(CursorType);
    }

    public LogicalGraphCursorRequest(String ontology,CreatePageRequest createPageRequest) {
        super(Collections.emptyList(),createPageRequest);
        this.ontology = ontology;
        this.setCursorType(CursorType);
    }

    public LogicalGraphCursorRequest(String ontology,Iterable<String> countTags, CreatePageRequest createPageRequest) {
        this(ontology,countTags,createPageRequest, GraphFormat.JSON);
        this.ontology = ontology;
    }

    public LogicalGraphCursorRequest(String ontology,Iterable<String> countTags, CreatePageRequest createPageRequest, GraphFormat format ) {
        super(countTags, createPageRequest);
        this.setCursorType(CursorType);
        this.ontology = ontology;
    }

    public LogicalGraphCursorRequest(String ontology,Include include, Iterable<String> countTags, CreatePageRequest createPageRequest) {
        this(ontology,include,countTags,createPageRequest, GraphFormat.JSON);
    }

    public LogicalGraphCursorRequest(String ontology,Include include, Iterable<String> countTags, CreatePageRequest createPageRequest, GraphFormat format) {
        super(include, countTags, createPageRequest,format);
        this.ontology = ontology;
        this.setCursorType(CursorType);
    }

    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    public String getOntology() {
        return ontology;
    }
}
