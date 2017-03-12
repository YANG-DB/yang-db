package com.kayhut.fuse.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.ontology.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.*;
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
    private static Ontology ontologyObj = new Ontology();


    @Test
    public void testOntologySerialization() throws IOException, JSONException {
        String ontologyActualJSON = mapper.writeValueAsString(ontologyObj);
        String ontologyExpectedJSONString = "{\"ont\":\"Dragons\",\"entityTypes\":[{\"eType\":1,\"name\":\"Person\",\"properties\":[{\"pType\":1,\"name\":\"first name\",\"type\":\"string\",\"report\":[\"raw\"]},{\"pType\":2,\"name\":\"last name\",\"type\":\"string\",\"report\":[\"raw\"]},{\"pType\":3,\"name\":\"gender\",\"type\":\"TYPE_Gender\"},{\"pType\":4,\"name\":\"birth date\",\"type\":\"date\",\"report\":[\"raw\"]},{\"pType\":5,\"name\":\"death date\",\"type\":\"date\"},{\"pType\":6,\"name\":\"height\",\"type\":\"int\",\"units\":\"cm\",\"report\":[\"raw\"]}],\"display\":[\"%1 %2\",\"%4\",\"%6\"]},{\"eType\":2,\"name\":\"Dragon\",\"properties\":[{\"pType\":1,\"name\":\"name\",\"type\":\"string\",\"report\":[\"raw\"]}],\"display\":[\"name: %1\"]}],\"relationshipTypes\":[{\"rType\":1,\"name\":\"own\",\"directional\":true,\"ePairs\":[{\"eTypeA\":1,\"eTypeB\":2}],\"properties\":[{\"pType\":1,\"name\":\"since\",\"type\":\"date\",\"report\":[\"min\",\"max\"]},{\"pType\":2,\"name\":\"till\",\"type\":\"date\",\"report\":[\"min\",\"max\"]}]},{\"rType\":2,\"name\":\"fires at\",\"directional\":true,\"ePairs\":[{\"eTypeA\":2,\"eTypeB\":2}],\"properties\":[{\"pType\":1,\"name\":\"time\",\"type\":\"datetime\",\"report\":[\"min\",\"max\"]}]}],\"enumeratedTypes\":[{\"eType\":\"TYPE_Gender\",\"values\":[{\"val\":1,\"name\":\"Female\"},{\"val\":2,\"name\":\"Male\"},{\"val\":3,\"name\":\"Other\"}]}]}";
        System.out.println("ontologyExpectedJSONString:" + ontologyExpectedJSONString);
        System.out.println("ontologyActualJSON:" + ontologyActualJSON);
        JSONAssert.assertEquals(ontologyExpectedJSONString, ontologyActualJSON, false);
    }

    @Test
    public void testOntologyDeSerialization() throws Exception {
        String ontologyExpectedJson = readJsonToString("Dragons_Ontology_Short.json");
        Ontology resultObj = new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class);
        Assert.assertNotNull(resultObj);
        String ontologyActualJSON = mapper.writeValueAsString(resultObj);
        JSONAssert.assertEquals(ontologyExpectedJson, ontologyActualJSON, false);
    }

    private static void createResults1() {
        ontologyObj.setOnt("Dragons");
        List<EntityType> entityTypes = new ArrayList<EntityType>();

        //region EntityType1
        EntityType entityType1 = new EntityType();
        entityType1.seteType(1);
        entityType1.setName("Person");
        List<Property> entityType1Properties = new ArrayList<Property>();

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

        //region EntityType2
        EntityType entityType2 = new EntityType();
        entityType2.seteType(2);
        entityType2.setName("Dragon");
        List<Property> entityType2Properties = new ArrayList<Property>();

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

        ontologyObj.setEntityTypes(entityTypes);

        //region relationshipTypes
        List<RelationshipType> relationshipTypes = new ArrayList<RelationshipType>();

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
        List<EnumeratedType> enumeratedTypes = new ArrayList<EnumeratedType>();

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

        ontologyObj.setRelationshipTypes(relationshipTypes);
        ontologyObj.setEnumeratedTypes(enumeratedTypes);

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
        createResults1();

    }
}
