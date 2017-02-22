package com.kayhut.fuse.model.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by benishue on 22-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EPair {

    public int geteTypeA() {
        return eTypeA;
    }

    public void seteTypeA(int eTypeA) {
        this.eTypeA = eTypeA;
    }

    public int geteTypeB() {
        return eTypeB;
    }

    public void seteTypeB(int eTypeB) {
        this.eTypeB = eTypeB;
    }

    @Override
    public String toString()
    {
        return "EPair [eTypeB = "+eTypeB+", eTypeA = "+eTypeA+"]";
    }

    //region Fields
    private int eTypeA;
    private int eTypeB;
    //endregion
}
