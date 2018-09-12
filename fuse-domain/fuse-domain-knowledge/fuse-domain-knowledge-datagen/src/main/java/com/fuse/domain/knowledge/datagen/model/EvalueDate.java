package com.fuse.domain.knowledge.datagen.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

public class EvalueDate  extends EvalueBase {
    //region Constructors
    public EvalueDate() {
        super();
    }

    public EvalueDate(String logicalId, String context, String entityId, String fieldId, Date dateValue) {
        this(logicalId, context, entityId, fieldId, dateValue, null);
    }

    public EvalueDate(String logicalId, String context, String entityId, String fieldId, String dateStringValue) {
        this(logicalId, context, entityId, fieldId, dateStringValue, null);
    }

    public EvalueDate(String logicalId, String context, String entityId, String fieldId, Date dateValue, KnowledgeEntityBase.Metadata metadata) {
        super(logicalId, context, entityId, fieldId, metadata);
        this.dateValue = dateValue;
    }

    public EvalueDate(String logicalId, String context, String entityId, String fieldId, String dateStringValue, KnowledgeEntityBase.Metadata metadata) {
        super(logicalId, context, entityId, fieldId, metadata);
        this.dateStringValue = dateStringValue;
    }
    //endregion

    //region Properties
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Date getStringValue() {
        return dateValue;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setStringValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getDateStringValue() {
        return dateStringValue;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setDateStringValue(String dateStringValue) {
        this.dateStringValue = dateStringValue;
    }

    //endregion

    //region Fields
    private Date dateValue;
    private String dateStringValue;
    //endregion
}
