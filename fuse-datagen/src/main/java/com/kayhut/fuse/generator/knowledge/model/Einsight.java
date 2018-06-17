package com.kayhut.fuse.generator.knowledge.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Einsight extends KnowledgeEntityBase {
    private static final String entityType = "e.insight";

    //region Constructors
    public Einsight() {
        super(entityType);
    }

    public Einsight(String entityId, String insightId) {
        super(entityType);
        this.entityId = entityId;
        this.insightId = insightId;
    }
    //endregion

    //region Properties
    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getInsightId() {
        return insightId;
    }

    public void setInsightId(String insightId) {
        this.insightId = insightId;
    }
    //endregion

    //region Fields
    private String entityId;
    private String insightId;
    //endregion
}
