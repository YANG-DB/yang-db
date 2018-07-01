package com.fuse.domain.knowledge.datagen.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Erelation extends KnowledgeEntityBase {
    private static final String entityType = "e.relation";

    //region Constructors
    public Erelation() {
        super(entityType);
    }

    public Erelation(
            String relationId,
            String context,
            String category,
            String entityAId,
            String entityACategory,
            String entityBId,
            String entityBCategory,
            String direction) {
        this(relationId, context, category, entityAId, entityACategory, entityBId, entityBCategory, direction, null);
    }

    public Erelation(
            String relationId,
            String context,
            String category,
            String entityAId,
            String entityACategory,
            String entityBId,
            String entityBCategory,
            String direction,
            KnowledgeEntityBase.Metadata metadata) {
        super(entityType, metadata);
        this.relationId = relationId;
        this.context = context;
        this.category = category;
        this.entityAId = entityAId;
        this.entityACategory = entityACategory;
        this.entityBId = entityBId;
        this.entityBCategory = entityBCategory;
        this.direction = direction;
    }
    //endregion

    //region Properties
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEntityAId() {
        return entityAId;
    }

    public void setEntityAId(String entityAId) {
        this.entityAId = entityAId;
    }

    public String getEntityACategory() {
        return entityACategory;
    }

    public void setEntityACategory(String entityACategory) {
        this.entityACategory = entityACategory;
    }

    public String getEntityBId() {
        return entityBId;
    }

    public void setEntityBId(String entityBId) {
        this.entityBId = entityBId;
    }

    public String getEntityBCategory() {
        return entityBCategory;
    }

    public void setEntityBCategory(String entityBCategory) {
        this.entityBCategory = entityBCategory;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getRelationId() {
        return relationId;
    }

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }
    //endregion

    //region Fields
    private String context;
    private String category;
    private String entityAId;
    private String entityACategory;
    private String entityBId;
    private String entityBCategory;
    private String direction;
    private String relationId;
    //endregion
}
