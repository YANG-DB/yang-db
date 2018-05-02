package com.kayhut.fuse.model.query.properties;

import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by moti on 5/17/2017.
 */
public abstract class BasePropGroup<T extends BaseProp, S extends BasePropGroup<T, S>> extends EBase {
    //region Constructors
    public BasePropGroup() {
        this(Collections.emptyList());
    }

    public BasePropGroup(int eNum) {
        this(eNum, Collections.emptyList());
    }

    public BasePropGroup(T...props) {
        this(Stream.of(props));
    }

    public BasePropGroup(Iterable<T> props) {
        this(0, props);
    }

    public BasePropGroup(int eNum, T...props) {
        this(eNum, Stream.of(props));
    }

    public BasePropGroup(int eNum, Iterable<T> props) {
        this(eNum, QuantType.all, props);
    }

    public BasePropGroup(int eNum, QuantType quantType, Iterable<T> props) {
        this(eNum, quantType, props, Collections.emptyList());
    }

    public BasePropGroup(int eNum, QuantType quantType, Iterable<T> props, Iterable<S> groups) {
        super(eNum);
        this.quantType = quantType;
        this.props = Stream.ofAll(props).toJavaList();
        this.groups = Stream.ofAll(groups).toJavaList();
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if (!super.equals(o)) {
            return false;
        }

        BasePropGroup that = (BasePropGroup) o;

        if (!this.quantType.equals(that.quantType)) {
            return false;
        }

        if (!(this.props != null ? this.props.equals(that.props) : that.props == null)) {
            return false;
        }

        if (!(this.groups != null ? this.groups.equals(that.groups) : that.groups == null)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.quantType.hashCode();
        result = 31 * result + (this.props != null ? this.props.hashCode() : 0);
        result = 31 * result + (this.groups != null ? this.groups.hashCode() : 0);
        return result;
    }
    //endregion

    //region Properties
    public List<T> getProps() {
        return props;
    }

    public QuantType getQuantType() {
        return quantType;
    }

    public void setQuantType(QuantType quantType) {
        this.quantType = quantType;
    }

    public List<S> getGroups() {
        return groups;
    }
    //endregion

    //Region Fields
    protected List<T> props;
    protected QuantType quantType;
    protected List<S> groups;
    //endregion
}

