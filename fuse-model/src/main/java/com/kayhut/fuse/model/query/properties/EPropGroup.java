package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.EBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 25-Apr-17.
 */
public class EPropGroup  extends EBase {

    public EPropGroup() {
        this.eProps = new ArrayList<>();
    }

    public List<EProp> geteProps() {
        return eProps;
    }

    public void seteProps(List<EProp> eProps) {
        this.eProps = eProps;
    }

    //Region Fields
    private List<EProp> eProps;
    //endregion

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EPropGroup that = (EPropGroup) o;

        return eProps != null ? eProps.equals(that.eProps) : that.eProps == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (eProps != null ? eProps.hashCode() : 0);
        return result;
    }

    public EPropGroup clone() {
        ArrayList<EProp> eProps = new ArrayList<>(geteProps());
        EPropGroup group = new EPropGroup();
        group.seteProps(eProps);
        group.seteNum(geteNum());
        return group;
    }
}
