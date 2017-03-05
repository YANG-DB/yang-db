package com.kayhut.fuse.model.query.combiner;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CombBase extends EBase {
    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    //region Fields
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    //endregion

}
