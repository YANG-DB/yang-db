package com.kayhut.fuse.model.query.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by user on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EUntyped extends EEntityBase {
    //region Constructors
    public EUntyped() {
        super();
        this.vTypes = new ArrayList<>();
        this.nvTypes = new ArrayList<>();
    }

    public EUntyped(int eNum, String eTag, int next, int b) {
        this(eNum, eTag, Collections.emptyList(), Collections.emptyList(), next, b);
    }

    public EUntyped(int eNum, String eTag, Iterable<String> vTypes, Iterable<String> nvTypes, int next, int b) {
        this(eNum, eTag, vTypes, nvTypes, Collections.emptyList(), next, b);
    }

    public EUntyped(int eNum, String eTag, List<String> reportProps, int next, int b) {
        this(eNum, eTag, Collections.emptyList(), Collections.emptyList(), reportProps, next, b);
    }

    public EUntyped(int eNum, String eTag, Iterable<String> vTypes, Iterable<String> nvTypes, List<String> reportProps, int next, int b) {
        super(eNum, eTag, reportProps, next, b);
        this.vTypes = Stream.ofAll(vTypes).toJavaList();
        this.nvTypes = Stream.ofAll(nvTypes).toJavaList();
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;

        EUntyped eUntyped = (EUntyped) o;

        if (vTypes != null ? !vTypes.equals(eUntyped.vTypes) : eUntyped.vTypes != null) return false;
        return nvTypes != null ? nvTypes.equals(eUntyped.nvTypes) : eUntyped.nvTypes == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (vTypes != null ? vTypes.hashCode() : 0);
        result = 31 * result + (nvTypes != null ? nvTypes.hashCode() : 0);
        return result;
    }
    //endregion

    //region Properties
    public List<String> getvTypes() {
        return vTypes;
    }

    public void setvTypes(List<String> vTypes) {
        this.vTypes = vTypes;
    }

    public List<String> getNvTypes() {
        return nvTypes;
    }

    public void setNvTypes(List<String> nvTypes) {
        this.nvTypes = nvTypes;
    }
    //endregion

    //region Fields
    private List<String> vTypes;
    private	List<String> nvTypes;
    //endregion
}
