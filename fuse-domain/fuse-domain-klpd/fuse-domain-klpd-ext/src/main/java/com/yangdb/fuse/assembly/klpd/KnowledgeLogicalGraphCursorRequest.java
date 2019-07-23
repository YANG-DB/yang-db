package com.yangdb.fuse.assembly.klpd;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;

public class KnowledgeLogicalGraphCursorRequest extends CreateGraphHierarchyCursorRequest {
    public static final String CursorType = "knowledgeLogicalGraphCursorRequest";

    //region Constructors
    public KnowledgeLogicalGraphCursorRequest() {
        super();
        this.setCursorType(CursorType);
    }

    public KnowledgeLogicalGraphCursorRequest(Iterable<String> countTags) {
        super(countTags);
        this.setCursorType(CursorType);
    }

    public KnowledgeLogicalGraphCursorRequest(Iterable<String> countTags, CreatePageRequest createPageRequest) {
        this(countTags,createPageRequest, GraphFormat.JSON);
    }

    public KnowledgeLogicalGraphCursorRequest(Iterable<String> countTags, CreatePageRequest createPageRequest,GraphFormat format ) {
        super(countTags, createPageRequest);
        this.format = format;
        this.setCursorType(CursorType);
    }

    public KnowledgeLogicalGraphCursorRequest(Include include, Iterable<String> countTags, CreatePageRequest createPageRequest) {
        this(include,countTags,createPageRequest,GraphFormat.JSON);
    }

    public KnowledgeLogicalGraphCursorRequest(Include include, Iterable<String> countTags, CreatePageRequest createPageRequest, GraphFormat format) {
        super(include, countTags, createPageRequest);
        this.format = format;
        this.setCursorType(CursorType);
    }

    public GraphFormat getFormat() {
        return format;
    }

    //endregion
    private GraphFormat format = GraphFormat.JSON;

    public enum GraphFormat {
        JSON,XML
    }
}
