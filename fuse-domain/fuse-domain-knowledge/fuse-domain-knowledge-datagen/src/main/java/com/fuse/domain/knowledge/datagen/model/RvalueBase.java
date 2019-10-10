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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class RvalueBase extends KnowledgeEntityBase {
    private static final String entityType = "r.value";

    //region Constructors
    public RvalueBase() {
        super(entityType);
    }

    public RvalueBase(String relationId, String context, String fieldId) {
        this(relationId, context, fieldId, null);
    }

    public RvalueBase(String relationId, String context, String fieldId, KnowledgeEntityBase.Metadata metadata) {
        super(entityType, metadata);
        this.relationId = relationId;
        this.context = context;
        this.fieldId = fieldId;
    }
    //endregion

    //region Properties
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getRelationId() {
        return relationId;
    }

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }
    //endregion

    //region Fields
    private String context;
    private String relationId;
    private String fieldId;
    //endregion
}
