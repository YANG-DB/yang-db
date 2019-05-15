package com.kayhut.fuse.model.query.entity;

/*-
 * #%L
 * EEntityBase.java - fuse-model - kayhut - 2,016
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
import com.kayhut.fuse.model.Below;
import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.Tagged;
import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;

/**
 * Created by lior.perry on 27/02/2017.
 */
public abstract class EEntityBase extends EBase implements Next<Integer>, Below<Integer>, Tagged {
    //region Constructors
    public EEntityBase() {
    }

    public EEntityBase(int eNum, String eTag, int next, int b) {
        super(eNum);

        this.eTag = eTag;
        this.next = next;
        this.b = b;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;

        EEntityBase that = (EEntityBase) o;
        if (eTag == null) {
            if (that.eTag != null)
                return false;
        } else {
            if (!eTag.equals(that.eTag)) return false;
        }
        if (next != that.next) return false;
        if (b != that.b) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + next;
        result = 31 * result + b;

        result = 31 * result + (eTag != null ? eTag.hashCode() : 0);
        return result;
    }

    @Override
    public EBase clone() {
        return clone(geteNum());
    }

    @Override
    public EBase clone(int eNum) {
        return super.clone(eNum);
    }
    //endregion

    //region Properties
    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
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

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }
    //endregion

    //region Fields
    private String eTag;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next = -1;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;
    //endregion
}
