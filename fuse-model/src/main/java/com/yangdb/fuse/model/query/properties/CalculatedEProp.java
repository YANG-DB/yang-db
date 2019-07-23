package com.yangdb.fuse.model.query.properties;

/*-
 * #%L
 * SchematicEProp.java - fuse-model - yangdb - 2,016
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
import com.yangdb.fuse.model.query.properties.projection.CalculatedFieldProjection;

/**
 *
 * a calculated field based on tag associated with the entity or entity relation
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CalculatedEProp extends EProp {
    //region Constructors
    public CalculatedEProp() {

    }

    public CalculatedEProp(int eNum, String expression, CalculatedFieldProjection con) {
        super(eNum, expression, con);
    }
    //endregion

    //region Override Methods
    //endregion

    @Override
    public CalculatedEProp clone() {
        return clone(geteNum());
    }

    @Override
    public CalculatedEProp clone(int eNum) {
        CalculatedEProp clone = new CalculatedEProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        return clone;
    }

    public static CalculatedEProp of(int eNum, String expression, CalculatedFieldProjection con) {
        return new CalculatedEProp(eNum, expression, con);
    }

    @Override
    public CalculatedFieldProjection getProj() {
        return (CalculatedFieldProjection) super.getProj();
    }
}
