package com.yangdb.fuse.model.query.quant;

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
 * Quant1.java - fuse-model - yangdb - 2,016
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
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Quant1 extends QuantBase {
    //region Constructors
    public Quant1() {
        super();
        this.next = Collections.emptyList();
    }

    public Quant1(int eNum, QuantType quantType) {
        this(eNum,quantType,Collections.emptyList());
    }

    public Quant1(int eNum, QuantType qType, Iterable<Integer> next) {
        this(eNum,qType,next,-1);
    }

    public Quant1(int eNum, QuantType qType, Iterable<Integer> next, int b) {
        super(eNum, qType);
        this.next = Stream.ofAll(next).toJavaList();
        this.b = b;
    }

    //endregion


    @Override
    public Quant1 clone() {
        return clone(geteNum());
    }

    @Override
    public Quant1 clone(int eNum) {
        final Quant1 clone = new Quant1();
        clone.seteNum(eNum);
        clone.setqType(getqType());
        return clone;
    }

    //region Properties
    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public List<Integer> getNext() {
        return next;
    }

    public void setNext(List<Integer> next) {
        this.next = next;
    }

    public void addNext(int next) {
        this.next.add(next);
    }

    @Override
    public boolean hasNext() {
        return !next.isEmpty();
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Quant1 quant1 = (Quant1) o;

        if (b != quant1.b) return false;
        return next.equals(quant1.next);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + b;
        result = 31 * result + next.hashCode();
        return result;
    }
    //endregion

    //region Fields
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;
    private List<Integer> next;
    //endregion
}
