package com.kayhut.fuse.assembly.knowledge.load;

import com.kayhut.fuse.assembly.knowledge.load.builder.*;
import com.kayhut.fuse.model.resourceInfo.FuseError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KnowledgeContext {
    private List<FuseError> failed;
    private List<EntityBuilder> entities;
    private List<ValueBuilder> eValues;
    private List<RelationBuilder> relations;
    private List<RvalueBuilder> rValues;

    public KnowledgeContext() {
        entities = new ArrayList<>();
        eValues = new ArrayList<>();
        relations = new ArrayList<>();
        rValues = new ArrayList<>();
        failed = new ArrayList<>();
    }

    public void failed(String error,String desc) {
        failed.add(new FuseError(error,desc));
    }

    public void add(ValueBuilder builder) {
        eValues.add(builder);
    }

    public void add(EntityBuilder builder) {
        entities.add(builder);
    }

    public void add(RelationBuilder builder) {
        relations.add(builder);
    }

    public void add(com.kayhut.fuse.assembly.knowledge.load.builder.RvalueBuilder builder) {
        rValues.add(builder);
    }

    public void addAll(List<RelationBuilder> relationBuilders) {
        relations.addAll(relationBuilders);
    }

    public List<EntityBuilder> getEntities() {
        return entities;
    }

    public List<ValueBuilder> geteValues() {
        return eValues;
    }

    public List<RelationBuilder> getRelations() {
        return relations;
    }

    public List<RvalueBuilder> getrValues() {
        return rValues;
    }

    public Optional<EntityBuilder> findEntityById(String id) {
        return entities.stream().filter(e->e.logicalId.equals(id)).findAny();
    }

    public Optional<EntityBuilder> findEntityByProperty(String property,String value) {
        return entities.stream()
                .filter(e->e.additionalProperties.containsKey(property))
                .filter(e->e.additionalProperties.get(property).equals(value))
                .findAny();
    }
}
