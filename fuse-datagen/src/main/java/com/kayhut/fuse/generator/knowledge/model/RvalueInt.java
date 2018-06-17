package com.kayhut.fuse.generator.knowledge.model;

import java.util.Date;

public class RvalueInt extends RvalueBase {
    //region Constructors
    public RvalueInt() {
        super();
    }

    public RvalueInt(String relationId, String context, String fieldId, int intValue) {
        this(relationId, context, fieldId, intValue, null);
    }

    public RvalueInt(String relationId, String context, String fieldId, int intValue, KnowledgeEntityBase.Metadata metadata) {
        super(relationId, context, fieldId, metadata);
        this.intValue = intValue;
    }
    //endregion

    //region Properties
    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }
    //endregion

    //region Fields
    private int intValue;
    //endregion
}
