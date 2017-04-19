package com.kayhut.fuse.model.ontology;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private List<EnumeratedType> enumeratedTypes;
    private List<CompositeType> compositeTypes;
    private List<PrimitiveType> primitiveTypes;
    //endregion

    //region Builder

    public static final class OntologyBuilder {
        private String ont;
        private List<EntityType> entityTypes;
        private List<RelationshipType> relationshipTypes;
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

        public Ontology build() {
            Ontology ontology = new Ontology();
            ontology.setOnt(ont);
            ontology.setEntityTypes(entityTypes);
            ontology.setRelationshipTypes(relationshipTypes);
            ontology.setEnumeratedTypes(enumeratedTypes);
            ontology.setCompositeTypes(compositeTypes);
            return ontology;
        }
    }

    //endregion

}
