package com.fuse.domain.knowledge.datagen;

/**
 * Created by Roman on 6/22/2018.
 */
public class ContextGenerationConfiguration {
    //region Constructors
    public ContextGenerationConfiguration() {}

    public ContextGenerationConfiguration(
            String fromContext,
            String toContext,
            double scaleFactor,
            double entityOverlapFactor,
            double entityValueOverlapFactor,
            int entityValueIdFrom,
            int relationValueIdFrom,
            int relationIdFrom,
            int insightIdFrom,
            int referenceIdFrom) {

        this.fromContext = fromContext;
        this.toContext = toContext;

        this.scaleFactor = scaleFactor;
        this.entityOverlapFactor = entityOverlapFactor;
        this.entityValueOverlapFactor = entityValueOverlapFactor;

        this.entityValueIdFrom = entityValueIdFrom;
        this.relationValueIdFrom = relationValueIdFrom;
        this.relationIdFrom = relationIdFrom;
        this.insightIdFrom = insightIdFrom;
        this.referenceIdFrom = referenceIdFrom;
    }
    //endregion

    //region Properties
    public String getFromContext() {
        return fromContext;
    }

    public void setFromContext(String fromContext) {
        this.fromContext = fromContext;
    }

    public String getToContext() {
        return toContext;
    }

    public void setToContext(String toContext) {
        this.toContext = toContext;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public double getEntityOverlapFactor() {
        return entityOverlapFactor;
    }

    public void setEntityOverlapFactor(double entityOverlapFactor) {
        this.entityOverlapFactor = entityOverlapFactor;
    }

    public double getEntityValueOverlapFactor() {
        return entityValueOverlapFactor;
    }

    public void setEntityValueOverlapFactor(double entityValueOverlapFactor) {
        this.entityValueOverlapFactor = entityValueOverlapFactor;
    }

    public int getEntityValueIdFrom() {
        return entityValueIdFrom;
    }

    public void setEntityValueIdFrom(int entityValueIdFrom) {
        this.entityValueIdFrom = entityValueIdFrom;
    }

    public int getRelationValueIdFrom() {
        return relationValueIdFrom;
    }

    public void setRelationValueIdFrom(int relationValueIdFrom) {
        this.relationValueIdFrom = relationValueIdFrom;
    }

    public int getRelationIdFrom() {
        return relationIdFrom;
    }

    public void setRelationIdFrom(int relationIdFrom) {
        this.relationIdFrom = relationIdFrom;
    }

    public int getReferenceIdFrom() {
        return referenceIdFrom;
    }

    public void setReferenceIdFrom(int referenceIdFrom) {
        this.referenceIdFrom = referenceIdFrom;
    }

    public int getInsightIdFrom() {
        return insightIdFrom;
    }

    public void setInsightIdFrom(int insightIdFrom) {
        this.insightIdFrom = insightIdFrom;
    }

    //endregion

    //region Fields
    private String fromContext;
    private String toContext;

    private double scaleFactor;
    private double entityOverlapFactor;
    private double entityValueOverlapFactor;

    private int entityValueIdFrom;
    private int relationValueIdFrom;
    private int relationIdFrom;
    private int referenceIdFrom;
    private int insightIdFrom;
    //endregion
}
