package com.fuse.domain.knowledge.datagen.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RvalueString extends RvalueBase {
    //region Constructors
    public RvalueString() {
        super();
    }

    public RvalueString(String relationId, String context, String fieldId, String stringValue) {
        this(relationId, context, fieldId, stringValue, null);
    }

    public RvalueString(String relationId, String context, String fieldId, String stringValue, KnowledgeEntityBase.Metadata metadata) {
        super(relationId, context, fieldId, metadata);
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
