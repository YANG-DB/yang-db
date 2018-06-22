package com.kayhut.fuse.generator.knowledge;

public class LightSchema {
    //region Constructors
    public LightSchema() {}

    public LightSchema(
            String entityIndex,
            String relationIndex,
            String insightIndex,
            String referenceIndex,
            String idFormat) {
        this.entityIndex = entityIndex;
        this.relationIndex = relationIndex;
        this.insightIndex = insightIndex;
        this.referenceIndex = referenceIndex;
        this.idFormat = idFormat;
    }
    //endregion

    //region Properties
    public String getEntityIndex() {
        return entityIndex;
    }

    public void setEntityIndex(String entityIndex) {
        this.entityIndex = entityIndex;
    }

    public String getRelationIndex() {
        return relationIndex;
    }

    public void setRelationIndex(String relationIndex) {
        this.relationIndex = relationIndex;
    }

    public String getInsightIndex() {
        return insightIndex;
    }

    public void setInsightIndex(String insightIndex) {
        this.insightIndex = insightIndex;
    }

    public String getReferenceIndex() {
        return referenceIndex;
    }

    public void setReferenceIndex(String referenceIndex) {
        this.referenceIndex = referenceIndex;
    }

    public String getIdFormat() {
        return idFormat;
    }

    public void setIdFormat(String idFormat) {
        this.idFormat = idFormat;
    }
    //endregion

    //region Fields
    private String entityIndex;
    private String relationIndex;
    private String insightIndex;
    private String referenceIndex;
    private String idFormat;
    //endregion
}
