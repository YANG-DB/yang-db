package com.fuse.domain.knowledge.datagen.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

public class RvalueDate extends RvalueBase {
    //region Constructors
    public RvalueDate() {
        super();
    }

    public RvalueDate(String relationId, String context, String fieldId, Date dateValue) {
        this(relationId, context, fieldId, dateValue, null);
    }

    public RvalueDate(String relationId, String context, String fieldId, Date dateValue, KnowledgeEntityBase.Metadata metadata) {
        super(relationId, context, fieldId, metadata);
        this.dateValue = dateValue;
    }

    public RvalueDate(String relationId, String context, String fieldId, String dateStringValue) {
        this(relationId, context, fieldId, dateStringValue, null);
    }

    public RvalueDate(String relationId, String context, String fieldId, String dateStringValue, KnowledgeEntityBase.Metadata metadata) {
        super(relationId, context, fieldId, metadata);
        this.dateStringValue = dateStringValue;
    }
    //endregion

    //region Properties
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Date getDateValue() {
        return dateValue;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public void setDateValue(Date dateValue) {
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
