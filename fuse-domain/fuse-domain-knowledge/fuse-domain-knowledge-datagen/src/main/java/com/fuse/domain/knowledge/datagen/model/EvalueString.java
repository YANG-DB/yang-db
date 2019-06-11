package com.fuse.domain.knowledge.datagen.model;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
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

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class EvalueString extends EvalueBase {
    //region Constructors
    public EvalueString() {
        super();
    }

    public EvalueString(String logicalId, String context, String entityId, String fieldId, String stringValue) {
        this(logicalId, context, entityId, fieldId, stringValue, null);
    }

    public EvalueString(String logicalId, String context, String entityId, String fieldId, String stringValue, KnowledgeEntityBase.Metadata metadata) {
        super(logicalId, context, entityId, fieldId, metadata);
        this.stringValue = stringValue;
    }
    //endregion

    //region Properties
    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
    //endregion

    //region Fields
    private String stringValue;
    //endregion
}
