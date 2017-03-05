package com.kayhut.fuse.model.query;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Created by benishue on 02-Mar-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HComb extends EBase {
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