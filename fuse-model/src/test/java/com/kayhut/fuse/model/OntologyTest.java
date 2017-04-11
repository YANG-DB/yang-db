package com.kayhut.fuse.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.ontology.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by benishue on 22-Feb-17.
 */
public class OntologyTest {

    private ObjectMapper mapper = new ObjectMapper();
    private static Ontology ontologyShortObj = new Ontology();
    private static Ontology ontologyWithCompositeObj = new Ontology();


    @Test
    public void testShortOntologySerialization() throws IOException, JSONException {
        String ontologyActualJSON = mapper.writeValueAsString(ontologyShortObj);
        String ontologyExpectedJSONString = "{\"ont\":\"Dragons\",\"entityTypes\":[{\"eType\":1,\"name\":\"Person\",\"properties\":[{\"pType\":1,\"name\":\"first name\",\"type\":\"string\",\"report\":[\"raw\"]},{\"pType\":2,\"name\":\"last name\",\"type\":\"string\",\"report\":[\"raw\"]},{\"pType\":3,\"name\":\"gender\",\"type\":\"TYPE_Gender\"},{\"pType\":4,\"name\":\"birth date\",\"type\":\"date\",\"report\":[\"raw\"]},{\"pType\":5,\"name\":\"death date\",\"type\":\"date\"},{\"pType\":6,\"name\":\"height\",\"type\":\"int\",\"units\":\"cm\",\"report\":[\"raw\"]}],\"display\":[\"%1 %2\",\"%4\",\"%6\"]},{\"eType\":2,\"name\":\"Dragon\",\"properties\":[{\"pType\":1,\"name\":\"name\",\"type\":\"string\",\"report\":[\"raw\"]}],\"display\":[\"name: %1\"]}],\"relationshipTypes\":[{\"rType\":1,\"name\":\"own\",\"directional\":true,\"ePairs\":[{\"eTypeA\":1,\"eTypeB\":2}],\"properties\":[{\"pType\":1,\"name\":\"since\",\"type\":\"date\",\"report\":[\"min\",\"max\"]},{\"pType\":2,\"name\":\"till\",\"type\":\"date\",\"report\":[\"min\",\"max\"]}]},{\"rType\":2,\"name\":\"fires at\",\"directional\":true,\"ePairs\":[{\"eTypeA\":2,\"eTypeB\":2}],\"properties\":[{\"pType\":1,\"name\":\"time\",\"type\":\"datetime\",\"report\":[\"min\",\"max\"]}]}],\"enumeratedTypes\":[{\"eType\":\"TYPE_Gender\",\"values\":[{\"val\":1,\"name\":\"Female\"},{\"val\":2,\"name\":\"Male\"},{\"val\":3,\"name\":\"Other\"}]}]}";
        System.out.println("ontologyExpectedJSONString:" + ontologyExpectedJSONString);
        System.out.println("ontologyActualJSON:" + ontologyActualJSON);
        JSONAssert.assertEquals(ontologyExpectedJSONString, ontologyActualJSON, false);
    }

