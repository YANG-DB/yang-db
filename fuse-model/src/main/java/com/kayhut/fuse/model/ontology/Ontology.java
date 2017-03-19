package com.kayhut.fuse.model.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.management.relation.Relation;
import java.util.List;
import java.util.Optional;

/**
 * Created by benishue on 22-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Ontology {

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

    @Override
    public String toString()
    {
        return "Ontology [enumeratedTypes = "+enumeratedTypes+", ont = "+ont+", relationshipTypes = "+relationshipTypes+", entityTypes = "+entityTypes+"]";
    }

    public Optional<String> getEntityLabel(int eType) {
        for(EntityType e : entityTypes) {
            if(e.geteType() == eType) {
                return Optional.of(e.getName());
            }
        }
        return Optional.empty();
    }

    public Optional<String> getRelationLabel(int rType) {
        for(RelationshipType r : relationshipTypes) {
            if(r.getrType() == rType) {
                return Optional.of(r.getName());
            }
        }
        return Optional.empty();
    }

    public Optional<Property> getProperty(int eType, int pType) {
        for(EntityType e : entityTypes) {
            if(e.geteType() == eType) {
                for(Property p : e.getProperties()) {
                    if(p.getpType() == pType) {
                        return Optional.of(p);
                    }
                }
            }
        }
        return Optional.empty();
    }


    //region Fields
    private String ont;
    private List<EntityType> entityTypes;
    private List<RelationshipType> relationshipTypes;
    private List<EnumeratedType> enumeratedTypes;
    //endregion

    //region Builder
    public static final class OntologyBuilder {
        private String ont;
        private List<EntityType> entityTypes;
        private List<RelationshipType> relationshipTypes;
        private List<EnumeratedType> enumeratedTypes;

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

        public Ontology build() {
            Ontology ontology = new Ontology();
            ontology.setOnt(ont);
            ontology.setEntityTypes(entityTypes);
            ontology.setRelationshipTypes(relationshipTypes);
            ontology.setEnumeratedTypes(enumeratedTypes);
            return ontology;
        }
    }
    //endregion

}
