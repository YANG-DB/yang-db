package com.kayhut.fuse.model.query.quant;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.quant.QuantBase;

import java.util.List;

/**
 * Created by user on 19-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HQuant extends QuantBase {
    public List<Integer> getB() {
        return b;
    }

    public void setB(List<Integer> b) {
        this.b = b;
    }

    //region Fields
    private List<Integer> b;
    //endregion
}
