package com.fuse.domain.knowledge.datagen.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class EvalueString extends EvalueBase {
    //region Constructors
    public EvalueString() {
        super();
    }

    public EvalueString(String logicalId, String context, String entityId, String fieldId, String stringValue) {
        this(logicalId, context, entityId, fieldId, stringValue, null);
    }

    public EvalueString(String logicalId, String context, String entityId, String fieldId, String stringValue, KnowledgeEntityBase.Metadata metadata) {
        super(logicalId, context, entityId, fieldId, metadata);
        this.stringValue = stringValue;
    }
    //endregion

    //region Properties
    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
    //endregion

    //region Fields
    private String stringValue;
    //endregion
}
