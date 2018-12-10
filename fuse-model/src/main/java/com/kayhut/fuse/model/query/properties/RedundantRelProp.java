package com.kayhut.fuse.model.query.properties;

/*-
 * #%L
 * RedundantRelProp.java - fuse-model - kayhut - 2,016
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

import com.kayhut.fuse.model.query.properties.constraint.Constraint;

/**
 * Created by moti on 5/9/2017.
 */
public class RedundantRelProp extends SchematicRelProp {
    //region Constructors
    public RedundantRelProp() {

    }

    public RedundantRelProp(String redundantPropName) {
        this.redundantPropName = redundantPropName;
        this.setSchematicName(redundantPropName);
    }

    public RedundantRelProp(int eNum, String pType, String redundantPropName, String schematicName, Constraint con) {
        super(eNum, pType, schematicName, con);
        this.redundantPropName = redundantPropName;
        if (this.getSchematicName() == null) {
            this.setSchematicName(redundantPropName);
        }
    }
    //endregion
    @Override
    public RedundantRelProp clone() {
        return clone(geteNum());
    }

    @Override
    public RedundantRelProp clone(int eNum) {
        final RedundantRelProp clone = new RedundantRelProp();
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
    public String getRedundantPropName() {
        return redundantPropName;
    }

    public void setRedundantPropName(String redundantPropName) {
        this.redundantPropName = redundantPropName;
        if (this.getSchematicName() == null) {
            this.setSchematicName(redundantPropName);
        }
    }
    //endregion

    //region Fields
    private String redundantPropName;
    //endregion

    public static RedundantRelProp of(int eNum, String redundantPropName, String pType, Constraint constraint){
        return new RedundantRelProp(eNum, pType, redundantPropName, redundantPropName, constraint);
    }

    public static RedundantRelProp of(int eNum, String redundantPropName, String schematicName, String pType, Constraint constraint){
        return new RedundantRelProp(eNum, pType, redundantPropName, schematicName, constraint);
    }
}
