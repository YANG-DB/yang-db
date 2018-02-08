package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moti on 5/17/2017.
 */
public class BasePropGroup<T extends BaseProp> extends EBase {
    //region Constructors
    public BasePropGroup() {
        this.props = new ArrayList<>();
    }

    public BasePropGroup(List<T> props) {
        this(0, props);
    }

    public BasePropGroup(int eNum, List<T> props) {
        super(eNum);
        this.props = Stream.ofAll(props).toJavaList();
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BasePropGroup that = (BasePropGroup) o;

        return props != null ? props.equals(that.props) : that.props == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (props != null ? props.hashCode() : 0);
        return result;
    }
    //endregion

    //region Properties
    public List<T> getProps() {
        return props;
    }
    //endregion

    //Region Fields
    protected List<T> props;
    //endregion

    public static <T extends BaseProp> BasePropGroup<T> cloneMe(BasePropGroup<T> group ) {
        ArrayList<T> eProps = new ArrayList<T>(group.props);
        BasePropGroup<T> clone = new BasePropGroup<>();
        clone.props = eProps;
        clone.seteNum(group.geteNum());
        return clone;
    }
}

