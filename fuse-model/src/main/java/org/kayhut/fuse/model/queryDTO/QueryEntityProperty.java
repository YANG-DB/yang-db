package org.kayhut.fuse.model.queryDTO;

import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
public class QueryEntityProperty extends QueryElementBase{

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

    //region Fields
    private int pType;
    private String pTag;
    private List<QueryPropertyCondition> cond;
    //endregion

}
