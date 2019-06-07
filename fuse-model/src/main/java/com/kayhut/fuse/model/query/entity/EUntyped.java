package com.kayhut.fuse.model.query.entity;

/*-
 * #%L
 * EUntyped.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lior.perry on 16-Feb-17.
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
        super(eNum, eTag, next, b);
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

    @Override
    public EBase clone() {
        return clone(geteNum());
    }

    @Override
    public EUntyped clone(int eNum) {
        final EUntyped clone = new EUntyped();
        clone.seteTag(geteTag());
        clone.seteNum(eNum);
        clone.nvTypes = new ArrayList<>(nvTypes);
        clone.vTypes = new ArrayList<>(vTypes);
        return clone;
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
