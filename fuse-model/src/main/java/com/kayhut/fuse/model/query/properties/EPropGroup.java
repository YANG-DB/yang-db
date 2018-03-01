package com.kayhut.fuse.model.query.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by benishue on 25-Apr-17.
 */
public class EPropGroup extends BasePropGroup<EProp> {
    //region Constructors
    public EPropGroup() {}

    public EPropGroup(EProp...props) {
        super(props);
    }

    public EPropGroup(Iterable<EProp> props) {
        super(props);
    }

    public EPropGroup(int eNum, EProp...props) {
        super(eNum, props);
    }

    public EPropGroup(int eNum, Iterable<EProp> props) {
        super(eNum, props);
    }
    //endregion

    //region Override Methods
    @Override
    public EPropGroup clone() {
        return new EPropGroup(this.geteNum(), this.getProps());
    }
    //endregion
}
