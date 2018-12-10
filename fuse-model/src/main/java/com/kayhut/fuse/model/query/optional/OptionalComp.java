package com.kayhut.fuse.model.query.optional;

/*-
 * #%L
 * OptionalComp.java - fuse-model - kayhut - 2,016
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
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.EProp;

import java.util.List;

/**
 * Created by roman.margolis on 20/11/2017.
 */
public class OptionalComp extends EBase {
    //region Constructors
    public OptionalComp() {

    }

    public OptionalComp(int eNum, int next) {
        super(eNum);
        this.next = next;
    }
    //endregion

    @Override
    public OptionalComp clone() {
        return clone(geteNum());
    }

    @Override
    public OptionalComp clone(int eNum) {
        OptionalComp clone = new OptionalComp();
        clone.seteNum(eNum);
        return clone;
    }

    //region Properties
    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }
    //endregion

    //region Fields
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    //endregion
}
