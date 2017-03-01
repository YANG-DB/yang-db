package com.kayhut.fuse.model.query;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by user on 19-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HQuant extends QuantBase {
    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    //region Fields
    private int b;
    //endregion
}
