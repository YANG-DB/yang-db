package com.kayhut.fuse.generator.knowledge.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class EvalueDate  extends EvalueBase {
    //region Constructors
    public EvalueDate() {
        super();
    }

    public EvalueDate(String logicalId, String context, String entityId, String fieldId, Date dateValue) {
        this(logicalId, context, entityId, fieldId, dateValue, null);
    }

    public EvalueDate(String logicalId, String context, String entityId, String fieldId, Date dateValue, KnowledgeEntityBase.Metadata metadata) {
        super(logicalId, context, entityId, fieldId, metadata);
        this.dateValue = dateValue;
    }
    //endregion

    //region Properties
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public Date getStringValue() {
        return dateValue;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public void setStringValue(Date dateValue) {
        this.dateValue = dateValue;
    }
    //endregion

    //region Fields
    private Date dateValue;
    //endregion
}
