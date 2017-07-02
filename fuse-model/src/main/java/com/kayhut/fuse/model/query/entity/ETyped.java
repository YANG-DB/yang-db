package com.kayhut.fuse.model.query.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.Below;

import java.util.Collections;
import java.util.List;

/**
 * Created by user on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ETyped extends EEntityBase implements Typed.eTyped {
    //region Constructors
    public ETyped() {}

    public ETyped(int eNum, String eTag, String eType, int next, int b) {
        this(eNum, eTag, eType, Collections.emptyList(), next, b);
        this.eType = eType;
    }

    public ETyped(int eNum, String eTag, String eType, List<String> reportProps, int next, int b) {
        super(eNum, eTag, reportProps, next, b);
        this.eType = eType;
    }
    //endregion

    //region Properties
    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        ETyped eTyped = (ETyped) o;

        return eType.equals(eTyped.eType);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eType.hashCode();
        return result;
    }
    //endregion

    //region Fields
    private String	eType;
    //endregion
}
