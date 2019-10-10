package com.fuse.domain.knowledge.datagen;

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

public class LightSchema {
    //region Constructors
    public LightSchema() {}

    public LightSchema(
            String entityIndex,
            String relationIndex,
            String insightIndex,
            String referenceIndex,
            String idFormat) {
        this.entityIndex = entityIndex;
        this.relationIndex = relationIndex;
        this.insightIndex = insightIndex;
        this.referenceIndex = referenceIndex;
        this.idFormat = idFormat;
    }
    //endregion

    //region Properties
    public String getEntityIndex() {
        return entityIndex;
    }

    public void setEntityIndex(String entityIndex) {
        this.entityIndex = entityIndex;
    }

    public String getRelationIndex() {
        return relationIndex;
    }

    public void setRelationIndex(String relationIndex) {
        this.relationIndex = relationIndex;
    }

    public String getInsightIndex() {
        return insightIndex;
    }

    public void setInsightIndex(String insightIndex) {
        this.insightIndex = insightIndex;
    }

    public String getReferenceIndex() {
        return referenceIndex;
    }

    public void setReferenceIndex(String referenceIndex) {
        this.referenceIndex = referenceIndex;
    }

    public String getIdFormat() {
        return idFormat;
    }

    public void setIdFormat(String idFormat) {
        this.idFormat = idFormat;
    }
    //endregion

    //region Fields
    private String entityIndex;
    private String relationIndex;
    private String insightIndex;
    private String referenceIndex;
    private String idFormat;
    //endregion
}
