package com.yangdb.fuse.model.query;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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
import javaslang.collection.Stream;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RelUntyped extends Rel implements Untyped{

    public RelUntyped() {
        super();
        this.vTypes = new HashSet<>();
        this.nvTypes = new HashSet<>();
    }

    public RelUntyped(int eNum, Set<String> rType, Direction dir, String wrapper, int next) {
        this(eNum, rType, Collections.emptySet(),dir,wrapper, next);
    }

    public RelUntyped(int eNum, String rType, Direction dir, String wrapper, int next) {
        this(eNum, Collections.singleton(rType), Collections.emptySet(),dir,wrapper, next);
    }

    public RelUntyped(int eNum, Set<String> rType,Set<String> nvType, Direction dir, String wrapper, int next) {
        super(eNum,null,dir,wrapper,next);
        this.vTypes = Stream.ofAll(rType).toJavaSet();
        this.nvTypes = Stream.ofAll(nvType).toJavaSet();
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;

        RelUntyped eUntyped = (RelUntyped) o;

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
    public RelUntyped clone() {
        return clone(geteNum());
    }

    @Override
    public RelUntyped clone(int eNum) {
        final RelUntyped clone = new RelUntyped();
        clone.seteTag(geteTag());
        clone.seteNum(eNum);
        clone.setB(getB());
        clone.setDir(getDir());
        clone.setWrapper(getWrapper());

        clone.nvTypes = new HashSet<>(nvTypes);
        clone.vTypes = new HashSet<>(vTypes);
        return clone;
    }

//endregion

    //region Properties

    @Override
    public void setrType(String rType) {
        getvTypes().add(rType);
    }

    @Override
    public String getTyped() {
        return getvTypes().iterator().hasNext() ? getvTypes().iterator().next() : null;
    }

    public Set<String> getvTypes() {
        return vTypes;
    }

    public void setvTypes(Set<String> vTypes) {
        this.vTypes = vTypes;
    }

    public Set<String> getNvTypes() {
        return nvTypes;
    }

    public void setNvTypes(Set<String> nvTypes) {
        this.nvTypes = nvTypes;
    }
    //endregion

    //region Fields
    private Set<String> vTypes;
    private	Set<String> nvTypes;
    //endregion

}
