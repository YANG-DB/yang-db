package com.yangdb.fuse.assembly.knowledge.domain;

/*-
 *
 * fuse-domain-knowledge-test
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

/**
 * Created by lior.perry pc on 5/12/2018.
 */
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "entityAId",
        "entityACategory",
        "techId",
        "entityATechId",
        "entityBTechId",
        "entityBId",
        "entityBCategory",
        "context",
        "category",
        "relationId",
        "direction",
        "lastUpdateUser",
        "creationUser",
        "lastUpdateTime",
        "creationTime",
        "authorizationCount",
        "authorization"
})
public class RelationEntity {

    @JsonProperty("type")
    private String type;
    @JsonProperty("entityAId")
    private String entityAId;
    @JsonProperty("entityATechId")
    private String entityATechId;
    @JsonProperty("entityACategory")
    private String entityACategory;
    @JsonProperty("entityBId")
    private String entityBId;
    @JsonProperty("entityBTechId")
    private String entityBTechId;
    @JsonProperty("entityBCategory")
    private String entityBCategory;
    @JsonProperty("context")
    private String context;
    @JsonProperty("techId")
    private String techId;
    @JsonProperty("category")
    private String category;
    @JsonProperty("relationId")
    private String relationId;
    @JsonProperty("direction")
    private String direction;
    @JsonProperty("lastUpdateUser")
    private String lastUpdateUser;
    @JsonProperty("creationUser")
    private String creationUser;
    @JsonProperty("lastUpdateTime")
    private Date lastUpdateTime;
    @JsonProperty("creationTime")
    private Date creationTime;
    @JsonProperty("authorizationCount")
    private Integer authorizationCount;
    @JsonProperty("authorization")
    private List<String> authorization = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    public RelationEntity withType(String type) {
        this.type = type;
        return this;
    }

    @JsonProperty("techId")
    public String getTechId() {
        return techId;
    }

    @JsonProperty("techId")
    public void setTechId(String techId) {
        this.techId = techId;
    }

    @JsonProperty("entityATechId")
    public String getEntityATechId() {
        return entityATechId;
    }

    @JsonProperty("entityATechId")
    public void setEntityATechId(String entityATechId) {
        this.entityATechId = entityATechId;
    }

    @JsonProperty("entityBTechId")
    public String getEntityBTechId() {
        return entityBTechId;
    }

    @JsonProperty("entityBTechId")
    public void setEntityBTechId(String entityBTechId) {
        this.entityBTechId = entityBTechId;
    }


    @JsonProperty("entityAId")
    public String getEntityAId() {
        return entityAId;
    }

    @JsonProperty("entityAId")
    public void setEntityAId(String entityAId) {
        this.entityAId = entityAId;
    }

    public RelationEntity withEntityAId(String entityAId) {
        this.entityAId = entityAId;
        return this;
    }

    @JsonProperty("entityACategory")
    public String getEntityACategory() {
        return entityACategory;
    }

    @JsonProperty("entityACategory")
    public void setEntityACategory(String entityACategory) {
        this.entityACategory = entityACategory;
    }

    public RelationEntity withEntityACategory(String entityACategory) {
        this.entityACategory = entityACategory;
        return this;
    }

    @JsonProperty("entityBId")
    public String getEntityBId() {
        return entityBId;
    }

    @JsonProperty("entityBId")
    public void setEntityBId(String entityBId) {
        this.entityBId = entityBId;
    }

    public RelationEntity withEntityBId(String entityBId) {
        this.entityBId = entityBId;
        return this;
    }

    @JsonProperty("entityBCategory")
    public String getEntityBCategory() {
        return entityBCategory;
    }

    @JsonProperty("entityBCategory")
    public void setEntityBCategory(String entityBCategory) {
        this.entityBCategory = entityBCategory;
    }

    public RelationEntity withEntityBCategory(String entityBCategory) {
        this.entityBCategory = entityBCategory;
        return this;
    }

    @JsonProperty("context")
    public String getContext() {
        return context;
    }

    @JsonProperty("context")
    public void setContext(String context) {
        this.context = context;
    }

    public RelationEntity withContext(String context) {
        this.context = context;
        return this;
    }

    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    @JsonProperty("category")
    public void setCategory(String category) {
        this.category = category;
    }

    public RelationEntity withCategory(String category) {
        this.category = category;
        return this;
    }

    @JsonProperty("relationId")
    public String getRelationId() {
        return relationId;
    }

    @JsonProperty("relationId")
    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    public RelationEntity withRelationId(String relationId) {
        this.relationId = relationId;
        return this;
    }

    @JsonProperty("direction")
    public String getDirection() {
        return direction;
    }

    @JsonProperty("direction")
    public void setDirection(String direction) {
        this.direction = direction;
    }

    public RelationEntity withDirection(String direction) {
        this.direction = direction;
        return this;
    }

    @JsonProperty("lastUpdateUser")
    public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    @JsonProperty("lastUpdateUser")
    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }

    public RelationEntity withLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
        return this;
    }

    @JsonProperty("creationUser")
    public String getCreationUser() {
        return creationUser;
    }

    @JsonProperty("creationUser")
    public void setCreationUser(String creationUser) {
        this.creationUser = creationUser;
    }

    public RelationEntity withCreationUser(String creationUser) {
        this.creationUser = creationUser;
        return this;
    }

    @JsonProperty("lastUpdateTime")
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    @JsonProperty("lastUpdateTime")
    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public RelationEntity withLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    @JsonProperty("creationTime")
    public Date getCreationTime() {
        return creationTime;
    }

    @JsonProperty("creationTime")
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public RelationEntity withCreationTime(Date creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    @JsonProperty("authorizationCount")
    public Integer getAuthorizationCount() {
        return authorizationCount;
    }

    @JsonProperty("authorizationCount")
    public void setAuthorizationCount(Integer authorizationCount) {
        this.authorizationCount = authorizationCount;
    }

    public RelationEntity withAuthorizationCount(Integer authorizationCount) {
        this.authorizationCount = authorizationCount;
        return this;
    }

    @JsonProperty("authorization")
    public List<String> getAuthorization() {
        return authorization;
    }

    @JsonProperty("authorization")
    public void setAuthorization(List<String> authorization) {
        this.authorization = authorization;
    }

    public RelationEntity withAuthorization(List<String> authorization) {
        this.authorization = authorization;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public RelationEntity withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
