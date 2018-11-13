package com.kayhut.fuse.model.query.properties;

/*-
 * #%L
 * SchematicEProp.java - fuse-model - kayhut - 2,016
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

/**
 * Created by roman.margolis on 07/02/2018.
 *
 * Translates pType to a schematic name such as "stringValue.keyword"
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SchematicEProp extends EProp {
    //region Constructors
    public SchematicEProp() {

    }

    public SchematicEProp(int eNum, String pType, String schematicName, Constraint con) {
        super(eNum, pType, con);
        this.schematicName = schematicName;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }

        if (!(o instanceof SchematicEProp)) {
            return false;
        }

        SchematicEProp other = (SchematicEProp)o;
        if (!this.schematicName.equals(other.schematicName)) {
            return false;
        }

        return true;
    }
    //endregion

    //region Properties
    public String getSchematicName() {
        return schematicName;
    }

    public void setSchematicName(String schematicName) {
        this.schematicName = schematicName;
    }
    //enregion

    //region Fields
    private String schematicName;
    //endregion
}
