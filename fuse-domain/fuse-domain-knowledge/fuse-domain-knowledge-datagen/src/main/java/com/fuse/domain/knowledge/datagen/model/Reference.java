package com.fuse.domain.knowledge.datagen.model;

import java.util.Date;

public class Reference extends KnowledgeEntityBase {
    private static final String entityType = "reference";

    //region Constructors
    public Reference() {
        super(entityType);
    }

    public Reference(String title, String content, String url, String system) {
        this(title, content, url, system, null);
    }

    public Reference(String title, String content, String url, String system, KnowledgeEntityBase.Metadata metadata) {
        super(entityType, metadata);
        this.title = title;
        this.content = content;
        this.url = url;
        this.system = system;
    }
    //endregion

    //region Properties
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }
    //endregion

    //region Fields
    private String title;
    private String content;
    private String url;
    private String system;
    //endregion
}
