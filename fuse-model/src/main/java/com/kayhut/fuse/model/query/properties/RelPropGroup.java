package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.quant.QuantType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by benishue on 25-Apr-17.
 */
public class RelPropGroup extends BasePropGroup<RelProp, RelPropGroup> {
    //region Constructors
    public RelPropGroup() {
        super(Collections.emptyList());
    }

    public RelPropGroup(int eNum) {
        super(eNum);
    }

    public RelPropGroup(RelProp...props) {
        super(props);
    }

    public RelPropGroup(Iterable<RelProp> props) {
        super(0, props);
    }

    public RelPropGroup(int eNum, RelProp...props) {
        super(eNum, props);
    }

    public RelPropGroup(int eNum, Iterable<RelProp> props) {
        super(eNum, QuantType.all, props);
    }

    public RelPropGroup(int eNum, QuantType quantType, Iterable<RelProp> props) {
        super(eNum, quantType, props, Collections.emptyList());
    }

    public RelPropGroup(int eNum, QuantType quantType, Iterable<RelProp> props, Iterable<RelPropGroup> groups) {
        super(eNum, quantType, props, groups);
    }
    //endregion

    //region Override Methods
    @Override
    public RelPropGroup clone() {
        RelPropGroup propGroup = new RelPropGroup();
        propGroup.seteNum(geteNum());
        propGroup.props = new ArrayList<>(getProps());
        return propGroup;

    }
    //endregion

    public static RelPropGroup of(List<RelProp> props) {
        return new RelPropGroup(props);
    }
}
