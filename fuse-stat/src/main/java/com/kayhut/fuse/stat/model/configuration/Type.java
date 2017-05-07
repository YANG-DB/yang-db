package com.kayhut.fuse.stat.model.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class Type {

    public Type() {
    }

    public Type(String type, List<Field> fields) {
        this.type = type;
        this.fields = fields;
    }

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


    //region Fields
    private String type;
    private List<Field> fields;
    //endregion


}
