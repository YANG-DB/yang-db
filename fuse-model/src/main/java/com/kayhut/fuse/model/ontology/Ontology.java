package com.kayhut.fuse.model.ontology;

/*-
 * #%L
 * Ontology.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.awt.geom.Point2D;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"primitiveTypes"})
public class Ontology {
    public Ontology() {
        primitiveTypes = new ArrayList<>();
        entityTypes = new ArrayList<>();
        relationshipTypes = new ArrayList<>();
        enumeratedTypes = new ArrayList<>();
        properties = new ArrayList<>();
        compositeTypes = new ArrayList<>();


        primitiveTypes.add(new PrimitiveType("int", Long.class));
        primitiveTypes.add(new PrimitiveType("string", String.class));
        primitiveTypes.add(new PrimitiveType("float", Double.class));
        primitiveTypes.add(new PrimitiveType("date", Date.class));
        primitiveTypes.add(new PrimitiveType("datetime", Date.class));
        primitiveTypes.add(new PrimitiveType("geo_shape", Point2D.class));
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
    public String toString()
    {
        return "Ontology [enumeratedTypes = "+enumeratedTypes+", ont = "+ont+", relationshipTypes = "+relationshipTypes+", entityTypes = "+entityTypes+"]";
    }

    //endregion

    //region Fields
    private String ont;
    private List<EntityType> entityTypes;
    private List<RelationshipType> relationshipTypes;
    private List<Property> properties;
    private List<EnumeratedType> enumeratedTypes;
    private List<CompositeType> compositeTypes;
    private List<PrimitiveType> primitiveTypes;
    //endregion

    //region Builder

    public static final class OntologyBuilder {
        private String ont;
        private List<EntityType> entityTypes;
        private List<RelationshipType> relationshipTypes;
        private List<Property> properties;
        private List<EnumeratedType> enumeratedTypes;
        private List<CompositeType> compositeTypes;

        private OntologyBuilder() {
            this.entityTypes = Collections.emptyList();
            this.relationshipTypes = Collections.emptyList();
            this.properties = Collections.emptyList();
            this.enumeratedTypes = Collections.emptyList();
            this.compositeTypes = Collections.emptyList();
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

        public OntologyBuilder withRelationshipTypes(List<RelationshipType> relationshipTypes) {
            this.relationshipTypes = relationshipTypes;
            return this;
        }

        public OntologyBuilder withEnumeratedTypes(List<EnumeratedType> enumeratedTypes) {
            this.enumeratedTypes = enumeratedTypes;
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

        public Ontology build() {
            Ontology ontology = new Ontology();
            ontology.setOnt(ont);
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

        public EntityType $entity$(String eType) {
            return $entity(eType).get();
        }

        public Optional<EntityType> entity(String entityName) {
            return Optional.ofNullable(this.entitiesByName.get(entityName));
        }

        public EntityType entity$(String entityName) {
            return entity(entityName).get();
        }

        public Optional<String> eType(String entityName) {
            EntityType entityType = this.entitiesByName.get(entityName);
            return entityType == null ? Optional.empty() : Optional.of(entityType.geteType());
        }

        public String eType$(String entityName) {
            return eType(entityName).get();
        }

        public Optional<RelationshipType> $relation(String rType) {
            return Optional.ofNullable(this.relationsByRtype.get(rType));
        }

        public RelationshipType $relation$(String rType) {
            return $relation(rType).get();
        }

        public Optional<RelationshipType> relation(String relationName) {
            return Optional.ofNullable(this.relationsByName.get(relationName));
        }

        public RelationshipType relation$(String relationName) {
            return relation(relationName).get();
        }

        public Optional<String> rType(String relationName) {
            RelationshipType relationshipType = this.relationsByName.get(relationName);
            return relationshipType == null ? Optional.empty() : Optional.of(relationshipType.getrType());
        }

        public String rType$(String relationName) {
            return rType(relationName).get();
        }

        public Optional<Property> $property(String pType) {
            return Optional.ofNullable(this.propertiesByPtype.get(pType));
        }

        public Property $property$(String pType) {
            return $property(pType).get();
        }

        public Optional<Property> property(String propertyName) {
            return Optional.ofNullable(this.propertiesByName.get(propertyName));
        }

        public Property property$(String propertyName) {
            return property(propertyName).get();
        }

        public Optional<String> pType(String propertyName) {
            Property property = this.propertiesByName.get(propertyName);
            return property == null ? Optional.empty() : Optional.of(property.getpType());
        }

        public String pType$(String propertyName) {
            return pType(propertyName).get();
        }

        public Iterable<String> pTypes() {
            return Stream.ofAll(ontology.getProperties()).map(Property::getpType).toJavaList();
        }

        public Iterable<EntityType> entities() {
            return Stream.ofAll(ontology.getEntityTypes()).toJavaList();
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
        //endregion
    }
    //endregion

}
