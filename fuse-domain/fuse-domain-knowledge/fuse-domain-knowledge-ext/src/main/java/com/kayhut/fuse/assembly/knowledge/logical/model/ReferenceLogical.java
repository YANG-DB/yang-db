package com.kayhut.fuse.assembly.knowledge.logical.model;

public class ReferenceLogical extends LogicalItemBase {

    //region Constructors
    public ReferenceLogical(String title, String url, String system, String content, Metadata metadata) {
        super(metadata);
        this.title = title;
        this.url = url;
        this.system = system;
        this.content = content;
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
