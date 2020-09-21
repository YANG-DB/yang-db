package com.yangdb.fuse.model.ontology;

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.ontology.DirectiveType.DirectiveClasses.*;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"primitiveTypes"})
public class Ontology {
    public Ontology() {
        directives = new ArrayList<>();
        primitiveTypes = new ArrayList<>();
        entityTypes = new ArrayList<>();
        relationshipTypes = new ArrayList<>();
        enumeratedTypes = new ArrayList<>();
        properties = new ArrayList<>();
        compositeTypes = new ArrayList<>();

        primitiveTypes.add(new PrimitiveType("int", Long.class));
        primitiveTypes.add(new PrimitiveType("string", String.class));
        primitiveTypes.add(new PrimitiveType("text", String.class));
        primitiveTypes.add(new PrimitiveType("float", Double.class));
        primitiveTypes.add(new PrimitiveType("date", Date.class));
        primitiveTypes.add(new PrimitiveType("datetime", Date.class));
        primitiveTypes.add(new PrimitiveType("geo_point", Point2D.class));
        primitiveTypes.add(new PrimitiveType("array", Array.class));
    }

    //region Getters & Setters

    public String getOnt() {
        return ont;
    }

    public void setOnt(String ont) {
        this.ont = ont;
    }

    public List<EntityType> getEntityTypes() {
        return entityTypes;
    }