    @Test
    public void testShortOntologyDeSerialization() throws Exception {
        String ontologyExpectedJson = readJsonToString("Dragons_Ontology_Short.json");
        Ontology resultObj = new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class);
        Assert.assertNotNull(resultObj);
        String ontologyActualJSON = mapper.writeValueAsString(resultObj);
        JSONAssert.assertEquals(ontologyExpectedJson, ontologyActualJSON, false);
    }

    @Test
    public void testOntologyWithCompositeSerialization() throws IOException, JSONException {
        String ontologyActualJSON = mapper.writeValueAsString(ontologyWithCompositeObj);
        String ontologyExpectedJSONString = "{\"ont\":\"Dragons\",\"enumeratedTypes\":[{\"eType\":\"TYPE_Gender\",\"values\":[{\"val\":1,\"name\":\"Female\"},{\"val\":2,\"name\":\"Male\"},{\"val\":3,\"name\":\"Other\"}]}],\"compositeTypes\":[{\"cType\":\"datespan\",\"properties\":[{\"pType\":1,\"type\":\"date\",\"DBpName\":\"since\",\"name\":\"since\"},{\"pType\":2,\"type\":\"date\",\"DBpName\":\"till\",\"name\":\"till\"}]},{\"cType\":\"personName\",\"properties\":[{\"pType\":1,\"type\":\"string\",\"DBpName\":\"first\",\"name\":\"first\"},{\"pType\":2,\"type\":\"string\",\"DBpName\":\"last\",\"name\":\"last\"}]}],\"entityTypes\":[{\"eType\":1,\"DBeName\":\"Person\",\"name\":\"Person\",\"properties\":[{\"pType\":1,\"type\":\"personName\",\"DBpName\":\"name\",\"name\":\"name\",\"report\":[\"raw(first)\",\"raw(last)\"]},{\"pType\":3,\"type\":\"TYPE_Gender\",\"DBpName\":\"gender\",\"name\":\"gender\"},{\"pType\":4,\"type\":\"date\",\"DBpName\":\"birth\",\"name\":\"birth date\",\"report\":[\"raw\"]},{\"pType\":5,\"type\":\"date\",\"DBpName\":\"death\",\"name\":\"death date\"},{\"pType\":6,\"type\":\"int\",\"DBpName\":\"height\",\"name\":\"height\",\"units\":\"cm\",\"report\":[\"raw\"]}],\"display\":[\"%1 %2\",\"%4\",\"%6\"]},{\"eType\":2,\"DBeName\":\"Dragon\",\"name\":\"Dragon\",\"properties\":[{\"pType\":1,\"type\":\"string\",\"DBpName\":\"name\",\"name\":\"name\",\"report\":[\"raw\"]}],\"display\":[\"name: %1\"]},{\"eType\":4,\"DBeName\":\"Guild\",\"name\":\"Guild\",\"properties\":[{\"pType\":1,\"type\":\"string\",\"DBpName\":\"name\",\"name\":\"name\",\"report\":[\"raw\"]}]}],\"relationshipTypes\":[{\"rType\":1,\"DBrName\":\"own\",\"name\":\"own\",\"directional\":true,\"ePairs\":[{\"eTypeA\":1,\"eTypeB\":2}],\"properties\":[{\"pType\":1,\"type\":\"datespan\",\"DBpName\":\"span\",\"name\":\"tf\",\"report\":[\"min(since)\",\"max(since)\",\"min(till)\",\"max(till)\"]}]},{\"rType\":6,\"DBrName\":\"member\",\"name\":\"member of\",\"directional\":true,\"ePairs\":[{\"eTypeA\":1,\"eTypeB\":4}],\"properties\":[{\"pType\":1,\"type\":\"datespan\",\"DBpName\":\"time\",\"name\":\"tf\",\"report\":[\"raw(since)\",\"raw(till)\"]}]}]}";
        System.out.println("ontologyExpectedJSONString:" + ontologyExpectedJSONString);
        System.out.println("ontologyActualJSON:" + ontologyActualJSON);
        JSONAssert.assertEquals(ontologyExpectedJSONString, ontologyActualJSON, false);
    }

    private static void createDragonsOntologyShort() {
        ontologyShortObj.setOnt("Dragons");
        List<EntityType> entityTypes = new ArrayList<>();

        //region EntityType1 = Person
        EntityType entityType1 = new EntityType();
        entityType1.seteType(1);
        entityType1.setName("Person");
        List<Property> entityType1Properties = new ArrayList<>();

        Property entityType1Prop1 = new Property() {{
            setpType(1);
            setName("first name");
            setType("string");
            setReport(Arrays.asList("raw"));
        }};

        Property entityType1Prop2 = new Property() {{
            setpType(2);
            setName("last name");
            setType("string");
            setReport(Arrays.asList("raw"));
        }};

        Property entityType1Prop3 = new Property() {{
            setpType(3);
            setName("gender");
            setType("TYPE_Gender");
        }};

        Property entityType1Prop4 = new Property() {{
            setpType(4);
            setName("birth date");
            setType("date");
            setReport(Arrays.asList("raw"));
        }};

        Property entityType1Prop5 = new Property() {{
            setpType(5);
            setName("death date");
            setType("date");
        }};

        Property entityType1Prop6 = new Property() {{
            setpType(6);
            setName("height");
            setType("int");
            setUnits("cm");
            setReport(Arrays.asList("raw"));
        }};

        entityType1Properties.addAll(Arrays.asList(entityType1Prop1, entityType1Prop2, entityType1Prop3, entityType1Prop4, entityType1Prop5, entityType1Prop6));
        entityType1.setProperties(entityType1Properties);
        entityType1.setDisplay(Arrays.asList("%1 %2", "%4", "%6"));
        entityTypes.add(entityType1);
        //endregion

        //region EntityType2 = Dragon
        EntityType entityType2 = new EntityType();
        entityType2.seteType(2);
        entityType2.setName("Dragon");
        List<Property> entityType2Properties = new ArrayList<>();

        Property entityType2Prop1 = new Property() {{
            setpType(1);
            setName("name");
            setType("string");
            setReport(Arrays.asList("raw"));
        }};

        entityType2Properties.addAll(Arrays.asList(entityType2Prop1));
        entityType2.setProperties(entityType2Properties);
        entityType2.setDisplay(Arrays.asList("name: %1"));
        entityTypes.add(entityType2);
        //endregion

        //region Future Testing
//        EntityType entityType3 = new EntityType();
//        entityType3.seteType(3);
//        entityType3.setName("Horse");
//        List<Property> entityType3Properties = new ArrayList<Property>();
//
//        Property entityType3Prop1 = new Property() {{
//            setpType(1);
//            setName("name");
//            setType("string");
//            setReport(Arrays.asList("raw"));
//        }};
//
//        Property entityType3Prop2 = new Property() {{
//            setpType(2);
//            setName("color");
//            setType("TYPE_HorseColor");
//            setReport(Arrays.asList("raw"));
//        }};
//
//        Property entityType3Prop3 = new Property() {{
//            setpType(3);
//            setName("weight");
//            setType("int");
//            setUnits("Kg");
//            setReport(Arrays.asList("raw"));
//        }};
//
//        entityType3Properties.addAll(Arrays.asList(entityType3Prop1, entityType3Prop2, entityType3Prop3));
//        entityType3.setProperties(entityType3Properties);
//        entityType3.setDisplay(Arrays.asList("%1", "%2", "%3"));
//        entityTypes.add(entityType3);
//
//        EntityType entityType4 = new EntityType();
//        entityType4.seteType(4);
//        entityType4.setName("Guild");
//        List<Property> entityType4Properties = new ArrayList<Property>();
//
//        Property entityType4Prop1 = new Property() {{
//            setpType(1);
//            setName("name");
//            setType("string");
//            setReport(Arrays.asList("raw"));
//        }};
//
//        entityType4Properties.addAll(Arrays.asList(entityType4Prop1));
//        entityType4.setProperties(entityType4Properties);
//        entityTypes.add(entityType4);
//
//        EntityType entityType5 = new EntityType();
//        entityType5.seteType(5);
//        entityType5.setName("Kingdom");
//        List<Property> entityType5Properties = new ArrayList<Property>();
//
//        Property entityType5Prop1 = new Property() {{
//            setpType(5);
//            setName("name");
//            setType("string");
//            setReport(Arrays.asList("raw"));
//        }};
//
//        entityType5Properties.addAll(Arrays.asList(entityType5Prop1));
//        entityType5.setProperties(entityType5Properties);
//        entityType5.setDisplay(Arrays.asList("%1"));
//        entityTypes.add(entityType5);
        //endregion

        ontologyShortObj.setEntityTypes(entityTypes);

        //region relationshipTypes
        List<RelationshipType> relationshipTypes = new ArrayList<>();

        RelationshipType relationshipType1 = new RelationshipType();
        relationshipType1.setrType(1);
        relationshipType1.setName("own");
        relationshipType1.setDirectional(true);
        relationshipType1.setePairs(Arrays.asList(new EPair() {{
            seteTypeA(1);
            seteTypeB(2);
        }}));
        relationshipType1.setProperties(Arrays.asList(new Property() {{
            setpType(1);
            setName("since");
            setType("date");
            setReport(Arrays.asList("min", "max"));
        }}, new Property() {{
            setpType(2);
            setName("till");
            setType("date");
            setReport(Arrays.asList("min", "max"));
        }}));

        relationshipTypes.add(relationshipType1);

        RelationshipType relationshipType2 = new RelationshipType();
        relationshipType2.setrType(2);
        relationshipType2.setName("fires at");
        relationshipType2.setDirectional(true);
        relationshipType2.setePairs(Arrays.asList(new EPair() {{
            seteTypeA(2);
            seteTypeB(2);
        }}));
        relationshipType2.setProperties(Arrays.asList(new Property() {{
            setpType(1);
            setName("time");
            setType("datetime");
            setReport(Arrays.asList("min", "max"));
        }}));

        relationshipTypes.add(relationshipType2);
        //endregion

        //region enumeratedTypes
        List<EnumeratedType> enumeratedTypes = new ArrayList<>();

        EnumeratedType enumeratedType1 = new EnumeratedType() {{
            seteType("TYPE_Gender");
            setValues(Arrays.asList(new Value() {{
                setName("Female");
                setVal(1);
            }}, new Value() {{
                setName("Male");
                setVal(2);
            }}, new Value() {{
                setName("Other");
                setVal(3);
            }}));
        }};

        enumeratedTypes.add(enumeratedType1);
        //endregion

        ontologyShortObj.setRelationshipTypes(relationshipTypes);
        ontologyShortObj.setEnumeratedTypes(enumeratedTypes);

    }

    private static void createDragonsOntologyWithComposite()
    {
        ontologyWithCompositeObj.setOnt("Dragons");
        List<EntityType> entityTypes = new ArrayList<>();

        //region Entity Type Person
        EntityType entityTypePerson = new EntityType();
        entityTypePerson.seteType(1);
        entityTypePerson.setName("Person");
        entityTypePerson.setDBeName("Person");
        List<Property> entityTypePersonProperties = new ArrayList<>();

        /*
        {
          "pType": 1,
          "type": "personName",
          "DBpName": "name",
          "name": "name",
          "report": [
            "raw(first)",
            "raw(last)"
          ]
        }
         */
        Property entityTypePersonProp1 = new Property();
        entityTypePersonProp1.setpType(1);
        entityTypePersonProp1.setType("personName");
        entityTypePersonProp1.setDBpName("name");
        entityTypePersonProp1.setName("name");
        entityTypePersonProp1.setReport(Arrays.asList("raw(first)","raw(last)"));


        /*
        {
          "pType": 3,
          "type": "TYPE_Gender",
          "DBpName": "gender",
          "name": "gender"
        }
         */

        Property entityTypePersonProp2 = new Property();
        entityTypePersonProp2.setpType(3);
        entityTypePersonProp2.setType("TYPE_Gender");
        entityTypePersonProp2.setDBpName("gender");
        entityTypePersonProp2.setName("gender");


        /*
        {
          "pType": 4,
          "type": "date",
          "DBpName": "birth",
          "name": "birth date",
          "report": [
            "raw"
          ]
         */
        Property entityTypePersonProp3 = new Property();
        entityTypePersonProp3.setpType(4);
        entityTypePersonProp3.setType("date");
        entityTypePersonProp3.setDBpName("birth");
        entityTypePersonProp3.setName("birth date");
        entityTypePersonProp3.setReport(Arrays.asList("raw"));

        /*
        {
          "pType": 5,
          "type": "date",
          "DBpName": "death",
          "name": "death date"
        }
         */

        Property entityTypePersonProp4 = new Property();
        entityTypePersonProp4.setpType(5);
        entityTypePersonProp4.setType("date");
        entityTypePersonProp4.setDBpName("death");
        entityTypePersonProp4.setName("death date");

        /*
        {
            "pType": 6,
                "type": "int",
                "DBpName": "height",
                "name": "height",
                "units": "cm",
                "report": [
            "raw"
          ]
        }
        */
        Property entityTypePersonProp5 = new Property();
        entityTypePersonProp5.setpType(6);
        entityTypePersonProp5.setType("int");
        entityTypePersonProp5.setDBpName("height");
        entityTypePersonProp5.setName("height");
        entityTypePersonProp5.setUnits("cm");
        entityTypePersonProp5.setReport(Arrays.asList("raw"));


        entityTypePersonProperties.addAll(Arrays.asList(entityTypePersonProp1,entityTypePersonProp2,entityTypePersonProp3,entityTypePersonProp4,entityTypePersonProp5));
        entityTypePerson.setProperties(entityTypePersonProperties);
        entityTypePerson.setDisplay(Arrays.asList("%1 %2", "%4", "%6"));

        //endregion

        entityTypes.add(entityTypePerson);


        //region EntityType Dragon
        EntityType entityTypeDragon = new EntityType();
        /*
        "eType": 2,
        "DBeName": "Dragon",
        "name": "Dragon",
         */
        entityTypeDragon.seteType(2);
        entityTypeDragon.setDBeName("Dragon");
        entityTypeDragon.setName("Dragon");
        List<Property> entityTypeDragonProperties = new ArrayList<>();

        /*
        {
          "pType": 1,
          "type": "string",
          "DBpName": "name",
          "name": "name",
          "report": [
            "raw"
          ]
        }
         */
        Property entityTypeDragonProp1 = new Property();
        entityTypeDragonProp1.setpType(1);
        entityTypeDragonProp1.setType("string");
        entityTypeDragonProp1.setDBpName("name");
        entityTypeDragonProp1.setName("name");
        entityTypeDragonProp1.setReport(Arrays.asList("raw"));


        entityTypeDragonProperties.addAll(Arrays.asList(entityTypeDragonProp1));
        entityTypeDragon.setProperties(entityTypeDragonProperties);
        entityTypeDragon.setDisplay(Arrays.asList("name: %1"));
        //endregion

        entityTypes.add(entityTypeDragon);


        //region EntityType Guild
        EntityType entityTypeGuild = new EntityType();
        /*
        "eType": 4,
        "DBeName": "Guild",
        "name": "Guild",
         */
        entityTypeGuild.seteType(4);
        entityTypeGuild.setDBeName("Guild");
        entityTypeGuild.setName("Guild");
        List<Property> entityTypeGuildProperties = new ArrayList<>();

        /*
        {
          "pType": 1,
          "type": "string",
          "DBpName": "name",
          "name": "name",
          "report": [
            "raw"
          ]
        }
         */
        Property entityTypeGuildProp1 = new Property();
        entityTypeGuildProp1.setpType(1);
        entityTypeGuildProp1.setType("string");
        entityTypeGuildProp1.setDBpName("name");
        entityTypeGuildProp1.setName("name");
        entityTypeGuildProp1.setReport(Arrays.asList("raw"));


        entityTypeGuildProperties.addAll(Arrays.asList(entityTypeGuildProp1));
        entityTypeGuild.setProperties(entityTypeGuildProperties);
        //endregion

        entityTypes.add(entityTypeGuild);



        ontologyWithCompositeObj.setEntityTypes(entityTypes);

        //region relationshipTypes
        List<RelationshipType> relationshipTypes = new ArrayList<>();

        /*
          "rType": 1,
          "DBrName": "own",
          "name": "own",
          "directional": true,
          "ePairs": [
            {
              "eTypeA": 1,
              "eTypeB": 2
            },
            {
              "eTypeA": 1,
              "eTypeB": 3
            }
          ]
         */
        RelationshipType relationshipTypeOwn = new RelationshipType();
        relationshipTypeOwn.setrType(1);
        relationshipTypeOwn.setName("own");
        relationshipTypeOwn.setDBrName("own");
        relationshipTypeOwn.setDirectional(true);
        relationshipTypeOwn.setePairs(Arrays.asList(new EPair() {{
            seteTypeA(1);
            seteTypeB(2);
        }}));

        List<Property> relationshipTypeOwnProperties = new ArrayList<>();
        Property relationshipTypeOwnProperty1 = new Property();

        /*
        {
          "pType": 1,
          "type": "datespan",
          "DBpName": "span",
          "name": "tf",
          "report": [
            "min(since)",
            "max(since)",
            "min(till)",
            "max(till)"
          ]
        }
         */
        relationshipTypeOwnProperty1.setpType(1);
        relationshipTypeOwnProperty1.setType("datespan");
        relationshipTypeOwnProperty1.setDBpName("span");
        relationshipTypeOwnProperty1.setName("tf");
        relationshipTypeOwnProperty1.setReport(Arrays.asList("min(since)", "max(since)","min(till)","max(till)"));

        relationshipTypeOwnProperties.add(relationshipTypeOwnProperty1);
        relationshipTypeOwn.setProperties(relationshipTypeOwnProperties);
        relationshipTypes.add(relationshipTypeOwn);

        RelationshipType relationshipTypeMember = new RelationshipType();
        /*
          "rType": 6,
          "DBrName": "member",
          "name": "member of",
          "directional": true,
         */
        relationshipTypeMember.setrType(6);
        relationshipTypeMember.setDBrName("member");
        relationshipTypeMember.setName("member of");
        relationshipTypeMember.setDirectional(true);
        relationshipTypeMember.setePairs(Arrays.asList(new EPair() {{
            seteTypeA(1);
            seteTypeB(4);
        }}));

        List<Property> relationshipTypeMemberProperties = new ArrayList<>();

        /*
        {
          "pType": 1,
          "type": "datespan",
          "DBpName": "time",
          "name": "tf",
          "report": [
            "raw(since)",
            "raw(till)"
          ]
        }
         */
        Property relationshipTypeMemberProperty1 = new  Property();
        relationshipTypeMemberProperty1.setpType(1);
        relationshipTypeMemberProperty1.setType("datespan");
        relationshipTypeMemberProperty1.setDBpName("time");
        relationshipTypeMemberProperty1.setName("tf");
        relationshipTypeMemberProperty1.setReport(Arrays.asList("raw(since)","raw(till)"));
        relationshipTypeMemberProperties.add(relationshipTypeMemberProperty1);
        relationshipTypeMember.setProperties(relationshipTypeMemberProperties);


        relationshipTypes.add(relationshipTypeMember);
        //endregion

        //region enumeratedTypes
        List<EnumeratedType> enumeratedTypes = new ArrayList<>();

        EnumeratedType enumeratedType1 = new EnumeratedType() {{
            seteType("TYPE_Gender");
            setValues(Arrays.asList(new Value() {{
                setName("Female");
                setVal(1);
            }}, new Value() {{
                setName("Male");
                setVal(2);
            }}, new Value() {{
                setName("Other");
                setVal(3);
            }}));
        }};

        enumeratedTypes.add(enumeratedType1);
        //endregion

        //region CompositeTypes

        List<CompositeType> compositeTypes = new ArrayList<>();

        CompositeType compositeTypePersonName = new CompositeType();
        compositeTypePersonName.setcType("personName");
        List<Property> compositeTypePersonNameProperties = new ArrayList<>();

        /*
        {
            "pType": 1,
            "type": "string",
            "DBpName": "first",
            "name": "first"
        }
        */

        Property propertyFirst = new Property();
        propertyFirst.setpType(1);
        propertyFirst.setType("string");
        propertyFirst.setDBpName("first");
        propertyFirst.setName("first");
        compositeTypePersonNameProperties.add(propertyFirst);

        /*
        {
            "pType": 2,
            "type": "string",
            "DBpName": "last",
            "name": "last"
        }
        */

        Property propertyLast = new Property();
        propertyLast.setpType(2);
        propertyLast.setType("string");
        propertyLast.setDBpName("last");
        propertyLast.setName("last");
        compositeTypePersonNameProperties.add(propertyLast);
        compositeTypePersonName.setProperties(compositeTypePersonNameProperties);

        compositeTypes.add(compositeTypePersonName);

        CompositeType compositeTypeDateSpan = new CompositeType();
        compositeTypeDateSpan.setcType("datespan");
        List<Property> compositeTypeDateSpanProperties = new ArrayList<>();

        /*
        {
          "pType": 1,
          "type": "date",
          "DBpName": "since",
          "name": "since"
        }
        */

        Property propertySince = new Property();
        propertySince.setpType(1);
        propertySince.setType("date");
        propertySince.setDBpName("since");
        propertySince.setName("since");
        compositeTypeDateSpanProperties.add(propertySince);

        /*
        {
          "pType": 2,
          "type": "date",
          "DBpName": "till",
          "name": "till"
        }
        */

        Property propertyTill = new Property();
        propertyTill.setpType(2);
        propertyTill.setType("date");
        propertyTill.setDBpName("till");
        propertyTill.setName("till");
        compositeTypeDateSpanProperties.add(propertyTill);
        compositeTypeDateSpan.setProperties(compositeTypeDateSpanProperties);

        compositeTypes.add(compositeTypeDateSpan);
        //endregion

        ontologyWithCompositeObj.setRelationshipTypes(relationshipTypes);
        ontologyWithCompositeObj.setEnumeratedTypes(enumeratedTypes);
        ontologyWithCompositeObj.setCompositeTypes(compositeTypes);

    }

    private String readJsonToString(String jsonFileName) throws Exception {
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream("OntologyJsons/" + jsonFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Before
    public void setup() {
    }

    @BeforeClass
    public static void setUpOnce() {
        createDragonsOntologyShort();
        createDragonsOntologyWithComposite();

    }
}
