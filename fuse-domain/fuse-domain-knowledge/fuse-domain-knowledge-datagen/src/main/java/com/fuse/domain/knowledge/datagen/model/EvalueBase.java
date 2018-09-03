package com.fuse.domain.knowledge.datagen.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class EvalueBase extends KnowledgeEntityBase {
    private static final String entityType = "e.value";

    //region Constructors
    public EvalueBase() {
        super(entityType);
    }

    public EvalueBase(String logicalId, String context, String entityId, String fieldId) {
        this(logicalId, context, entityId, fieldId, null);
    }

    public EvalueBase(String logicalId, String context, String entityId, String fieldId, KnowledgeEntityBase.Metadata metadata) {
        super(entityType, metadata);
        this.logicalId = logicalId;
        this.context = context;
        this.entityId = entityId;
        this.fieldId = fieldId;
    }
    //endregion

    //region Properties
    public String getLogicalId() {
        return logicalId;
    }

    public void setLogicalId(String logicalId) {
        this.logicalId = logicalId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }
    //endregion

    //region Fields
    private String logicalId;
    private String context;
    private String entityId;
    private String fieldId;
    //endregion
}
