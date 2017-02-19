package org.kayhut.fuse.model.query;

import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
public class RelProp extends EBase {


    public int getpType() {
        return pType;
    }

    public void setpType(int pType) {
        this.pType = pType;
    }

    public String getpTag() {
        return pTag;
    }

    public void setpTag(String pTag) {
        this.pTag = pTag;
    }

    public List<PropCondition> getCond() {
        return cond;
    }

    public void setCond(List<PropCondition> cond) {
        this.cond = cond;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public String getF() {
        return f;
    }

    public void setF(String f) {
        this.f = f;
    }

    //region Fields
    private int	pType;
    private String f;
    private String pTag;
    private List<PropCondition> cond;
    private int b;
    //endregion


}
