package com.kayhut.fuse.assembly.knowledge.logical.model;

import java.util.HashMap;

public class RelationLogical extends ElementBaseLogical {
    public RelationLogical(String id, String context, String relationCategory, Metadata metadata) {
        super(metadata);
        this.id = id;
        this.context = context;
        this.relationCategory = relationCategory;
    }
    public RelationLogical(String id, String context, String relationCategory, String entityAId, String entityBId, Metadata metadata) {
        super(metadata);
        this.id = id;
        this.context = context;
        this.relationCategory = relationCategory;
        this.entityA = new GlobalEntityLogical(entityAId);
        this.entityB = new GlobalEntityLogical(entityBId);
    }

    //region Properties
    public HashMap<String, ReferenceLogical> getReferences() {
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

    public String getRelationCategory() {
        return relationCategory;
    }

    public void setRelationCategory(String relationCategory) {
        this.relationCategory = relationCategory;
    }

    public HashMap<String, FieldLogical> getFields() {
        return fields;
    }

    public void setFields(HashMap<String, FieldLogical> fields) {
        this.fields = fields;
    }

    public GlobalEntityLogical getEntityA() {
        return entityA;
    }

    public void setEntityA(GlobalEntityLogical entityA) {
        this.entityA = entityA;
    }

    public GlobalEntityLogical getEntityB() {
        return entityB;
    }

    public void setEntityB(GlobalEntityLogical entityB) {
        this.entityB = entityB;
    }


    //region Fields
    private String id;
    private String context;
    private String relationCategory;
    // ref id is key
    private HashMap<String, ReferenceLogical> references = new HashMap<>();
    // field id is key
    private HashMap<String, FieldLogical> fields = new HashMap<>();

    private GlobalEntityLogical entityA;
    private GlobalEntityLogical entityB;


    //endregion
}
