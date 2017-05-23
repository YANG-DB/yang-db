package com.kayhut.fuse.model.ontology;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import javaslang.collection.Stream;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties({"primitiveTypes"})
public class Ontology {
    public Ontology() {
        primitiveTypes = new ArrayList<>();
        primitiveTypes.add(new PrimitiveType("int", Long.class));
        primitiveTypes.add(new PrimitiveType("string", String.class));
        primitiveTypes.add(new PrimitiveType("float", Double.class));
        primitiveTypes.add(new PrimitiveType("date", Date.class));
        primitiveTypes.add(new PrimitiveType("datetime", Date.class));
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

        public Optional<EntityType> $entity(int eType) {
            return Stream.ofAll(ontology.getEntityTypes())
                    .filter(entityType -> entityType.geteType() == eType)
                    .toJavaOptional();
        }

        public EntityType $entity$(int eType) {
            return $entity(eType).get();
        }

        public Optional<EntityType> entity(String entityName) {
            return Stream.ofAll(ontology.getEntityTypes())
                    .filter(entityType -> entityType.getName().equals(entityName))
                    .toJavaOptional();
        }

        public EntityType entity$(String entityName) {
            return entity(entityName).get();
        }

        public Optional<Integer> eType(String entityName) {
            return Stream.ofAll(ontology.getEntityTypes())
                    .filter(entityType -> entityType.getName().equals(entityName))
                    .map(EntityType::geteType)
                    .toJavaOptional();
        }

        public int eType$(String entityName) {
            return eType(entityName).get();
        }

        public Optional<RelationshipType> $relation(int rType) {
            return Stream.ofAll(ontology.getRelationshipTypes())
                    .filter(relationshipType -> relationshipType.getrType() == rType)
                    .toJavaOptional();
        }

        public RelationshipType $relation$(int rType) {
            return $relation(rType).get();
        }

        public Optional<RelationshipType> relation(String relationName) {
            return Stream.ofAll(ontology.getRelationshipTypes())
                    .filter(relationshipType -> relationshipType.getName().equals(relationName))
                    .toJavaOptional();
        }

        public RelationshipType relation$(String relationName) {
            return relation(relationName).get();
        }

        public Optional<Integer> rType(String relationName) {
            return Stream.ofAll(ontology.getRelationshipTypes())
                    .filter(relationshipType -> relationshipType.getName().equals(relationName))
                    .map(RelationshipType::getrType)
                    .toJavaOptional();
        }

        public Integer rType$(String relationName) {
            return rType(relationName).get();
        }

        public Optional<Property> $property(int pType) {
            return Stream.ofAll(ontology.getProperties())
                    .filter(property -> property.getpType() == pType)
                    .toJavaOptional();
        }

        public Property $property$(int pType) {
            return $property(pType).get();
        }

        public Optional<Property> property(String propertyName) {
            return Stream.ofAll(ontology.getProperties())
                    .filter(property -> property.getName().equals(propertyName))
                    .toJavaOptional();
        }

        public Property property$(String propertyName) {
            return property(propertyName).get();
        }

        public Optional<Integer> pType(String propertyName) {
            return Stream.ofAll(ontology.getProperties())
                    .filter(property -> property.getName().equals(propertyName))
                    .map(Property::getpType)
                    .toJavaOptional();
        }

        public Integer pType$(String propertyName) {
            return pType(propertyName).get();
        }

        public List<EntityType> entities() {
            return Stream.ofAll(ontology.getEntityTypes()).toJavaList();
        }

        public List<Integer> eTypes() {
            return Stream.ofAll(ontology.getEntityTypes()).map(EntityType::geteType).toJavaList();
        }

        public List<RelationshipType> relations() {
            return Stream.ofAll(ontology.getRelationshipTypes()).toJavaList();
        }
        //endregion

        //region Fields
        private Ontology ontology;
        //endregion
    }
    //endregion

}
