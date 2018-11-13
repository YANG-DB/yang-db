package com.kayhut.fuse.model.query.quant;

/*-
 * #%L
 * Quant2.java - fuse-model - kayhut - 2,016
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

import java.util.Collections;
import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Quant2 extends QuantBase {

    public List<Integer> getNext() {
        return next;
    }

    public void setNext(List<Integer> next) {
        this.next = next;
    }

    @Override
    public boolean hasNext() {
        return !next.isEmpty();
    }

    //region Fields
    private List<Integer> next = Collections.emptyList();
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Quant2 quant2 = (Quant2) o;

        return next.equals(quant2.next);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + next.hashCode();
        return result;
    }
}
