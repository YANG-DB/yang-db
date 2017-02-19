package org.kayhut.fuse.model.query;

import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
public class RelationshipProperty extends ElementBase {


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

    public List<PropertyCondition> getCond() {
        return cond;
    }

    public void setCond(List<PropertyCondition> cond) {
        this.cond = cond;
    }

    public int getBelow() {
        return below;
    }

    public void setBelow(int below) {
        this.below = below;
    }

    //region Fields
    private int	pType;
    private String pTag;
    private List<PropertyCondition> cond;
    private int	below;
    //endregion


}
