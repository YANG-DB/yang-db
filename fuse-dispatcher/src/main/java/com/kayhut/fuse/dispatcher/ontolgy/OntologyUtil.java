package com.kayhut.fuse.dispatcher.ontolgy;

import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import jdk.nashorn.internal.ir.ReturnNode;

import java.util.Optional;

/**
 * Created by benishue on 12-Mar-17.
 */
public class OntologyUtil {

    public static String getEntityTypeNameById(Ontology ontology, int eTypeId){
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
}
