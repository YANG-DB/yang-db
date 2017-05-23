package com.kayhut.fuse.model.ontology;

import com.kayhut.fuse.model.query.entity.EUntyped;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by benishue on 12-Mar-17.
 */
public class OntologyUtil {
    /*public static int getEntityTypeIdByName(Ontology ontology,String name) {
        Optional<EntityType> entityTypeMatch = ontology.getEntityTypes().stream()
                .filter(entityType -> entityType.getName().equals(name))
                .findFirst();
        int entityTypeId ;
        if (entityTypeMatch.isPresent()) {
            entityTypeId = entityTypeMatch.get().geteType();
        }
        else {
            throw new IllegalArgumentException("Not Supported Entity Type name: " + name);
        }
        return entityTypeId;
    }*/

    /*public static String getEntityTypeNameById(Ontology ontology, int eTypeId){
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
    }*/

    public static String getRelationTypeNameById(Ontology ontology, int rTypeId){
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

    public static Optional<List<String>> getAllEntityLabels(Ontology ontology) {
        List<String> entityLabels = ontology.getEntityTypes().stream().map(p -> p.getName()).collect(Collectors.toList());
        if (entityLabels.size() != 0)
            return Optional.of(entityLabels);
        else
            return Optional.empty();
    }

    public static Optional<List<String>> getAllRelationshipTypeLabels(Ontology ontology) {
        List<String> relationshipTypeLabels = ontology.getRelationshipTypes().stream().map(p -> p.getName()).collect(Collectors.toList());
        if (relationshipTypeLabels.size() != 0)
            return Optional.of(relationshipTypeLabels);
        else
            return Optional.empty();
    }

    public static Optional<String> getRelationLabel(Ontology ontology, int rType) {
        for(RelationshipType r : ontology.getRelationshipTypes()) {
            if(r.getrType() == rType) {
                return Optional.of(r.getName());
            }
        }
        return Optional.empty();
    }

    /*static Optional<Property> getProperty(Ontology ontology, int eType, String pType) {
        Optional<EntityType> entityType = ontology.getEntityTypes()
                .stream().filter(entityType1 -> entityType1.geteType() == eType).findFirst();
        if (!entityType.isPresent())
            return Optional.empty();

        if (pType.contains("."))
        {
            return getCompositeProperty(ontology, pType, entityType.get().getProperties());
        }
        else {
            Optional<Property> p = getProperty(pType, entityType.get().getProperties());
            if (p.isPresent())
                return p;
        }
        return Optional.empty();
    }*/

    /*static Optional<Property> getRelationshipProperty(Ontology ontology, int rType, String pType) {
        Optional<RelationshipType> relationType = ontology.getRelationshipTypes()
                .stream().filter(relationshipType -> relationshipType.getrType() == rType).findFirst();
        if (!relationType.isPresent())
            return Optional.empty();

        //This is a composite type - we support currently only 2 levels for now (e.g., 1.2)
        if (pType.contains("."))
        {
            return getCompositeProperty(ontology, pType, relationType.get().getProperties());
        }
        else {
            Optional<Property> p = getProperty(pType, relationType.get().getProperties());
            if (p.isPresent())
                return p;
        }
        return Optional.empty();
    }*/

    public static Optional<Property> getProperty(Ontology ontology, int pType) {
        return Stream.ofAll(ontology.getProperties())
                .filter(property -> property.getpType() == pType)
                .toJavaOptional();
    }

    /*static Optional<Property> getCompositeProperty(Ontology ontology, String pType, List<Property> properties) {
        String[] typesTree = pType.split("\\.");

        if (typesTree.length>1) {
            Optional<Property> rootProperty = getProperty(typesTree[0], properties);
            if (rootProperty.isPresent()) {
                String cType = rootProperty.get().getType();
                Optional<CompositeType> compositeType = getCompositeType(ontology, cType);
                if (compositeType.isPresent()) {
                    return getProperty(typesTree[1], compositeType.get().getProperties());
                }
            }
        }
        return Optional.empty();
    }*/

    public static List<Integer> getComplementaryTypes(Ontology ontology, EUntyped eUntyped) {
        return ontology.getEntityTypes().stream().map(et -> et.geteType()).filter(e -> !eUntyped.getNvTypes().contains(e)).collect(Collectors.toList());
    }

    public static Optional<RelationshipType> getRelationshipType(Ontology ontology, int relTypeId) {
        return getRelationshipType(ontology,getRelationTypeNameById(ontology,relTypeId));
    }

    public static Optional<RelationshipType> getRelationshipType(Ontology ontology, String name) {
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

    static Optional<CompositeType> getCompositeType(Ontology ontology, String cType) {
        Optional<CompositeType> CompositeTypeMatch = ontology.getCompositeTypes().stream()
                .filter(compositeTypeType-> compositeTypeType.getcType().equals(cType))
                .findFirst();
        CompositeType compositeType;
        if (CompositeTypeMatch.isPresent()) {
            compositeType = CompositeTypeMatch.get();
        }
        else {
            throw new IllegalArgumentException("Not Supported Composite Type: " + cType);
        }
        return Optional.of(compositeType);
    }

    public static Optional<PrimitiveType> getPrimitiveType(Ontology ontology, String pType){
        return ontology.getPrimitiveTypes().stream().
                            filter(primitiveType -> primitiveType.getType().equals(pType)).
                            findFirst();

    }

    public static Optional<EnumeratedType> getEnumeratedType(Ontology ontology, String type) {
        return ontology.getEnumeratedTypes().stream().filter(e -> e.geteType().equals(type)).findFirst();
    }
}
