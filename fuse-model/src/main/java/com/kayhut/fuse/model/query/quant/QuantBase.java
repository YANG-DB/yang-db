package com.kayhut.fuse.model.query.quant;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by benishue on 17/02/2017.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QuantBase extends EBase {
    //region Constructors
    public QuantBase() {
        super();
    }

    public QuantBase(int eNum, QuantType qType) {
        super(eNum);
        this.qType = qType;
    }
    //endregion

    //region Properties
    public QuantType getqType() {
        return qType;
    }

    public void setqType(QuantType qType) {
        this.qType = qType;
    }
    //endregion

    //region Fields
    private QuantType qType;
    //endregion

}
