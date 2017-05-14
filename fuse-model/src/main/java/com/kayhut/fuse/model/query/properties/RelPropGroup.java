package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.EBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 25-Apr-17.
 */
public class RelPropGroup  extends EBase {

    public RelPropGroup() {
        this.rProps = new ArrayList<>();
    }

    public List<RelProp> getrProps() {
        return rProps;
    }

    public void setrProps(List<RelProp> rProps) {
        this.rProps = rProps;
    }

    //Region Fields
    private List<RelProp> rProps;
    //endregion

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RelPropGroup that = (RelPropGroup) o;

        return rProps.equals(that.rProps);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + rProps.hashCode();
        return result;
    }

    @Override
    public RelPropGroup clone() {
        RelPropGroup propGroup = new RelPropGroup();
        propGroup.seteNum(geteNum());
        propGroup.setrProps(new ArrayList<>(getrProps()));
        return propGroup;

    }
}
