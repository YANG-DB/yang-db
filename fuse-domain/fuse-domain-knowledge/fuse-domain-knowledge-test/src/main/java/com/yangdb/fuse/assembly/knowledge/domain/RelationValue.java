package com.yangdb.fuse.assembly.knowledge.domain;

/*-
 * #%L
 * fuse-domain-knowledge-test
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
import org.geojson.Point;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "relationId",
        "context",
        "fieldId",
        "bdt",
        "stringValue",
        "geoValue",
        "intValue",
        "dateValue",
        "lastUpdateUser",
        "creationUser",
        "lastUpdateTime",
        "creationTime",
        "authorizationCount",
        "authorization",
        "refs"
})
public class RelationValue {

    @JsonProperty("type")
    private String type;
    @JsonProperty("relationId")
    private String relationId;
    @JsonProperty("context")
    private String context;
    @JsonProperty("fieldId")
    private String fieldId;
    @JsonProperty("bdt")
    private String bdt;
    @JsonProperty("stringValue")
    private String stringValue;
    @JsonProperty("intValue")
    private Integer intValue;
    @JsonProperty("longValue")
    private Integer longValue;
    @JsonProperty("floatValue")
    private Integer floatValue;
    @JsonProperty("dateValue")
    private Date dateValue;
    @JsonProperty("geoValue")
    private Point geoValue;
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

    public RelationValue withType(String type) {
        this.type = type;
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

    public RelationValue withRelationId(String relationId) {
        this.relationId = relationId;
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

    public RelationValue withContext(String context) {
        this.context = context;
        return this;
    }

    @JsonProperty("fieldId")
    public String getFieldId() {
        return fieldId;
    }

    @JsonProperty("fieldId")
    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public RelationValue withFieldId(String fieldId) {
        this.fieldId = fieldId;
        return this;
    }

    @JsonProperty("bdt")
    public String getBdt() {
        return bdt;
    }

    @JsonProperty("bdt")
    public void setBdt(String bdt) {
        this.bdt = bdt;
    }

    public RelationValue withBdt(String bdt) {
        this.bdt = bdt;
        return this;
    }

    @JsonProperty("stringValue")
    public String getStringValue() {
        return stringValue;
    }

    @JsonProperty("stringValue")
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    @JsonProperty("geoValue")
    public Point getGeoValue() {
        return geoValue;
    }

    @JsonProperty("geoValue")
    public void setGeoValue(Point geoValue) {
        this.geoValue = geoValue;
    }

    public RelationValue withStringValue(String stringValue) {
        this.stringValue = stringValue;
        return this;
    }

    @JsonProperty("longValue")
    public Integer getLongValue() {
        return longValue;
    }

    @JsonProperty("longValue")
    public void setLongValue(Integer longValue) {
        this.longValue = longValue;
    }

    @JsonProperty("floatValue")
    public Integer getFloatValue() {
        return floatValue;
    }

    @JsonProperty("floatValue")
    public void setFloatValue(Integer floatValue) {
        this.floatValue = floatValue;
    }

    @JsonProperty("intValue")
    public Integer getIntValue() {
        return intValue;
    }

    @JsonProperty("intValue")
    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public RelationValue withIntValue(Integer intValue) {
        this.intValue = intValue;
        return this;
    }

    @JsonProperty("dateValue")
    public Date getDateValue() {
        return dateValue;
    }

    @JsonProperty("dateValue")
    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public RelationValue withDateValue(Date dateValue) {
        this.dateValue = dateValue;
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

    public RelationValue withLastUpdateUser(String lastUpdateUser) {
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

    public RelationValue withCreationUser(String creationUser) {
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

    public RelationValue withLastUpdateTime(Date lastUpdateTime) {
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

    public RelationValue withCreationTime(Date creationTime) {
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

    public RelationValue withAuthorizationCount(Integer authorizationCount) {
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

    public RelationValue withAuthorization(List<String> authorization) {
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

    public RelationValue withRefs(List<String> refs) {
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

    public RelationValue withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
