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

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class EvalueDate  extends EvalueBase {
    //region Constructors
    public EvalueDate() {
        super();
    }

    public EvalueDate(String logicalId, String context, String entityId, String fieldId, Date dateValue) {
        this(logicalId, context, entityId, fieldId, dateValue, null);
    }

    public EvalueDate(String logicalId, String context, String entityId, String fieldId, Date dateValue, KnowledgeEntityBase.Metadata metadata) {
        super(logicalId, context, entityId, fieldId, metadata);
        this.dateValue = dateValue;
    }
    //endregion

    //region Properties
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public Date getStringValue() {
        return dateValue;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public void setStringValue(Date dateValue) {
        this.dateValue = dateValue;
    }
    //endregion

    //region Fields
    private Date dateValue;
    //endregion
}
