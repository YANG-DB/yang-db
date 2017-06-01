package com.kayhut.fuse.stat.model.result;

import com.kayhut.fuse.stat.model.enums.DataType;

/**
 * Created by benishue on 24/05/2017.
 */
public class StatGlobalCardinalityResult extends StatResultBase{

    //region Ctors
    public StatGlobalCardinalityResult() {
        super();
    }

    public StatGlobalCardinalityResult(String index, String type, String field, String direction, long count, long cardinality) {
        super(index, type, field, field + "_" + direction, DataType.string, count, cardinality);
        this.direction = direction;
    }

    //endregion

    //region Getter & Setters
    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
    //endregion

    //region Fields
    private String direction;
    //endregion
}
