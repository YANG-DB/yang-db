package com.kayhut.fuse.model.query.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.Below;

/**
 * Created by user on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ETyped extends EEntityBase implements Typed {
    //region Constructors
    public ETyped() {}

    public ETyped(int eNum, String eTag, int eType, int next, int b) {
        super(eNum, eTag, next, b);
        this.eType = eType;
    }
    //endregion

    //region Properties
    public int geteType() {
        return eType;
    }

    public void seteType(int eType) {
        this.eType = eType;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        ETyped eTyped = (ETyped) o;

        return eType == eTyped.eType;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eType;
        return result;
    }
    //endregion

    //region Fields
    private int	eType;
    //endregion
}
