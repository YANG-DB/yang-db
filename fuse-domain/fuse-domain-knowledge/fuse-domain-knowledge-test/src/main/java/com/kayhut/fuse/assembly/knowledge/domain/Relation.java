package com.kayhut.fuse.assembly.knowledge.domain;

/*-
 * #%L
 * fuse-domain-knowledge-test
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

/**
 * Created by lior.perry pc on 5/11/2018.
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
        "entityALogicalId",
        "entityAId",
        "entityACategory",
        "entityBLogicalId",
        "entityBId",
        "entityBCategory",
        "context",
        "category",
        "lastUpdateUser",
        "creationUser",
        "lastUpdateTime",
        "creationTime",
        "authorizationCount",
        "authorization",
        "refs"
})
public class Relation {

    @JsonProperty("type")
    private String type;
    @JsonProperty("entityALogicalId")
    private String entityALogicalId;
    @JsonProperty("entityAId")
    private String entityAId;
    @JsonProperty("entityACategory")
    private String entityACategory;
    @JsonProperty("entityBLogicalId")
    private String entityBLogicalId;
    @JsonProperty("entityBId")
    private String entityBId;
    @JsonProperty("entityBCategory")
    private String entityBCategory;
    @JsonProperty("context")
    private String context;
    @JsonProperty("category")
    private String category;
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
    @JsonProperty("refs")
    private List<String> refs = null;
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

    public Relation withType(String type) {
        this.type = type;
        return this;
    }

    @JsonProperty("entityALogicalId")
    public String getEntityALogicalId() {
        return entityALogicalId;
    }

    @JsonProperty("entityALogicalId")
    public void setEntityALogicalId(String entityALogicalId) {
        this.entityALogicalId = entityALogicalId;
    }

    public Relation withEntityALogicalId(String entityALogicalId) {
        this.entityALogicalId = entityALogicalId;
        return this;
    }

    @JsonProperty("entityAId")
    public String getEntityAId() {
        return entityAId;
    }

    @JsonProperty("entityAId")
    public void setEntityAId(String entityAId) {
        this.entityAId = entityAId;
    }

    public Relation withEntityAId(String entityAId) {
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

    public Relation withEntityACategory(String entityACategory) {
        this.entityACategory = entityACategory;
        return this;
    }

    @JsonProperty("entityBLogicalId")
    public String getEntityBLogicalId() {
        return entityBLogicalId;
    }

    @JsonProperty("entityBLogicalId")
    public void setEntityBLogicalId(String entityBLogicalId) {
        this.entityBLogicalId = entityBLogicalId;
    }

    public Relation withEntityBLogicalId(String entityBLogicalId) {
        this.entityBLogicalId = entityBLogicalId;
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

    public Relation withEntityBId(String entityBId) {
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

    public Relation withEntityBCategory(String entityBCategory) {
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

    public Relation withContext(String context) {
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

    public Relation withCategory(String category) {
        this.category = category;
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

    public Relation withLastUpdateUser(String lastUpdateUser) {
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

    public Relation withCreationUser(String creationUser) {
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

    public Relation withLastUpdateTime(Date lastUpdateTime) {
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

    public Relation withCreationTime(Date creationTime) {
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

    public Relation withAuthorizationCount(Integer authorizationCount) {
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

    public Relation withAuthorization(List<String> authorization) {
        this.authorization = authorization;
        return this;
    }

    @JsonProperty("refs")
    public List<String> getRefs() {
        return refs;
    }

    @JsonProperty("refs")
    public void setRefs(List<String> refs) {
        this.refs = refs;
    }

    public Relation withRefs(List<String> refs) {
        this.refs = refs;
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

    public Relation withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
