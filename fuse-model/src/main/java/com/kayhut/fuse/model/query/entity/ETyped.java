package com.kayhut.fuse.model.query.entity;

/*-
 * #%L
 * ETyped.java - fuse-model - kayhut - 2,016
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
import com.kayhut.fuse.model.Below;

import java.util.Collections;
import java.util.List;

/**
 * Created by lior.perry on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ETyped extends EEntityBase implements Typed.eTyped {
    //region Constructors
    public ETyped() {}

    public ETyped(int eNum, String eTag, String eType, int next, int b) {
        super(eNum, eTag, next, b);
        this.eType = eType;
    }
    //endregion

    //region Properties
    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        ETyped eTyped = (ETyped) o;

        return eType.equals(eTyped.eType);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eType.hashCode();
        return result;
    }
    //endregion

    //region Fields
    private String	eType;
    //endregion
}
