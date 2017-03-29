package com.kayhut.fuse.model.ontology;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by benishue on 12-Mar-17.
 */
public interface OntologyUtil {

    static String getEntityTypeNameById(Ontology ontology, int eTypeId){
        Optional<EntityType> entityTypeMatch = ontology.getEntityTypes().stream()
                .filter(entityType -> entityType.geteType() == eTypeId)
                .findFirst();
        String entityTypeName ;
        if (entityTypeMatch.isPresent()) {
            entityTypeName = entityTypeMatch.get().getName();
        }
        else {
            throw new IllegalArgumentException("Not Supported Entity Type Id: " + eTypeId);
        }
        return entityTypeName;
    }

    static String getRelationTypeNameById(Ontology ontology, int rTypeId){
        Optional<RelationshipType> relationTypeMatch = ontology.getRelationshipTypes().stream()
                .filter(relationshipType-> relationshipType.getrType() == rTypeId)
                .findFirst();
        String relationTypeName ;
        if (relationTypeMatch.isPresent()) {
            relationTypeName = relationTypeMatch.get().getName();
        }
        else {
            throw new IllegalArgumentException("Not Supported Relation Type Id: " + rTypeId);
        }
        return relationTypeName;
    }

    static Optional<String> getEntityLabel(Ontology ontology,int eType) {
        for(EntityType e : ontology.getEntityTypes()) {
            if(e.geteType() == eType) {
                return Optional.of(e.getName());
            }
        }
        return Optional.empty();
    }

    static Optional<List<String>> getAllEntityLabels(Ontology ontology)
    {
        List<String> entityLabels = ontology.getEntityTypes().stream().map(p -> p.getName()).collect(Collectors.toList());
        if (entityLabels.size() != 0)
            return Optional.of(entityLabels);
        else
            return Optional.empty();
    }

    static Optional<List<String>> getAllRelationshipTypeLabels(Ontology ontology)
    {
        List<String> relationshipTypeLabels = ontology.getRelationshipTypes().stream().map(p -> p.getName()).collect(Collectors.toList());
        if (relationshipTypeLabels.size() != 0)
            return Optional.of(relationshipTypeLabels);
        else
            return Optional.empty();
    }

    static Optional<String> getRelationLabel(Ontology ontology,int rType) {
        for(RelationshipType r : ontology.getRelationshipTypes()) {
            if(r.getrType() == rType) {
                return Optional.of(r.getName());
            }
        }
        return Optional.empty();
    }

    static Optional<Property> getProperty(Ontology ontology,int eType, int pType) {
        for(EntityType e : ontology.getEntityTypes()) {
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

    static Optional<RelationshipType> getRelationshipType(Ontology ontology,String name) {
        Optional<RelationshipType> relationTypeMatch = ontology.getRelationshipTypes().stream()
                .filter(relationshipType-> relationshipType.getName().equals(name))
                .findFirst();
        RelationshipType relationshipType;
        if (relationTypeMatch.isPresent()) {
            relationshipType = relationTypeMatch.get();
        }
        else {
            throw new IllegalArgumentException("Not Supported Relation Type: " + name);
        }
        return Optional.of(relationshipType);
    }


}
