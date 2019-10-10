package com.yangdb.fuse.model.query.properties;

/*-
 *
 * EProp.java - fuse-model - yangdb - 2,016
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
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.projection.Projection;

/**
 * Created by benishue on 17/02/2017.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EProp extends BaseProp {
    //region Constructors
    public EProp() {
        super();
    }

    public EProp(int eNum, String pType, Constraint con) {
        super(eNum, pType, con);
    }

    public EProp(int eNum, String pType, Projection proj) {
        super(eNum, pType, proj);
    }
    //endregion


    @Override
    public EProp clone() {
        return clone(geteNum());
    }

    @Override
    public EProp clone(int eNum) {
        EProp clone = new EProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        return clone;
    }


    //region Static
    public static EProp of(int eNum, String pType, Constraint con) {
        return new EProp(eNum, pType, con);
    }

    public static EProp of(int eNum, String pType, Projection proj) {
        return new EProp(eNum, pType, proj);
    }
    //endregion
}
