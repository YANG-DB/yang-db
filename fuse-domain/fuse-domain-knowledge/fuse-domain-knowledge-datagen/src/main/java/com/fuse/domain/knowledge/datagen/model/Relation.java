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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Relation extends KnowledgeEntityBase {
    private static final String entityType = "relation";

    //region Constructors
    public Relation() {
        super(entityType);
    }

    public Relation(String context, String category, String entityAId, String entityACategory, String entityBId, String entityBCategory) {
        this(context, category, entityAId, entityACategory, entityBId, entityBCategory, null);
    }

    public Relation(
            String context,
            String category,
            String entityAId,
            String entityACategory,
            String entityBId,
            String entityBCategory,
            KnowledgeEntityBase.Metadata metadata) {
        super(entityType, metadata);
        this.context = context;
        this.category = category;
        this.entityAId = entityAId;
        this.entityACategory = entityACategory;
        this.entityBId = entityBId;
        this.entityBCategory = entityBCategory;
    }
    //endregion

    //region Properties
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEntityAId() {
        return entityAId;
    }

    public void setEntityAId(String entityAId) {
        this.entityAId = entityAId;
    }

    public String getEntityACategory() {
        return entityACategory;
    }

    public void setEntityACategory(String entityACategory) {
        this.entityACategory = entityACategory;
    }

    public String getEntityBId() {
        return entityBId;
    }

    public void setEntityBId(String entityBId) {
        this.entityBId = entityBId;
    }

    public String getEntityBCategory() {
        return entityBCategory;
    }

    public void setEntityBCategory(String entityBCategory) {
        this.entityBCategory = entityBCategory;
    }
    //endregion

    //region Fields
    private String context;
    private String category;
    private String entityAId;
    private String entityACategory;
    private String entityBId;
    private String entityBCategory;
    //endregion
}
