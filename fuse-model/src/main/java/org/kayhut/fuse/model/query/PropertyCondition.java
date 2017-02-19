package org.kayhut.fuse.model.query;

/**
 * Created by benishue on 17/02/2017.
 */
public class PropertyCondition {


    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getL() {
        return l;
    }

    public void setL(String l) {
        this.l = l;
    }

    //region Fields
    private String op;
    private String r;
    private String l;
    //endregion

}
