package com.kayhut.fuse.assembly.knowledge.domain;

import com.kayhut.fuse.model.ontology.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.OntologyTestUtils.Gender.TYPE_GENDER;
import static com.kayhut.fuse.model.ontology.Property.Builder.get;


public class KnowlegdeOntology {

    public static final String DATE_TYPE = "date";
    public static final String INT = "int";
    public static final String STRING = "string";
    public static final String FLOAT = "float";
    public static final String CM = "cm";

//entity
    public static Property id = new Property("id", STRING, "id");
    public static Property logicalId = new Property("logicalId", STRING, "logicalId");
    public static Property context = new Property("context", STRING, "context");
    public static Property category = new Property("category", TYPE_GENDER, "category");
    public static Property creationTime = new Property("creationTime", DATE_TYPE, "creationTime");
    public static Property lastUpdateTime = new Property("lastUpdateTime", DATE_TYPE, "lastUpdateTime");
    public static Property deleteUpdateTime = new Property("deleteUpdateTime", DATE_TYPE, "deleteUpdateTime");
    public static Property creationUser = new Property("creationUser", STRING, "creationUser");
    public static Property lastUpdateUser = new Property("lastUpdateUser", STRING, "lastUpdateUser");

// entityValue
    public static Property fieldId = new Property("fieldId", STRING, "fieldId");
    public static Property bdt = new Property("bdt", STRING, "bdt");
    public static Property stringValue = new Property("stringValue", STRING, "stringValue");
    public static Property intValue = new Property("intValue", INT, "intValue");
    public static Property dateValue = new Property("dateValue", DATE_TYPE, "dateValue");
    public static Property floatValue = new Property("floatValue", FLOAT, "floatValue");

// relation
    public static Property relationId = new Property("relationId", STRING, "relationId");
    public static Property entityAId = new Property("entityAId", STRING, "entityAId");
    public static Property entityBId = new Property("entityBId", STRING, "entityBId");
    public static Property entityACategory = new Property("entityACategory", STRING, "entityACategory");
    public static Property entityBCategory = new Property("entityBCategory", STRING, "entityBCategory");
    public static Property direction = new Property("direction", STRING, "direction");

// Reference
    public static Property url = new Property("url", STRING, "url");
    public static Property content = new Property("value", STRING, "value");
    public static Property system = new Property("system", STRING, "system");


    public static List<Property> properties = Arrays.asList(
            logicalId,category,content,creationTime,context,lastUpdateTime,deleteUpdateTime,
            creationUser,lastUpdateUser,fieldId,bdt,stringValue,intValue,dateValue,floatValue,
            relationId,entityAId,entityACategory,entityBId,entityBCategory,direction,
            url,system);

    /**
     {
         "entityAId": "e001.context1",
         "entityACategory": "person",
         "entityBId": "e000.context1",
         "entityBCategory": "car",
         "context": "context1",
         "category": "owns",
         "creationTime": "2017-01-01 00:00:00.000",
         "lastUpdateTime": "2017-01-01 00:00:00.000",
         "creationUser": "user1",
         "lastUpdateUser": "user2",
         "deleteTime": "2017-01-01 00:00:00.000",
         "authorization": ["authType1String1.authType2String1", "authType1String2.authType2String2"],
         "authorizationCount": 2
     }
     */
    public static final RelationshipType HAS = new RelationshipType("has", "has", true)
            .withProperty(id.type,entityAId.type,entityACategory.type,entityBId.type,entityBCategory.type,
                    context.type,category.type,creationTime.type,lastUpdateTime.type,
                    creationUser.type,lastUpdateUser.type,deleteUpdateTime.type);

    public static final RelationshipType HAS_INSIGHT = new RelationshipType("hasInsight", "hasInsight", true)
            .withProperty(id.type,entityAId.type,entityACategory.type,entityBId.type,entityBCategory.type,
                    context.type,category.type,creationTime.type,lastUpdateTime.type,
                    creationUser.type,lastUpdateUser.type,deleteUpdateTime.type);


    public static List<RelationshipType> relationships = Collections.singletonList(HAS);

    public static class Property {
        public String name;
        public boolean redundant;
        public String className;
        public String type;

        public Property(String name, String className, String type) {
            this(name, className, type, false);
        }

        public Property(String name, String className, String type, boolean redundant) {
            this.name = name;
            this.className = className;
            this.type = type;
            this.redundant = redundant;
        }

    }

