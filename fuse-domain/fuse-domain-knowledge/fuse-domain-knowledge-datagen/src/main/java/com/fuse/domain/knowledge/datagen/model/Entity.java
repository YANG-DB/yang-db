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
public class Entity extends KnowledgeEntityBase {
    private static final String entityType = "entity";

    //region Constructors
    public Entity() {
        super(entityType);
    }

    public Entity(String logicalId, String context, String category) {
        this(logicalId, context, category, null);
    }

    public Entity(String logicalId, String context, String category, KnowledgeEntityBase.Metadata metadata) {
        super(entityType, metadata);
        this.logicalId = logicalId;
        this.context = context;
        this.category = category;
    }
    //endregion

    //region Properties
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLogicalId() {
        return logicalId;
    }

    public void setLogicalId(String logicalId) {
        this.logicalId = logicalId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
    //endregion

    //region Fields
    private String category;
    private String logicalId;
    private String context;
    //endregion
}
