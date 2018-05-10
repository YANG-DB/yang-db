package com.kayhut.fuse.assembly.knowlegde;

import com.fasterxml.jackson.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "authorization",
        "system",
        "creationUser",
        "creationTime",
        "authorizationCount",
        "lastUpdateUser",
        "type",
        "title",
        "value",
        "url",
        "lastUpdateTime"
})
public class KnowledgeReference {

    @JsonProperty("authorization")
    private List<String> authorization = null;
    @JsonProperty("system")
    private String system;
    @JsonProperty("creationUser")
    private String creationUser;
    @JsonProperty("creationTime")
    private String creationTime;
    @JsonProperty("authorizationCount")
    private Integer authorizationCount;
    @JsonProperty("lastUpdateUser")
    private String lastUpdateUser;
    @JsonProperty("type")
    private String type;
    @JsonProperty("title")
    private String title;
    @JsonProperty("value")
    private String value;
    @JsonProperty("url")
    private String url;
    @JsonProperty("lastUpdateTime")
    private String lastUpdateTime;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("authorization")
    public List<String> getAuthorization() {
        return authorization;
    }

    @JsonProperty("authorization")
    public void setAuthorization(List<String> authorization) {
        this.authorization = authorization;
    }

    @JsonProperty("system")
    public String getSystem() {
        return system;
    }

    @JsonProperty("system")
    public void setSystem(String system) {
        this.system = system;
    }

    @JsonProperty("creationUser")
    public String getCreationUser() {
        return creationUser;
    }

    @JsonProperty("creationUser")
    public void setCreationUser(String creationUser) {
        this.creationUser = creationUser;
    }

    @JsonProperty("creationTime")
    public String getCreationTime() {
        return creationTime;
    }

    @JsonProperty("creationTime")
    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    @JsonProperty("authorizationCount")
    public Integer getAuthorizationCount() {
        return authorizationCount;
    }

    @JsonProperty("authorizationCount")
    public void setAuthorizationCount(Integer authorizationCount) {
        this.authorizationCount = authorizationCount;
    }

    @JsonProperty("lastUpdateUser")
    public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    @JsonProperty("lastUpdateUser")
    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("lastUpdateTime")
    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    @JsonProperty("lastUpdateTime")
    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

//import java.sql.Timestamp;
//import java.util.List;

/**
 * Created by rani on 5/10/2018.
 */
/*public class KnowledgeReference {
    private String type;
    private String title;
    private String url;
    private String value;
    private String system;
    private int authorizationCount;
    private Timestamp lastUpdateTime;
    private Timestamp creationTime;
    private String creationUser;
    private String lastUpdateUser;
    private List<String> authorization = null;

    public List<String> getAuthorization() {
        return authorization;
    }

    public void addAuthorization(String authorizationValue) {
        if (authorization == null) {
            authorization = new List<String>();
        }
        authorization.add(authorizationValue);
    }

    public int getAuthorizationCount() {
        return 1;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String iSystem) {
        value = iSystem;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String iSystem) {
        value = iSystem;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String iSystem) {
        value = iSystem;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String iValue) {
        value = iValue;
    }

    public String getType() {
        return "reference";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String iTitle) {
        title = iTitle;
    }

    public void setUrl(String iUrl) {
        url = iUrl;
    }

    public String getUrl() {
        return url;
    }


    .put("type", "reference")
                            .put("title", "Title of - " + referenceId)
                            .put("url", "http://" + UUID.randomUUID().toString() + "." + domains.get(random.nextInt(domains.size())))
            .put("value", contents.get(random.nextInt(contents.size())))
            .put("system", "system" + random.nextInt(10))
            .put("authorization", Arrays.asList("source1.procedure1", "source2.procedure2"))
            .put("authorizationCount", 1)
                            .put("lastUpdateUser", users.get(random.nextInt(users.size())))
            .put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())))
            .put("creationUser", users.get(random.nextInt(users.size())))
            .put("creationTime", sdf.format(new Date(System.currentTimeMillis()))).get()));
}*/
