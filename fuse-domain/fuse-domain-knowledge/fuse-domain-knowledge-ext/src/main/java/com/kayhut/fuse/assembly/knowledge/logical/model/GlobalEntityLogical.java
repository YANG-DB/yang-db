package com.kayhut.fuse.assembly.knowledge.logical.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class GlobalEntityLogical extends ElementBaseLogical {
    //region Constructors
    public GlobalEntityLogical(String id, String category, String title, Set<String> nicknames, Metadata metadata) {
        super(metadata);
        this.id = id;
        this.category = category;
        this.title = title;
        this.nicknames = nicknames;
    }

    public GlobalEntityLogical(String id, String category, Metadata metadata) {
        super(metadata);
        this.id = id;
        this.category = category;
    }

    public GlobalEntityLogical(String id) {
        super(null);
        this.id = id;
    }

    //endregion

    //region Properties
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<String> getNicknames() {
        return nicknames;
    }

    public void setNicknames(HashSet<String> nicknames) {
        this.nicknames = nicknames;
    }

    public List<PovLogical> getPovs() {
        return povs;
    }

    public void setPovs(List<PovLogical> povs) {
        this.povs = povs;
    }

    //endregion

    //region Fields
    private String id;
    private String category;
    private String title;
    private Set<String> nicknames = new HashSet<>();
    private List<PovLogical> povs = new ArrayList<>();
    //endregion
}
