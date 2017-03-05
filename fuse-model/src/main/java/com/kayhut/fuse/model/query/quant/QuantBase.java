package com.kayhut.fuse.model.query.quant;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by benishue on 17/02/2017.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QuantBase extends EBase {

    public String getqType() {
        return qType;
    }

    public void setqType(String qType) {
        this.qType = qType;
    }

    //region Fields
    private String qType;
    //endregion

}
