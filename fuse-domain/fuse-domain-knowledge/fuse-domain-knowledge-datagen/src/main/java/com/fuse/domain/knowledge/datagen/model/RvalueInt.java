package com.fuse.domain.knowledge.datagen.model;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
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

import java.util.Date;

public class RvalueInt extends RvalueBase {
    //region Constructors
    public RvalueInt() {
        super();
    }

    public RvalueInt(String relationId, String context, String fieldId, int intValue) {
        this(relationId, context, fieldId, intValue, null);
    }

    public RvalueInt(String relationId, String context, String fieldId, int intValue, KnowledgeEntityBase.Metadata metadata) {
        super(relationId, context, fieldId, metadata);
        this.intValue = intValue;
    }
    //endregion

    //region Properties
    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }
    //endregion

    //region Fields
    private int intValue;
    //endregion
}
