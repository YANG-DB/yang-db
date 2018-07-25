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
    public String creationTime = sdf.format(new Date(System.currentTimeMillis()));
    public String lastUpdateTime = sdf.format(new Date(System.currentTimeMillis()));
    public String[] authorization = new String[]{"procedure.1", "procedure.2"};

    public Metadata() {
    }

    public Metadata(Metadata builder) {
        this.creationTime = builder.creationTime;
        this.creationUser = builder.creationUser;
        this.lastUpdateTime = builder.lastUpdateTime;
        this.lastUpdateUser = builder.lastUpdateUser;
    }

    public List<Property> collect(List<Property> properties) {
        ArrayList<Property> list = new ArrayList<>(properties);
        list.addAll(Arrays.asList(
                new Property("lastUpdateUser", "raw", lastUpdateUser),
                new Property("creationUser", "raw", creationUser),
                new Property("lastUpdateTime", "raw", lastUpdateTime),
                new Property("creationTime", "raw", creationTime),
                new Property("authorization", "raw", Arrays.asList(authorization))));
        return list;
    }

    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ArrayNode authNode = mapper.createArrayNode();
        for (String auth : authorization) {
            authNode.add(auth);
        }

        node.put("authorizationCount", authNode.size());
        node.put("authorization", authNode); // Authorization = Clearance
        node.put("lastUpdateUser", lastUpdateUser);
        node.put("lastUpdateTime", lastUpdateTime);
        node.put("creationUser", creationUser);
        node.put("creationTime", creationTime);
        return node;
    }


}
