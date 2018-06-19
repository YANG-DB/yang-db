package com.kayhut.fuse.generator.knowledge;

public class LightSchema {
    //region Properties
    public String getEntityIndex() {
        return entityIndex;
    }

    public String getRelationIndex() {
        return relationIndex;
    }

    public String getInsightIndex() {
        return insightIndex;
    }

    public String getReferenceIndex() {
        return referenceIndex;
    }
    //endregion

    //region Fields
    private String entityIndex;
    private String relationIndex;
    private String insightIndex;
    private String referenceIndex;
    //endregion
}
