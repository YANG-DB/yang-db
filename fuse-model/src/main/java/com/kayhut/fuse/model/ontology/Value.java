package com.kayhut.fuse.model.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Value {

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "Value [val = "+val+", name = "+name+"]";
    }

    //region Fields
    private int val;
    private String name;
    //endregion
}
