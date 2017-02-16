package org.kayhut.fuse.model.queryDTO;

/**
 * Created by User on 16/02/2017.
 */
public class QueryElementBase {
    //region Properties
    public int geteNum() {
        return eNum;
    }

    public void seteNum(int eNum) {
        this.eNum = eNum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    //endregion

    //region Fields
    private int eNum;
    private String type;
    //endregion
}
