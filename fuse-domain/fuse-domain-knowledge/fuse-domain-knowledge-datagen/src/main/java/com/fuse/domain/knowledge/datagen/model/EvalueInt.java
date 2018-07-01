package com.fuse.domain.knowledge.datagen.model;

import java.util.Date;

public class EvalueInt extends EvalueBase {
    //region Constructors
    public EvalueInt() {
        super();
    }

    public EvalueInt(String logicalId, String context, String entityId, String fieldId, int intValue) {
        this(logicalId, context, entityId, fieldId, intValue, null);
    }

    public EvalueInt(String logicalId, String context, String entityId, String fieldId, int intValue, KnowledgeEntityBase.Metadata metadata) {
        super(logicalId, context, entityId, fieldId, metadata);
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
