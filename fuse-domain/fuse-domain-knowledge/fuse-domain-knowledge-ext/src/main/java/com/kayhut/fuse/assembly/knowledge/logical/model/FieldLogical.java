package com.kayhut.fuse.assembly.knowledge.logical.model;

import java.util.HashMap;


public class FieldLogical extends ElementBaseLogical {
    public FieldLogical(String id, String type, String bdt) {
        super(null);
        this.id = id;
        this.type = type;
        this.bdt = bdt;
    }

    public FieldLogical(String id, String type, String bdt, ValueLogical value) {
        super(null);
        this.id = id;
        this.type = type;
        this.bdt = bdt;
        this.values.put(value.getId(), value);
    }

    public FieldLogical(String id, String type, String bdt, HashMap<String, ValueLogical> values) {
        super(null);
        this.id = id;
        this.type = type;
        this.bdt = bdt;
        this.values = values;
    }

    //region Properties

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String content) {
        this.type = content;
    }

    public String getBdt() {
        return bdt;
    }

    public void setBdt(String bdt) {
        this.bdt = bdt;
    }

    public HashMap<String, ValueLogical> getValues() {
        return values;
    }

    public void setValues(HashMap<String, ValueLogical> values) {
        this.values = values;
    }
    //endregion

    //region Fields
    private String id;
    private String type;
    private String bdt;
    private HashMap<String, ValueLogical> values;




    //endregion
}
