package com.kayhut.fuse.model.query.properties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 25-Apr-17.
 */
public class EPropGroup extends BasePropGroup<EProp> {
    public EPropGroup() {}

    public EPropGroup(List<EProp> props) {
        super(props);
    }

    @Override
    public EPropGroup clone() {
        EPropGroup propGroup = new EPropGroup();
        propGroup.seteNum(geteNum());
        propGroup.props = new ArrayList<>(getProps());
        return propGroup;

    }
}