    /**
     {
         "logicalId": "e000",
         "context": "context1",
         "category": "car",
         "creationTime": "2017-01-01 00:00:00.000",
         "lastUpdateTime": "2017-01-01 00:00:00.000",
         "creationUser": "user1",
         "lastUpdateUser": "user2",
         "deleteTime": "2017-01-01 00:00:00.000",
     }
     */
    public static class ENTITY {
        public static String name = "ENTITY";
        public static String type = "ENTITY";
        public static List<Property> propertyList = Arrays.asList(id, logicalId, category, context,
                creationTime, lastUpdateTime, creationUser, lastUpdateUser, deleteUpdateTime);

        public static List<RelationshipType> relationshipList = Arrays.asList(
                HAS.addPair(new EPair(type, ENTITY_VALUE.type)),
                HAS.addPair(new EPair(type, INSIGHT.type)),
                HAS.addPair(new EPair(type, REFERENCE.type)));
    }

    /*
    {
        "logicalId": "e000",
        "context": "context1",
        "fieldId": "name",
        "bdt": "name",
        "stringValue": "Shimon", (or intValue or floatValue or dateValue),
        "refs": [ "ref00000", "ref00001" ]
        "creationTime": "2017-01-01 00:00:00.000",
        "lastUpdateTime": "2017-01-01 00:00:00.000",
        "creationUser": "user1",
        "lastUpdateUser": "user2",
        "deleteTime": "2017-01-01 00:00:00.000",
        "authorization": ["authType1String1.authType2String1", "authType1String2.authType2String2"],
        "authorizationCount": 2
    }
     */
    public static class ENTITY_VALUE {
        public static String name = "ENTITY_VALUE";
        public static String type = "ENTITY_VALUE";
        public static List<Property> propertyList = Arrays.asList(id, logicalId, category, context,
                creationTime, lastUpdateTime, creationUser, lastUpdateUser, deleteUpdateTime,
                fieldId,bdt,stringValue,intValue,dateValue);

        public static List<RelationshipType> relationshipList =
                Collections.singletonList(HAS.addPair(new EPair(type, REFERENCE.type)));
    }

    public static class RELATION_VALUE {
        public static String name = "RELATION_VALUE";
        public static String type = "RELATION_VALUE";
        public static List<Property> propertyList = Arrays.asList(id, logicalId, category,
                creationTime, lastUpdateTime, creationUser, lastUpdateUser, deleteUpdateTime);

        public static List<RelationshipType> relationshipList =
                Collections.singletonList(HAS.addPair(new EPair(type, REFERENCE.type)));

    }

    public static class REFERENCE {
        public static String name = "REFERENCE";
        public static String type = "REFERENCE";
        public static List<Property> propertyList = Arrays.asList(id, logicalId, category,
                creationTime, lastUpdateTime, creationUser, lastUpdateUser, deleteUpdateTime);

    }

    public static class INSIGHT  {
        public static String name = "INSIGHT";
        public static String type = "INSIGHT";
        public static List<Property> propertyList = Arrays.asList(id, logicalId, category,
                creationTime, lastUpdateTime, creationUser, lastUpdateUser, deleteUpdateTime);

    }

    public static Ontology createOntology() {
        Ontology ontologyShortObj = new Ontology();
        ontologyShortObj.setOnt("Knowledge");


        //properties
        ontologyShortObj.setProperties(properties.stream().map(p->get().build(p.type,p.name,p.className)).collect(Collectors.toList()));

        //relationships
        ontologyShortObj.setRelationshipTypes(relationships);

        //entities
        ontologyShortObj.getEntityTypes().addAll(Arrays.asList(
                new EntityType(ENTITY.type, ENTITY.name, ENTITY.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(ENTITY_VALUE.type, ENTITY_VALUE.name, ENTITY_VALUE.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(INSIGHT.type, INSIGHT.name, INSIGHT.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList())),
                new EntityType(REFERENCE.type, REFERENCE.name, REFERENCE.propertyList.stream().map(p1 -> p1.type).collect(Collectors.toList()))));

        return OntologyFinalizer.finalize(ontologyShortObj);
    }

    public static Property getPropertyByName(List<Property> properties, String name) {
        return properties.stream().filter(p -> p.name.equals(name)).findFirst().get();
    }

    public static Property getPropertyByType(List<Property> properties, String type) {
        return properties.stream().filter(p -> p.type.equals(type)).findFirst().get();
    }
}
