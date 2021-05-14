package com.yangdb.fuse.model.schema;

/*-
 * #%L
 * fuse-model
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


/*-
 *
 * Ontology.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ImmutableList;
import com.yangdb.fuse.model.ontology.BaseElement;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yangdb.fuse.model.schema.MappingIndexType.STATIC;
import static com.yangdb.fuse.model.schema.MappingIndexType.UNIFIED;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "entities",
        "relations"
})
public class IndexProvider {

    @JsonProperty("ontology")
    private String ontology;
    @JsonProperty("entities")
    private List<Entity> entities = new ArrayList<>();
    @JsonProperty("relations")
    private List<Relation> relations = new ArrayList<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("entities")
    public List<Entity> getEntities() {
        return Stream.concat(entities.stream()
                .filter(e -> !e.getNested().isEmpty())
                .flatMap(e -> e.getNested().stream()), entities.stream())
                .collect(Collectors.toList());
    }

    @JsonProperty("entities")
    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    @JsonProperty("relations")
    public List<Relation> getRelations() {
        return Stream.concat(relations.stream()
                .filter(e -> !e.getNested().isEmpty())
                .flatMap(e -> e.getNested().stream()), relations.stream())
                .collect(Collectors.toList());
    }

    @JsonProperty("relations")
    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    @JsonProperty("ontology")
    public String getOntology() {
        return ontology;
    }

    @JsonProperty("ontology")
    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonIgnore
    public IndexProvider withEntity(Entity entity) {
        entities.add(entity);
        return this;
    }

    @JsonIgnore
    public IndexProvider withRelation(Relation relation) {
        relations.add(relation);
        return this;
    }

    @JsonIgnore
    public Optional<Entity> getEntity(String label) {
        Optional<Entity> nest = getEntities().stream().filter(e -> !e.getNested().isEmpty())
                .flatMap(e -> e.getNested().stream())
                .filter(nested -> nested.getType().equals(label))
                .findAny();
        if (nest.isPresent())
            return nest;

        return getEntities().stream().filter(e -> e.getType().equals(label)).findAny();
    }

    @JsonIgnore
    public Optional<Entity> getEntityByProp(String prop) {
        Optional<Entity> nest = getEntities().stream().filter(e -> !e.getNested().isEmpty())
                .flatMap(e -> e.getNested().stream())
                .filter(nested -> nested.getProps().getValues().contains(prop))
                .findAny();
        if (nest.isPresent())
            return nest;

        return getEntities().stream().filter(e -> e.getProps().getValues().contains(prop)).findAny();
    }

    @JsonIgnore
    public Optional<Relation> getRelation(String label) {
        Optional<Relation> nest = getRelations().stream().filter(e -> !e.getNested().isEmpty())
                .flatMap(e -> e.getNested().stream())
                .filter(nested -> nested.getType().equals(label))
                .findAny();
        if (nest.isPresent())
            return nest;

        return getRelations().stream().filter(e -> e.getType().equals(label)).findAny();
    }

    public static class Builder {

        public static IndexProvider generate(String ontologyName) {
            IndexProvider provider = new IndexProvider();
            provider.ontology = ontologyName;
            return provider;
        }

        /**
         * creates default index provider according to the given ontology - simple static index strategy
         *
         * @param ontology
         * @return
         */
        //todo - refactor this so that all base elements (entityType, relationType) are collected according to schemaName and
        // all similar schema names are joined with the all the collected parameters
        public static IndexProvider generate(Ontology ontology) {
            IndexProvider provider = new IndexProvider();
            provider.ontology = ontology.getOnt();
            //dedup entities according to physical schema name and select the first representing index of each schematic name & collect all group's properties into that representative
            List<BaseElement> dedupedEntities = ontology.getEntityTypes().stream().collect(Collectors.groupingBy(EntityType::getSchemaName,
                    Collectors.toSet())).values().stream().flatMap(v -> v.stream().limit(1)).collect(Collectors.toList());

            //dedup relations according to physical schema name and select the first representing index of each schematic name & collect all group's properties into that representative
            List<BaseElement> dedupedRelations = ontology.getRelationshipTypes().stream().collect(Collectors.groupingBy(RelationshipType::getSchemaName,
                    Collectors.toSet())).values().stream().flatMap(v -> v.stream().limit(1)).collect(Collectors.toList());

            //generate entities
            provider.entities = dedupedEntities.stream().map(e ->
                            new Entity(e.getName(), STATIC.name(), PartitionType.INDEX.name(),
                                    //E/S indices need to be lower cased
                                    new Props(ImmutableList.of(e.getSchemaName())),
                                    Collections.emptyList(), Collections.emptyMap()))
                    .collect(Collectors.toList());

            //generate relations
            provider.relations = dedupedRelations.stream().map(e -> {
                // verify if relationship exists with same DB name as index we need to create a UNIFIED type mapping
                if (provider.getEntityByProp(e.getSchemaName()).isPresent()) {
                    //update type to UNIFIED
                    provider.getEntityByProp(e.getSchemaName()).get()
                            .setPartition(UNIFIED.name());
                    //add fields
                }
                return new Relation(e.getName(), STATIC.name(), PartitionType.INDEX.name(), false, Collections.emptyList(),
                        //E/S indices need to be lower cased
                        new Props(ImmutableList.of(e.getSchemaName())),
                        Collections.emptyList(), Collections.emptyMap());
            }).collect(Collectors.toList());

            //todo collect all similar schema names elements and combine them - practically will always remove entity index provider to be combined
            //with its associated relation index provider and also should copy the properties from the combined entity
            return provider;
        }


    }
}
