package com.yangdb.fuse.model.query.properties;

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
 * SchematicRelProp.java - fuse-model - yangdb - 2,016
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

import com.yangdb.fuse.model.query.properties.constraint.Constraint;

/**
 *
 * Translates rType to a schematic name such as "stringValue.keyword"
 */
public class SchematicRelProp extends RelProp {
    //region Constructors
    public SchematicRelProp() {

    }

    public SchematicRelProp(int eNum, String pType, String schematicName, Constraint con) {
        this(eNum, pType, schematicName, con, 0);
    }

    public SchematicRelProp(int eNum, String pType, String schematicName, Constraint con, int b) {
        super(eNum, pType, con, b);
        this.schematicName = schematicName;
    }
    //endregion

    @Override
    public SchematicRelProp clone() {
        return clone(geteNum());
    }

    @Override
    public SchematicRelProp clone(int eNum) {
        final SchematicRelProp clone = new SchematicRelProp();
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
    public String getSchematicName() {
        return this.schematicName;
    }

    public void setSchematicName(String schematicName) {
        this.schematicName = schematicName;
    }
    //endregion

    //region Fields
    private String schematicName;
    //endregion
}
