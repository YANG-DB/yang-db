package com.fuse.domain.knowledge.datagen.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Entity extends KnowledgeEntityBase {
    private static final String entityType = "entity";

    //region Constructors
    public Entity() {
        super(entityType);
    }

    public Entity(String logicalId, String context, String category) {
        this(logicalId, context, category, null);
    }

    public Entity(String logicalId, String context, String category, KnowledgeEntityBase.Metadata metadata) {
        super(entityType, metadata);
        this.logicalId = logicalId;
        this.context = context;
        this.category = category;
    }
    //endregion

    //region Properties
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLogicalId() {
        return logicalId;
    }

    public void setLogicalId(String logicalId) {
        this.logicalId = logicalId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
    //endregion

    //region Fields
    private String category;
    private String logicalId;
    private String context;
    //endregion
}
