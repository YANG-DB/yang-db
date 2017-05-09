package com.kayhut.fuse.model.query.entity;

import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by User on 27/02/2017.
 */
public abstract class EEntityBase extends EBase implements Next<Integer> {
    //region Properties
    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }
    //endregion

    //region Fields
    private	String eTag;
    //endregion

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EEntityBase that = (EEntityBase) o;

        return eTag.equals(that.eTag);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eTag.hashCode();
        return result;
    }
}
