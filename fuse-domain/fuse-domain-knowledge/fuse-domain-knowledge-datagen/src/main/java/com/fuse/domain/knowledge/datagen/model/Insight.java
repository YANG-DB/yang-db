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
public class Insight extends KnowledgeEntityBase {
    private static final String entityType = "insight";

    //region Constructors
    public Insight() {
        super(entityType);
    }

    public Insight(String context, String content, Iterable<String> entityIds) {
        this(context, content, entityIds, null);
    }

    public Insight(String context, String content, Iterable<String> entityIds, KnowledgeEntityBase.Metadata metadata) {
        super(entityType, metadata);
        this.context = context;
        this.content = content;
        this.entityIds = entityIds;
    }
    //endregion

    //region Properties
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Iterable<String> getEntityIds() {
        return entityIds;
    }

    public void setEntityIds(Iterable<String> entityIds) {
        this.entityIds = entityIds;
    }
    //endregion

    //region Fields
    private String context;
    private String content;
    private Iterable<String> entityIds;
    //endregion
}
