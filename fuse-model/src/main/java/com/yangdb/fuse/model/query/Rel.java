package com.yangdb.fuse.model.query;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

/*-
 *
 * Rel.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.yangdb.fuse.model.Below;
import com.yangdb.fuse.model.Next;
import com.yangdb.fuse.model.Tagged;
import com.yangdb.fuse.model.query.entity.Typed;

/**
 * Created by lior.perry on 16-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "RelPattern", value = RelPattern.class)})
public class Rel extends EBase implements Next<Integer>, Below<Integer> ,Typed.rTyped, Tagged {

    @Override
    public String getTyped() {
        return getrType();
    }

    @Override
    public String geteTag() {
        return getWrapper();
    }

    @Override
    public void seteTag(String eTag) {
        this.setWrapper(eTag);
    }

    public enum Direction {
        R,
        L,
        RL;

        public Direction reverse() {
            if (this == RL)
                return R;
            return L == this ? R : L;
        }
    }

    //region Constructors
    public Rel() {
    }

    public Rel(int eNum, String rType, Direction dir, String wrapper, int next) {
        this(eNum,rType,dir,wrapper,next,-1);
    }

    public Rel(int eNum, String rType, Direction dir, String wrapper, int next, int b) {
        super(eNum);
        this.rType = rType;
        this.dir = dir;
        this.wrapper = wrapper;
        this.next = next;
        this.b = b;
    }
    //endregion

    //region Properties
    public String getrType() {
        return rType;
    }

    public void setrType(String rType) {
        this.rType = rType;
    }

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }

    public String getWrapper() {
        return wrapper;
    }

    public void setWrapper(String wrapper) {
        this.wrapper = wrapper;
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

    //region Override Methods
    @Override
    public Rel clone() {
        return clone(geteNum());
    }

    @Override
    public Rel clone(int eNum) {
        return new Rel(eNum,getrType(),getDir(),getWrapper(),getNext(),getB());
    }
    //endregion

    //region Fields
    private String rType;
    private Direction dir;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String wrapper;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Rel rel = (Rel) o;

        if (!rType.equals(rel.rType)) return false;
        if (next != rel.next) return false;
        if (b != rel.b) return false;
        if (dir != rel.dir) return false;
        return wrapper != null ? wrapper.equals(rel.wrapper) : rel.wrapper == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + rType.hashCode();
        result = 31 * result + dir.hashCode();
        result = 31 * result + (wrapper != null ? wrapper.hashCode() : 0);
        result = 31 * result + next;
        result = 31 * result + b;
        return result;
    }
}
