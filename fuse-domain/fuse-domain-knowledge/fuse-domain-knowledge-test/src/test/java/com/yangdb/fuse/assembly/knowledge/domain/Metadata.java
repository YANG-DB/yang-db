package com.yangdb.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.results.Property;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Metadata extends KnowledgeDomainBuilder {
    static SimpleDateFormat sdf;

    static {
        sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public Metadata() {
    }

    public Metadata(Metadata metadata) {
        this.lastUpdateTime = metadata.lastUpdateTime;
        this.creationTime = metadata.creationTime;
        this.creationUser = metadata.creationUser;
        this.lastUpdateUser = metadata.lastUpdateUser;
        this.authorization = metadata.authorization;
        this.deleteTime = metadata.deleteTime;
    }

    public String lastUpdateUser = "test";
    public String creationUser = "test";
    //public String creationTime = sdf.format(new Date(System.currentTimeMillis()));
    //public String lastUpdateTime = sdf.format(new Date(System.currentTimeMillis()));
    public Date creationTime = new Date(System.currentTimeMillis());
    public Date lastUpdateTime = new Date(System.currentTimeMillis());
    public Date deleteTime;
    public String[] authorization = new String[]{"procedure.1", "procedure.2"};

    public <T extends Metadata> T lastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
        return (T) this;
    }

    public <T extends Metadata> T creationUser(String creationUser) {
        this.creationUser = creationUser;
        return (T) this;
    }

    public <T extends Metadata> T creationTime(Date creationTime) {
        this.creationTime = creationTime;
        return (T) this;
    }

    public <T extends Metadata> T lastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return (T) this;
    }

    public <T extends Metadata> T deleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
        return (T) this;
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
                new Property("lastUpdateTime", "raw", lastUpdateTime != null ? sdf.format(lastUpdateTime) : null),
                new Property("creationTime", "raw", creationTime != null ? sdf.format(creationTime) : null),
                new Property("deleteTime", "raw", deleteTime != null ? sdf.format(deleteTime) : null),
                new Property("authorization", "raw", Arrays.asList(authorization)))
                .stream()
                .filter(p -> p.getValue() != null)
                .collect(Collectors.toList()));
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
        node.put("creationUser", creationUser);
        node.put("creationTime", sdf.format(creationTime));
        node.put("lastUpdateUser", lastUpdateUser);
        node.put("lastUpdateTime", sdf.format(lastUpdateTime));
        //delete time is not mandatory
        if (deleteTime != null) node.put("deleteTime", sdf.format(deleteTime));
        return node;
    }
}
