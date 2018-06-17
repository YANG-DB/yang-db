package com.kayhut.fuse.generator.knowledge.model;

import com.fasterxml.jackson.annotation.JsonFormat;

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
    //endregion

    //region Properties
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public Date getDateValue() {
        return dateValue;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }
    //endregion

    //region Fields
    private Date dateValue;
    //endregion
}
