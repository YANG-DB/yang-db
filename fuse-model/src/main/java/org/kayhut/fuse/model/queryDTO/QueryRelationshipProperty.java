package org.kayhut.fuse.model.queryDTO;

import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
public class QueryRelationshipProperty extends QueryElementBase {


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

    public List<QueryPropertyCondition> getCond() {
        return cond;
    }

    public void setCond(List<QueryPropertyCondition> cond) {
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
    private List<QueryPropertyCondition> cond;
    private int	below;
    //endregion


}
