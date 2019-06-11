package com.kayhut.fuse.model.query;

/*-
 * #%L
 * Start.java - fuse-model - kayhut - 2,016
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
import com.kayhut.fuse.model.Next;

/**
 * Created by lior.perry on 16/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Start extends EBase implements Next<Integer> {
    //region Constructors
    public Start() {}
    public Start(int eNum, int next) {
        super(eNum);
        this.next = next;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Start start = (Start) o;

        if (next != start.next) return false;
        return b == start.b;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + next;
        result = 31 * result + b;
        return result;
    }
    //endregion

    //region Properties
    public Integer getNext() {
        return next;
    }

    @Override
    public boolean hasNext() {
        return next > -1;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
    //endregion

    //region Fields
    private int next;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;
    //endregion
}
