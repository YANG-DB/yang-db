package com.kayhut.fuse.model.query.entity;

/*-
 * #%L
 * EAgg.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.query.EBase;

/**
 * Created by lior.perry on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EAgg extends EBase implements Next<Integer> {

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }

    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    @Override
    public boolean hasNext() {
        return next > -1;
    }


    //region Fields
    private	String eTag;
    private	String fName;
    private	String eName;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private	int	next;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EAgg eAgg = (EAgg) o;

        if (next != eAgg.next) return false;
        if (!eTag.equals(eAgg.eTag)) return false;
        if (!fName.equals(eAgg.fName)) return false;
        return eName.equals(eAgg.eName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eTag.hashCode();
        result = 31 * result + fName.hashCode();
        result = 31 * result + eName.hashCode();
        result = 31 * result + next;
        return result;
    }
}
