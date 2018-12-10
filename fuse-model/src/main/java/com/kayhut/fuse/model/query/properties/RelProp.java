package com.kayhut.fuse.model.query.properties;

/*-
 * #%L
 * RelProp.java - fuse-model - kayhut - 2,016
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
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.projection.Projection;

/**
 * Created by benishue on 17/02/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RelProp extends BaseProp {
    //region Constructors
    public RelProp() {
        super();
    }

    public RelProp(int eNum, String pType, Constraint con, int b) {
        super(eNum, pType, con);
        this.b = b;
    }

    public RelProp(int eNum, String pType, Projection proj, int b) {
        super(eNum, pType, proj);
        this.b = b;
    }
    //endregion

    @Override
    public RelProp clone() {
        return clone(geteNum());
    }

    @Override
    public RelProp clone(int eNum) {
        final RelProp clone = new RelProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setB(getB());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        return clone;
    }

    //region Properties
    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
    //endregion

    //region Fields
    private int b;
    //endregion

    //region Static
    public static RelProp of(int eNum, String pType, Constraint con) {
        return new RelProp(eNum, pType, con, 0);
    }

    public static RelProp of(int eNum, String pType, Projection proj) {
        return new RelProp(eNum, pType, proj, 0);
    }
    //endregion
}
