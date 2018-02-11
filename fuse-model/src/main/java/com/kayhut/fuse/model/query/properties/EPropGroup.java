package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.quant.QuantType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by benishue on 25-Apr-17.
 */
public class EPropGroup extends BasePropGroup<EProp, EPropGroup> {
    //region Constructors
    public EPropGroup() {
        super(Collections.emptyList());
    }

    public EPropGroup(int eNum) {
        super(eNum);
    }

    public EPropGroup(Iterable<EProp> props) {
        super(0, props);
    }

    public EPropGroup(int eNum, Iterable<EProp> props) {
        super(eNum, props);
    }

    public EPropGroup(int eNum, QuantType quantType, Iterable<EProp> props) {
        super(eNum, quantType, props, Collections.emptyList());
    }

    public EPropGroup(int eNum, QuantType quantType, Iterable<EProp> props, Iterable<EPropGroup> groups) {
        super(eNum, quantType, props, groups);
    }
    //endregion

    //region Override Methods
    @Override
    public EPropGroup clone() {
        return new EPropGroup(
                this.geteNum(),
                this.getQuantType(),
                this.getProps(),
                this.getGroups());
    }
    //endregion
}
