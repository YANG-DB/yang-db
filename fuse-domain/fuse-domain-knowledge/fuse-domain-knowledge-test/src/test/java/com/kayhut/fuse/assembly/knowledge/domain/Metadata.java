package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Property;

import java.text.SimpleDateFormat;
import java.util.*;

//todo - for kobi usage
public abstract class Metadata extends KnowledgeDomainBuilder {
    static SimpleDateFormat sdf;

    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public String lastUpdateUser = "test";
    public String creationUser = "test";
    //public String creationTime = sdf.format(new Date(System.currentTimeMillis()));
    //public String lastUpdateTime = sdf.format(new Date(System.currentTimeMillis()));
    public Date creationTime = new Date(System.currentTimeMillis());
    public Date lastUpdateTime = new Date(System.currentTimeMillis());
    public Date deleteTime = new Date(System.currentTimeMillis());
    public String[] authorization = new String[]{"procedure.1", "procedure.2"};

    public <T extends Metadata> T lastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
        return (T)this;
    }

    public <T extends Metadata> T creationUser(String creationUser) {
        this.creationUser = creationUser;
        return (T)this;
    }

    public <T extends Metadata> T creationTime(Date creationTime) {
        this.creationTime = creationTime;
        return (T)this;
    }

    public <T extends Metadata> T lastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return (T)this;
    }

    public <T extends Metadata> T deleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
        return (T)this;
    }

//        public Metadata creationTime(String creationTime) {
//        this.creationTime = creationTime;
//        return this;
//    }

    // Compare from what fetched from ES
    public List<Property> collect(List<Property> properties) {
        ArrayList<Property> list = new ArrayList<>(properties);
        list.addAll(Arrays.asList(
                new Property("lastUpdateUser", "raw", lastUpdateUser),
                new Property("creationUser", "raw", creationUser),
                new Property("lastUpdateTime", "raw", sdf.format(lastUpdateTime)),
                new Property("creationTime", "raw", sdf.format(creationTime)),
                new Property("deleteTime", "raw", sdf.format(deleteTime)),
                new Property("authorization", "raw", Arrays.asList(authorization))));
        return list;
    }

    // Write to ES
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ArrayNode authNode = mapper.createArrayNode();
        for (String auth : authorization) {
            authNode.add(auth);
        }

        node.put("authorizationCount", authNode.size());
        node.put("authorization", authNode); // Authorization = Clearance
        node.put("lastUpdateUser", lastUpdateUser);
        node.put("lastUpdateTime", sdf.format(lastUpdateTime));
        node.put("creationUser", creationUser);
        node.put("deleteTime", sdf.format(deleteTime));
        node.put("creationTime", sdf.format(creationTime));
        return node;
    }
}
