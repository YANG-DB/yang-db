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

import java.util.Date;

public class Reference extends KnowledgeEntityBase {
    private static final String entityType = "reference";

    //region Constructors
    public Reference() {
        super(entityType);
    }

    public Reference(String title, String content, String url, String system) {
        this(title, content, url, system, null);
    }

    public Reference(String title, String content, String url, String system, KnowledgeEntityBase.Metadata metadata) {
        super(entityType, metadata);
        this.title = title;
        this.content = content;
        this.url = url;
        this.system = system;
    }
    //endregion

    //region Properties
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }
    //endregion

    //region Fields
    private String title;
    private String content;
    private String url;
    private String system;
    //endregion
}
