package com.kayhut.fuse.assembly.knowledge.logical.model;

import java.util.HashMap;

public class InsightLogical extends ElementBaseLogical {
    public InsightLogical(String id, String context, String content, Metadata metadata) {
        super(metadata);
        this.id = id;
        this.context = context;
        this.content = content;
    }

    //region Properties
    public HashMap<String, ReferenceLogical>  getReferences() {
        return references;
    }

    public void setReferences(HashMap<String, ReferenceLogical> references) {
        this.references = references;
    }
    //endregion

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
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

    public HashMap<String, GlobalEntityLogical> getEntities() {
        return entities;
    }

    public void setEntities(HashMap<String, GlobalEntityLogical> entities) {
        this.entities = entities;
    }

    //region Fields
    private String id;
    private String context;
    private String content;
    // ref id is key
    private HashMap<String, ReferenceLogical> references = new HashMap<>();
    // field id is key
    private HashMap<String, GlobalEntityLogical> entities = new HashMap<>();


    //endregion
}
