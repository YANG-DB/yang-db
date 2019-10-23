package com.fuse.domain.knowledge.datagen.model;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
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

public class RvalueFloat extends RvalueBase {
    //region Constructors
    public RvalueFloat() {
        super();
    }

    public RvalueFloat(String relId, String context, String fieldId, float floatValue) {
        this(relId, context, fieldId, floatValue, null);
    }

    public RvalueFloat(String relId, String context, String fieldId, float floatValue, Metadata metadata) {
        super(relId, context, fieldId, metadata);
        this.floatValue = floatValue;
    }
    //endregion

    //region Properties
    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }
    //endregion

    //region Fields
    private float floatValue;
    //endregion
}
