package com.kayhut.fuse.generator.knowledge.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class RvalueBase extends KnowledgeEntityBase {
    private static final String entityType = "r.value";

    //region Constructors
    public RvalueBase() {
        super(entityType);
    }

    public RvalueBase(String relationId, String context, String fieldId) {
        this(relationId, context, fieldId, null);
    }

    public RvalueBase(String relationId, String context, String fieldId, KnowledgeEntityBase.Metadata metadata) {
        super(entityType, metadata);
        this.relationId = relationId;
        this.context = context;
        this.fieldId = fieldId;
    }
    //endregion

    //region Properties
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getRelationId() {
        return relationId;
    }

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }
    //endregion

    //region Fields
    private String context;
    private String relationId;
    private String fieldId;
    //endregion
}
