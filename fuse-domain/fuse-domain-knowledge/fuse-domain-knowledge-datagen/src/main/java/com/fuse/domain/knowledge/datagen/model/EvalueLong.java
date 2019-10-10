package com.fuse.domain.knowledge.datagen.model;

/*-
 *
 * fuse-domain-knowledge-datagen
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

public class EvalueLong extends EvalueBase {
    //region Constructors
    public EvalueLong() {
        super();
    }

    public EvalueLong(String logicalId, String context, String entityId, String fieldId, long longValue) {
        this(logicalId, context, entityId, fieldId, longValue, null);
    }

    public EvalueLong(String logicalId, String context, String entityId, String fieldId, long longValue, Metadata metadata) {
        super(logicalId, context, entityId, fieldId, metadata);
        this.longValue = longValue;
    }
    //endregion

    //region Properties
    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(int longValue) {
        this.longValue = longValue;
    }
    //endregion

    //region Fields
    private long longValue;
    //endregion
}
