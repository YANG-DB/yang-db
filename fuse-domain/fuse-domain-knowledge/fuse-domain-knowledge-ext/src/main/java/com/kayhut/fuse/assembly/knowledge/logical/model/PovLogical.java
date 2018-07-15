package com.kayhut.fuse.assembly.knowledge.logical.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PovLogical extends ElementBaseLogical {
    public PovLogical(String context, String category, Metadata metadata) {
        super(metadata);
        this.context = context;
        this.category = category;
    }

    //region Properties
    public HashMap<String, ReferenceLogical> getReferences() {
        return references;
    }

    public void setReferences(HashMap<String, ReferenceLogical> references) {
        this.references = references;
    }
    //endregion

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

    public HashMap<String, FieldLogical> getFields() {
        return fields;
    }

    public void setFields(HashMap<String, FieldLogical> fields) {
        this.fields = fields;
    }

    public HashMap<String, RelationLogical> getRelations() {
        return relations;
    }

    public void setRelations(HashMap<String, RelationLogical> relations) {
        this.relations = relations;
    }

    public HashMap<String, InsightLogical> getInsights() {
        return insights;
    }

    public void setInsights(HashMap<String, InsightLogical> insights) {
        this.insights = insights;
    }

    public HashMap<String, FileLogical> getFiles() {
        return files;
    }

    public void setFiles(HashMap<String, FileLogical> files) {
        this.files = files;
    }


    //region Fields
    private String context;
    private String category;
    // ref id is key
    private HashMap<String, ReferenceLogical> references = new HashMap<>();
    // field id is key
    private HashMap<String, FieldLogical> fields = new HashMap<>();
    // relation id is key
    private HashMap<String, RelationLogical> relations = new HashMap<>();
    // insight id is key
    private HashMap<String, InsightLogical> insights = new HashMap<>();
    // file id is key
    private HashMap<String, FileLogical> files = new HashMap<>();




    //endregion
}
