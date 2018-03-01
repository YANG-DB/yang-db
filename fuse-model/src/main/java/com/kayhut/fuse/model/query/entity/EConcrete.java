package com.kayhut.fuse.model.query.entity;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;

/**
 * Created by user on 16-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EConcrete extends ETyped implements Typed.eTyped{
    //region Constructors
    public EConcrete() {}

    public EConcrete(int eNum, String eTag, String eType, String eID, String eName, int next, int b) {
        super(eNum, eTag, eType, next, b);
        this.eID = eID;
        this.eName = eName;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;

        EConcrete eConcrete = (EConcrete) o;

        if (!eID.equals(eConcrete.eID)) return false;
        return eName.equals(eConcrete.eName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eID.hashCode();
        result = 31 * result + eName.hashCode();
        return result;
    }
    //endregion

    //region Properties
    public String geteID() {
        return eID;
    }

    public void seteID(String eID) {
        this.eID = eID;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }
    //endregion

    //region Fields
    private String eID;
    private String eName;
    //endregion
}
