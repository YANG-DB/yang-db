package com.kayhut.fuse.model.query.combiner;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.Next;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CombBase extends EBase implements Next<Integer> {
    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    //region Fields
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    //endregion

}
