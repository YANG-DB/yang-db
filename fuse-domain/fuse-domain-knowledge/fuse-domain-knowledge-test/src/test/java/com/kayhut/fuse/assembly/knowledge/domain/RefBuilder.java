package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import javaslang.collection.Stream;

import java.util.Arrays;

import static com.kayhut.fuse.assembly.knowledge.domain.KnowlegdeOntology.logicalId;

//todo - for kobi usage
public class RefBuilder extends Metadata {
    public static final String REF_INDEX = "ref0";

    public static final String type = "Reference";
    public static String physicalType = "reference";

    public String title;
    public String url;
    public String content;
    public String system;
    private String refId;

    public static RefBuilder _ref(String id) {
        final RefBuilder builder = new RefBuilder();
        builder.refId = id;
        return builder;
    }

    public RefBuilder title(String title) {
        this.title = title;
        return this;
    }

    public RefBuilder content(String content) {
        this.content = content;
        return this;
    }

    public RefBuilder url(String url) {
        this.url = url;
        return this;
    }

    public RefBuilder sys(String sys) {
        this.system = sys;
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String id() {
        return refId;
    }

    @Override
    public ObjectNode collect(ObjectMapper mapper, ObjectNode node) {
        ObjectNode on = super.collect(mapper, node);
        //create knowledge entity
        on.put("id", id());
        on.put("type", physicalType);
        on.put("title", title);
        on.put("url", url);
        on.put("system", system);
        //make sure value or content
        if (content != null) on.put("content", content);
        return on;
    }

    @Override
    public Entity
    toEntity() {
        return Entity.Builder.instance()
                .withEID(id())
                .withETag(Stream.of(getETag()).toJavaSet())
                .withEType(getType())
                .withProperties(collect(Stream.ofAll(Arrays.asList(
                        new Property("title", "raw", title),
                        new Property("content", "raw", content),
                        new Property("url", "raw", url),
                        new Property("system", "raw", system)))
                        .filter(p -> p.getValue() != null).toJavaList()
                )).build();
    }

    @Override
    public String getETag() {
        return "Reference." + id();
    }
}
