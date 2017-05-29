package com.kayhut.fuse.stat.model.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class Type {

    //region Ctrs
    public Type() {
        //needed for Jackson
    }

    public Type(String type, List<Field> fields) {
        this.type = type;
        this.fields = fields;
    }

    public Type(String type) {
        this.type = type;
        this.fields = new ArrayList<>();
    }
    //endregion

    //region Getters & Setters
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("fields")
    public List<Field> getFields() {
        return fields;
    }

    @JsonProperty("fields")
    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
    //endregion

    //region Fields
    private String type;
    private List<Field> fields;
    //endregion

}
