package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.collection.Stream;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Property {
    //region Constructors
    public Property() {

    }

    public Property(String pType, Object value) {
        this(pType, null, value);
    }

    public Property(String pType, String agg, Object value) {
        this.pType = pType;
        this.agg = agg;
        this.value = value;
    }
    //endregion

    //region Properties
    public String getpType() {
        return pType;
    }

    public void setpType(String pType) {
        this.pType = pType;
    }

    public String getAgg() {
        return agg;
    }

    public void setAgg(String agg) {
        this.agg = agg;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return String.format("Property [pType = %s, value = %s]", this.pType, this.value);
    }
    //endregion

    //region Fields
    private String pType;
    private String agg;
    private Object value;
    //endregion

    public static Optional<Property> findAny(Collection<Property> properties, Predicate<Property> predicate) {
        return Stream.ofAll(properties).filter(predicate).toJavaOptional();
    }
}