    public List<Property> getProperties() {
        return this.properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public void setEntityTypes(List<EntityType> entityTypes) {
        this.entityTypes = entityTypes;
    }

    public List<RelationshipType> getRelationshipTypes() {
        return relationshipTypes;
    }

    public List<DirectiveType> getDirectives() {
        return directives;
    }

    public void setDirectives(List<DirectiveType> directives) {
        this.directives = directives;
    }

    public void setRelationshipTypes(List<RelationshipType> relationshipTypes) {
        this.relationshipTypes = relationshipTypes;
    }

    public List<EnumeratedType> getEnumeratedTypes() {
        return enumeratedTypes;
    }

    public void setEnumeratedTypes(List<EnumeratedType> enumeratedTypes) {
        this.enumeratedTypes = enumeratedTypes;
    }

    public List<CompositeType> getCompositeTypes() {
        return compositeTypes;
    }

    public void setCompositeTypes(List<CompositeType> compositeTypes) {
        this.compositeTypes = compositeTypes;
    }

    public List<PrimitiveType> getPrimitiveTypes() {
        return primitiveTypes;
    }

    //endregion

    //region Public Methods

    @Override
    public String toString() {
        return "Ontology [enumeratedTypes = " + enumeratedTypes + ", ont = " + ont + ", relationshipTypes = " + relationshipTypes + ", entityTypes = " + entityTypes + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ontology ontology = (Ontology) o;
        return ont.equals(ontology.ont) &&
                directives.equals(ontology.directives) &&
                entityTypes.equals(ontology.entityTypes) &&
                relationshipTypes.equals(ontology.relationshipTypes) &&
                properties.equals(ontology.properties) &&
                metadata.equals(ontology.metadata) &&
                enumeratedTypes.equals(ontology.enumeratedTypes) &&
                compositeTypes.equals(ontology.compositeTypes) &&
                primitiveTypes.equals(ontology.primitiveTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ont,directives, entityTypes, relationshipTypes, properties, metadata, enumeratedTypes, compositeTypes, primitiveTypes);
    }

    //endregion

    //region Fields
    private String ont;
    private List<DirectiveType> directives;
    private List<EntityType> entityTypes;
    private List<RelationshipType> relationshipTypes;
    private List<Property> properties;
    private List<Property> metadata;
    private List<EnumeratedType> enumeratedTypes;
    private List<CompositeType> compositeTypes;
    private List<PrimitiveType> primitiveTypes;
    //endregion

    //region Builder

    public static final class OntologyBuilder {
        private String ont = "Generic";
        private List<DirectiveType> directives;
        private List<EntityType> entityTypes;
        private List<RelationshipType> relationshipTypes;
        private List<Property> properties;
        private List<EnumeratedType> enumeratedTypes;
        private List<CompositeType> compositeTypes;

        private OntologyBuilder() {
            this.directives = new ArrayList<>();
            this.entityTypes = new ArrayList<>();
            this.relationshipTypes = new ArrayList<>();
            this.properties = new ArrayList<>();
            this.enumeratedTypes = new ArrayList<>();
            this.compositeTypes = new ArrayList<>();
        }

        public static OntologyBuilder anOntology() {
            return new OntologyBuilder();
        }

        public OntologyBuilder withOnt(String ont) {
            this.ont = ont;
            return this;
        }

        public OntologyBuilder withEntityTypes(List<EntityType> entityTypes) {
            this.entityTypes = entityTypes;
            return this;
        }

        public OntologyBuilder addEntityTypes(List<EntityType> entityTypes) {
            this.entityTypes.addAll(entityTypes);
            return this;
        }

        public OntologyBuilder addEntityType(EntityType entityType) {
            this.entityTypes.add(entityType);
            return this;
        }

        public Optional<EntityType> getEntityType(String entityType) {
            return this.entityTypes.stream().filter(et -> et.geteType().equals(entityType)).findAny();
        }

        public OntologyBuilder withRelationshipTypes(List<RelationshipType> relationshipTypes) {
            this.relationshipTypes = relationshipTypes;
            return this;
        }

        public OntologyBuilder addRelationshipTypes(List<RelationshipType> relationshipTypes) {
            this.relationshipTypes.addAll(relationshipTypes);
            return this;
        }

        public Optional<RelationshipType> getRelationshipType(String relationshipType) {
            return this.relationshipTypes.stream().filter(et -> et.getrType().equals(relationshipType)).findAny();
        }

        public OntologyBuilder addRelationshipType(RelationshipType relationshipType) {
            this.relationshipTypes.add(relationshipType);
            return this;
        }

        public Optional<Property> getProperty(String property) {
            return this.properties.stream().filter(et -> et.getType().equals(property)).findAny();
        }

        public OntologyBuilder withDirective(DirectiveType directive) {
            this.directives.add(directive);
            return this;

        }

        public OntologyBuilder withDirectives(List<DirectiveType> directives) {
            this.directives.addAll(directives);
            return this;

        }
        public OntologyBuilder withEnumeratedTypes(List<EnumeratedType> enumeratedTypes) {
            this.enumeratedTypes = enumeratedTypes;
            return this;
        }

        public OntologyBuilder addEnumeratedTypes(EnumeratedType enumeratedType) {
            this.enumeratedTypes.add(enumeratedType);
            return this;
        }

        public OntologyBuilder withCompositeTypes(List<CompositeType> compositeTypes) {
            this.compositeTypes = compositeTypes;
            return this;
        }

        public OntologyBuilder withProperties(List<Property> properties) {
            this.properties = properties;
            return this;
        }

        public OntologyBuilder addProperty(Property property) {
            this.properties.add(property);
            return this;
        }

        public OntologyBuilder addProperties(List<Property> properties) {
            this.properties.addAll(properties);
            return this;
        }

        public Ontology build() {
            Ontology ontology = new Ontology();
            ontology.setOnt(ont);
            ontology.setDirectives(directives);
            ontology.setEntityTypes(entityTypes);
            ontology.setRelationshipTypes(relationshipTypes);
            ontology.setEnumeratedTypes(enumeratedTypes);
            ontology.setCompositeTypes(compositeTypes);
            ontology.setProperties(properties);
            return ontology;
        }

    }

    //endregion

    //region Accessor
    public static class Accessor implements Supplier<Ontology> {
        //region Constructors
        public Accessor(Ontology ontology) {
            this.ontology = ontology;

            this.entitiesByEtype = Stream.ofAll(ontology.getEntityTypes())
                    .toJavaMap(entityType -> new Tuple2<>(entityType.geteType(), entityType));
            this.entitiesByName = Stream.ofAll(ontology.getEntityTypes())
                    .toJavaMap(entityType -> new Tuple2<>(entityType.getName(), entityType));

            this.relationsByRtype = Stream.ofAll(ontology.getRelationshipTypes())
                    .toJavaMap(relationshipType -> new Tuple2<>(relationshipType.getrType(), relationshipType));
            this.relationsByName = Stream.ofAll(ontology.getRelationshipTypes())
                    .toJavaMap(relationshipType -> new Tuple2<>(relationshipType.getName(), relationshipType));

            this.propertiesByPtype = Stream.ofAll(ontology.getProperties())
                    .toJavaMap(property -> new Tuple2<>(property.getpType(), property));
            this.propertiesByName = Stream.ofAll(ontology.getProperties())
                    .toJavaMap(property -> new Tuple2<>(property.getName(), property));
        }
        //endregion

        //region Public Methods
        @Override
        public Ontology get() {
            return this.ontology;
        }

        public String name() {
            return this.ontology.getOnt();
        }

        public Optional<EntityType> $entity(String eType) {
            return Optional.ofNullable(this.entitiesByEtype.get(eType));
        }

        public Optional<DirectiveType> $directive(String name) {
            return this.ontology.directives.stream().filter(d->d.getName().equals(name)).findFirst();
        }

        public DirectiveType $directive$(String name) {
            return this.ontology.directives.stream().filter(d->d.getName().equals(name)).findFirst()
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology $directive$ for value ", "No Ontology $directive$ for value[" + name+"]")));
        }

        public EntityType $entity$(String eType) {
            return $entity(eType)
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology entity for value ", "No Ontology entity for value[" + eType+"]")));
        }

        public Optional<EntityType> entity(String entityName) {
            return Optional.ofNullable(this.entitiesByName.get(entityName));
        }

        public EntityType entity$(String entityName) {
            return entity(entityName)
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology entityType for value ", "No Ontology entityType for value[" + entityName+"]")));
        }

        public Optional<String> eType(String entityName) {
            EntityType entityType = this.entitiesByName.get(entityName);
            return entityType == null ? Optional.empty() : Optional.of(entityType.geteType());
        }

        public String eType$(String entityName) {
            return eType(entityName)
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology entityType for value ", "No Ontology entityType for value[" + entityName+"]")));
        }

        public Optional<RelationshipType> $relation(String rType) {
            return Optional.ofNullable(this.relationsByRtype.get(rType));
        }

        public RelationshipType $relation$(String rType) {
            return $relation(rType)
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology Relation for value ", "No Ontology Relation for value[" + rType+"]")));
        }

        public Optional<RelationshipType> relation(String relationName) {
            return Optional.ofNullable(this.relationsByName.get(relationName));
        }

        public RelationshipType relation$(String relationName) {
            return relation(relationName)
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology relationName for value ", "No Ontology relationName for value[" + relationName+"]")));
        }

        public Optional<String> rType(String relationName) {
            RelationshipType relationshipType = this.relationsByName.get(relationName);
            return relationshipType == null ? Optional.empty() : Optional.of(relationshipType.getrType());
        }

        public String rType$(String relationName) {
            return rType(relationName)
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology relationName for value ", "No Ontology relationName for value[" + relationName+"]")));
        }

        public Optional<Property> $property(String pType) {
            return Optional.ofNullable(this.propertiesByPtype.get(pType));
        }

        public Property $property$(String pType) {
            if (!$property(pType).isPresent())
                throw new IllegalArgumentException(String.format("No Such ontology value present %s", pType));
            return $property(pType).get();
        }

        public Optional<Property> property(String propertyName) {
            return Optional.ofNullable(this.propertiesByName.get(propertyName));
        }

        public List<Property> properties() {
            return this.ontology.properties;
        }

        public Property property$(String propertyName) {
            return property(propertyName)
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology propertyName for value ", "No Ontology propertyName for value[" + propertyName+"]")));
        }

        public Optional<String> pType(String propertyName) {
            Property property = this.propertiesByName.get(propertyName);
            return property == null ? Optional.empty() : Optional.of(property.getpType());
        }

        public String pType$(String propertyName) {
            return pType(propertyName)
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology propertyName for value ", "No Ontology propertyName for value[" + propertyName+"]")));
        }

        public Iterable<String> pTypes() {
            return Stream.ofAll(ontology.getProperties()).map(Property::getpType).toJavaList();
        }

        public Iterable<EntityType> entities() {
            return Stream.ofAll(ontology.getEntityTypes()).toJavaList();
        }

        public boolean containsMetadata(String pType) {
            return Stream.ofAll(ontology.entityTypes).flatMap(EntityType::getMetadata).toJavaSet().contains(pType);
        }

        public List<EntityType> nested$(String eType) {
            return entity$(eType).getProperties().stream()
                    .filter(p -> $entity(p).isPresent())
                    .map(p -> $entity(p).get())
                    .collect(Collectors.toList());

        }

        public boolean isNested(String eType) {
            if (!entity(eType).isPresent()) return false;

            return entity$(eType).getProperties().stream().anyMatch(p -> $entity(p).isPresent());
        }

        public Iterable<String> eNames() {
            return Stream.ofAll(entities()).map(EntityType::getName).toJavaList();
        }

        public Iterable<String> eTypes() {
            return Stream.ofAll(ontology.getEntityTypes()).map(EntityType::geteType).toJavaList();
        }

        public List<RelationshipType> relations() {
            return Stream.ofAll(ontology.getRelationshipTypes()).toJavaList();
        }

        public List<RelationshipType> relationBySideA(String eType) {
            return Stream.ofAll(ontology.getRelationshipTypes()).filter(r -> r.hasSideA(eType)).toJavaList();
        }

        public List<RelationshipType> relationBySideB(String eType) {
            return Stream.ofAll(ontology.getRelationshipTypes()).filter(r -> r.hasSideB(eType)).toJavaList();
        }

        public Iterable<String> rTypes() {
            return Stream.ofAll(relations()).map(RelationshipType::getrType).toJavaList();
        }

        public Iterable<String> rNames() {
            return Stream.ofAll(relations()).map(RelationshipType::getName).toJavaList();
        }

        public Optional<PrimitiveType> primitiveType(String typeName) {
            return Stream.ofAll(ontology.getPrimitiveTypes())
                    .filter(type -> type.getType().equals(typeName))
                    .toJavaOptional();
        }

        public PrimitiveType primitiveType$(String typeName) {
            return primitiveType(typeName).get();
        }


        public List<EnumeratedType> getEnumeratedTypes() {
            return ontology.getEnumeratedTypes();
        }

        public Optional<EnumeratedType> enumeratedType(String typeName) {
            return Stream.ofAll(ontology.getEnumeratedTypes())
                    .filter(type -> type.geteType().equals(typeName))
                    .toJavaOptional();
        }

        public EnumeratedType enumeratedType$(String typeName) {
            return enumeratedType(typeName).get();
        }
        //endregion

        //region Fields
        private Ontology ontology;

        private Map<String, EntityType> entitiesByEtype;
        private Map<String, EntityType> entitiesByName;

        private Map<String, RelationshipType> relationsByRtype;
        private Map<String, RelationshipType> relationsByName;

        private Map<String, Property> propertiesByName;
        private Map<String, Property> propertiesByPtype;

        /**
         * match named element to true type (included typed value identifier)
         * @param name
         * @return
         */
        public Optional<Tuple2<NodeType,String>> matchNameToType(String name) {
            if(eType(name).isPresent())
                return Optional.of(Tuple.of(NodeType.ENTITY, eType$(name)));
            if(rType(name).isPresent())
                return Optional.of(Tuple.of(NodeType.RELATION, rType$(name)));
            if(property(name).isPresent())
                return Optional.of(Tuple.of(NodeType.PROPERTY, property$(name).getpType()));

            return Optional.empty();
        }

        public enum NodeType {
            PROPERTY,RELATION,ENTITY
        }

        //endregion
    }
    //endregion

}
