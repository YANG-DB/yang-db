package com.kayhut.fuse.stat.model.result;

import com.kayhut.fuse.stat.model.enums.DataType;

/**
 * Created by benishue on 24/05/2017.
 */
public class StatTermResult <T> extends StatResultBase{

    //region Ctors
    public StatTermResult() {
    }

    public StatTermResult(String index, String type, String field, String key, DataType dataType, T term, long count, long cardinality) {
        super(index, type, field, key, dataType, count, cardinality);
        this.term = term;
    }

    //endregion

    //region Getter & Setters
    public T getTerm() {
        return term;
    }

    public void setTerm(T term) {
        this.term = term;
    }
    //endregion

    //region Fields
    private T term;
    //endregion
}
