package com.kayhut.fuse.stat.model.result;

import com.kayhut.fuse.stat.model.enums.DataType;

/**
 * Created by benishue on 03-May-17.
 */
public class StatRangeResult<T> extends StatResultBase {

    //region Ctors
    public StatRangeResult() {
        super();
    }

    public StatRangeResult(String index, String type, String field, String key, DataType dataType, T lowerBound, T upperBound, long count, long cardinality) {
        super(index, type, field, key, dataType, count, cardinality);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    //endregion

    //region Getter & Setters
    public T getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(T lowerBound) {
        this.lowerBound = lowerBound;
    }

    public T getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(T upperBound) {
        this.upperBound = upperBound;
    }
    //endregion

    //region Fields
    private T lowerBound;
    private T upperBound;
    //endregion
}
