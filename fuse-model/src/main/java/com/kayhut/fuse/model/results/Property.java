package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Property {

    //region Properties
    public int getpType() {
        return pType;
    }

    public void setpType(int pType) {
        this.pType = pType;
    }

    public String getAgg() {
        return agg;
    }

    public void setAgg(String agg) {
        this.agg = agg;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString()
    {
        return "Property [agg = "+agg+", value = "+value+", pType = "+pType+"]";
    }
    //endregion

    //region Fields
    private int pType;
    private String agg;
    private Object value;
    //endregion

}
