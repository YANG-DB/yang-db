package com.kayhut.fuse.model.query.optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;

import java.util.List;

/**
 * Created by roman.margolis on 20/11/2017.
 */
public class Optional extends EBase {
    //region Constructors
    public Optional() {

    }

    public Optional(int eNum, int next) {
        super(eNum);
        this.next = next;
    }
    //endregion

    //region Properties
    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }
    //endregion

    //region Fields
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    //endregion
}
