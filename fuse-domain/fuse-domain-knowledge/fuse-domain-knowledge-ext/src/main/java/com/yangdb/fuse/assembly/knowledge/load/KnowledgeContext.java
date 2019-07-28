package com.yangdb.fuse.assembly.knowledge.load;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.yangdb.fuse.assembly.knowledge.load.builder.*;
import com.yangdb.fuse.model.resourceInfo.FuseError;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KnowledgeContext {
    private List<FuseError> failed;
    private List<EntityBuilder> entities;
    private List<ValueBuilder> eValues;
    private List<RelationBuilder> relations;
    private List<RvalueBuilder> rValues;
    private List<RelationBuilder.EntityRelationBuilder> relationBuilders;

    public KnowledgeContext() {
        entities = new ArrayList<>();
        eValues = new ArrayList<>();
        relations = new ArrayList<>();
        rValues = new ArrayList<>();
        relationBuilders = new ArrayList<>();
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

    public void add(RelationBuilder.EntityRelationBuilder builder) {
        relationBuilders.add(builder);
    }

    public void add(RelationBuilder builder) {
        relations.add(builder);
    }

    public void add(com.yangdb.fuse.assembly.knowledge.load.builder.RvalueBuilder builder) {
        rValues.add(builder);
    }

    public void addAll(List<RelationBuilder> relationBuilders) {
        relations.addAll(relationBuilders);
    }

    public List<EntityBuilder> getEntities() {
        return entities;
    }

    public List<RelationBuilder.EntityRelationBuilder> getRelationBuilders() {
        return relationBuilders;
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

    public Optional<EntityBuilder> findEntityByTechId(String id) {
        return entities.stream().filter(e->e.techId.equals(id)).findAny();
    }

    public Optional<EntityBuilder> findEntityByProperty(String property,String value) {
        return entities.stream()
                .filter(e->e.additionalProperties.containsKey(property))
                .filter(e->e.additionalProperties.get(property).equals(value))
                .findAny();
    }
}
