package com.kayhut.fuse.assembly.knowledge.logical.model;

import java.util.ArrayList;
import java.util.List;

public class ValueLogical extends ElementBaseLogical {
    public ValueLogical(String id, String content, Metadata metadata) {
        super(metadata);
        this.id = id;
        this.content = content;
    }

    //region Properties
    public List<ReferenceLogical> getReferences() {
        return references;
    }

    public void setReferences(List<ReferenceLogical> references) {
        this.references = references;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    //endregion

    //region Fields
    private String id;
    private String content;
    private List<ReferenceLogical> references = new ArrayList<>();

    //endregion
}
