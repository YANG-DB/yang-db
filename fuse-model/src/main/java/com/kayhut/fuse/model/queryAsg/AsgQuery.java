package com.kayhut.fuse.model.queryAsg;

import java.util.List;

/**
 * Created by benishue on 23-Feb-17.
 */
public class AsgQuery {

    public String getOnt() {
        return ont;
    }

    public void setOnt(String ont) {
        this.ont = ont;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EBaseAsg getStart() {
        return start;
    }

    public void setStart(EBaseAsg start) {
        this.start = start;
    }

    //region Fields
    private String ont;
    private String name;
    private EBaseAsg start;
    //endregion


}
