package com.kayhut.fuse.assembly.knowledge.logical.model;

import java.util.HashMap;

public class FileLogical extends ElementBaseLogical {
    public FileLogical(String id, String path, String displayName, String mimeType, String category, String description, Metadata metadata) {
        super(metadata);
        this.id = id;
        this.path = path;
        this.displayName = displayName;
        this.mimeType = mimeType;
        this.category = category;
        this.description = description;
    }

    //region Properties
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //endregion

    //region Fields
    private String id;
    private String name;
    private String path;
    private String displayName;
    private String mimeType;
    private String category;
    private String description;

    //endregion
}
