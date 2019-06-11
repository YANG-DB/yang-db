package com.kayhut.fuse.assembly.knowledge.domain;

/*-
 * #%L
 * fuse-domain-knowledge-test
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
        "content",
        "context",
        "lastUpdateUser",
        "creationUser",
        "lastUpdateTime",
        "creationTime",
        "authorizationCount",
        "authorization",
        "refs",
        "entityIds"
})
public class Insight {

    @JsonProperty("type")
    private String type;
    @JsonProperty("content")
    private String content;
    @JsonProperty("context")
    private String context;
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
    @JsonProperty("entityIds")
    private List<String> entityIds = null;
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

    public Insight withType(String type) {
        this.type = type;
        return this;
    }

    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
    }

    public Insight withContent(String content) {
        this.content = content;
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

    public Insight withContext(String context) {
        this.context = context;
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

    public Insight withLastUpdateUser(String lastUpdateUser) {
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

    public Insight withCreationUser(String creationUser) {
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

    public Insight withLastUpdateTime(Date lastUpdateTime) {
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

    public Insight withCreationTime(Date creationTime) {
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

    public Insight withAuthorizationCount(Integer authorizationCount) {
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

    public Insight withAuthorization(List<String> authorization) {
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

    public Insight withRefs(List<String> refs) {
        this.refs = refs;
        return this;
    }

    @JsonProperty("entityIds")
    public List<String> getEntityIds() {
        return entityIds;
    }

    @JsonProperty("entityIds")
    public void setEntityIds(List<String> entityIds) {
        this.entityIds = entityIds;
    }

    public Insight withEntityIds(List<String> entityIds) {
        this.entityIds = entityIds;
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

    public Insight withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
