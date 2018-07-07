package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;

/**
 * Created by Roman on 7/7/2018.
 */
public class KnowledgeGraphHierarchyCursorRequest extends CreateGraphHierarchyCursorRequest {
    public static final String CursorType = "knowledgeGraphHierarchy";

    //region Constructors
    public KnowledgeGraphHierarchyCursorRequest() {
        super();
        this.setCursorType(CursorType);
    }

    public KnowledgeGraphHierarchyCursorRequest(Iterable<String> countTags) {
        super(countTags);
        this.setCursorType(CursorType);
    }

    public KnowledgeGraphHierarchyCursorRequest(Iterable<String> countTags, CreatePageRequest createPageRequest) {
        super(countTags, createPageRequest);
        this.setCursorType(CursorType);
    }

    public KnowledgeGraphHierarchyCursorRequest(Include include, Iterable<String> countTags, CreatePageRequest createPageRequest) {
        super(include, countTags, createPageRequest);
        this.setCursorType(CursorType);
    }
    //endregion
}
