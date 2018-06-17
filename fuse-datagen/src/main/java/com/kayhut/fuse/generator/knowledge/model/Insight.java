package com.kayhut.fuse.generator.knowledge.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Insight extends KnowledgeEntityBase {
    private static final String entityType = "insight";

    //region Constructors
    public Insight() {
        super(entityType);
    }

    public Insight(String context, String content, Iterable<String> entityIds) {
        this(context, content, entityIds, null);
    }

    public Insight(String context, String content, Iterable<String> entityIds, KnowledgeEntityBase.Metadata metadata) {
        super(entityType, metadata);
        this.context = context;
        this.content = content;
        this.entityIds = entityIds;
    }
    //endregion

    //region Properties
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Iterable<String> getEntityIds() {
        return entityIds;
    }

    public void setEntityIds(Iterable<String> entityIds) {
        this.entityIds = entityIds;
    }
    //endregion

    //region Fields
    private String context;
    private String content;
    private Iterable<String> entityIds;
    //endregion
}
