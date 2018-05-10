package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.Collections;

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

    public EPropGroup(EProp...props) {
        super(props);
    }

    public EPropGroup(Iterable<EProp> props) {
        super(0, props);
    }

    public EPropGroup(int eNum, EProp...props) {
        super(eNum, props);
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

    //region Static
    public static EPropGroup of(int eNum, EProp...props) {
        return new EPropGroup(eNum, props);
    }

    public static EPropGroup of(int eNum, QuantType quantType, EProp...props) {
        return new EPropGroup(eNum, quantType, Stream.of(props));
    }

    public static EPropGroup of(int eNum, EPropGroup...groups) {
        return new EPropGroup(eNum, QuantType.all, Collections.emptyList(), Stream.of(groups));
    }

    public static EPropGroup of(int eNum, QuantType quantType, EPropGroup...groups) {
        return new EPropGroup(eNum, quantType, Stream.empty(), Stream.of(groups));
    }

    public static EPropGroup of(int eNum, QuantType quantType, Iterable<EProp> props, Iterable<EPropGroup> groups) {
        return new EPropGroup(eNum, quantType, props, groups);
    }
    //endregion
}