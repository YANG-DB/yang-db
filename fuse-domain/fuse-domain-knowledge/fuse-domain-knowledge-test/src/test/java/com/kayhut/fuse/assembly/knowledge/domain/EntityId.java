package com.kayhut.fuse.assembly.knowledge.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;

//todo - for kobi usage
public abstract class EntityId extends Metadata{
    public String entityId;
    public String logicalId;
}
