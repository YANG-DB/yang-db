package com.kayhut.fuse.model.query.quant;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.query.EBase;

import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class QuantBase extends EBase implements Next<List<Integer>> {
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
