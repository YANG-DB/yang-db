package com.kayhut.fuse.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.ontology.*;
import javaslang.collection.Stream;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.*;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by benishue on 22-Feb-17.
 */
public class OntologyTest {

    private ObjectMapper mapper = new ObjectMapper();
    private static Ontology ontologyShortObj = new Ontology();
    private static Ontology ontologyWithCompositeObj = new Ontology();

    public static void main(String[] args) throws JsonProcessingException {
        Ontology ontology = OntologyTestUtils.createDragonsOntologyLong();
        String json = new ObjectMapper().writeValueAsString(ontology);
        System.out.println(json);
    }

    @Test
    public void a() {
        int numIterations = 50000;

        for(int i = 0; i < numIterations ; i++) {
            int a = Integer.parseInt(Integer.toString(i));
        }

        int a = 0;
        int b = 0;

        long start = System.currentTimeMillis();
        for(int i = 0; i < numIterations ; i++) {
            b += 1;
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.println("empty loop elapsed: " + elapsed);

        start = System.currentTimeMillis();
        for(int i = 0; i < numIterations ; i++) {
            a = Integer.parseInt(Integer.toString(i));
        }
        elapsed = System.currentTimeMillis() - start;

        System.out.println("parsing loop elapsed: " + elapsed);
        System.out.println(a);
        System.out.println(b);
    }

    @Test
    @Ignore
    public void testShortOntologySerialization() throws IOException, JSONException {
        String ontologyActualJSON = mapper.writeValueAsString(ontologyShortObj);
        String ontologyExpectedJSONString = "{\n" +
                "\t\"ont\": \"Dragons\",\n" +
                "\t\"enumeratedTypes\": [{\n" +
                "\t\t\"eType\": \"TYPE_Gender\",\n" +
                "\t\t\"values\": [{\n" +
                "\t\t\t\"val\": 1,\n" +
                "\t\t\t\"name\": \"Female\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"val\": 2,\n" +
                "\t\t\t\"name\": \"Male\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"val\": 3,\n" +
                "\t\t\t\"name\": \"Other\"\n" +
                "\t\t}]\n" +
                "\t}],\n" +
                "\t\"properties\": [{\n" +
                "\t\t\"pType\": 1,\n" +
                "\t\t\"name\": \"firstName\",\n" +
                "\t\t\"type\": \"string\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"pType\": 2,\n" +
                "\t\t\"name\": \"lastName\",\n" +
                "\t\t\"type\": \"string\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"pType\": 3,\n" +
                "\t\t\"name\": \"gender\",\n" +
                "\t\t\"type\": \"TYPE_Gender\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"pType\": 4,\n" +
                "\t\t\"name\": \"birthDate\",\n" +
                "\t\t\"type\": \"date\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"pType\": 5,\n" +
                "\t\t\"name\": \"deathDate\",\n" +
                "\t\t\"type\": \"date\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"pType\": 6,\n" +
                "\t\t\"name\": \"height\",\n" +
                "\t\t\"type\": \"int\",\n" +
                "\t\t\"units\": \"cm\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"pType\": 7,\n" +
                "\t\t\"name\": \"name\",\n" +
                "\t\t\"type\": \"string\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"pType\": 8,\n" +
                "\t\t\"name\": \"startDate\",\n" +
                "\t\t\"type\": \"date\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"pType\": 9,\n" +
                "\t\t\"name\": \"endDate\",\n" +
                "\t\t\"type\": \"date\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"pType\": 2147483646,\n" +
                "\t\t\"name\": \"type\",\n" +
                "\t\t\"type\": \"string\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"pType\": 2147483647,\n" +
                "\t\t\"name\": \"id\",\n" +
                "\t\t\"type\": \"string\"\n" +
                "\t}],\n" +
                "\t\"entityTypes\": [{\n" +
                "\t\t\"eType\": 1,\n" +
                "\t\t\"name\": \"Person\",\n" +
                "\t\t\"properties\": [1,\n" +
                "\t\t2,\n" +
                "\t\t3,\n" +
                "\t\t4,\n" +
                "\t\t5,\n" +
                "\t\t6]\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"eType\": 2,\n" +
                "\t\t\"name\": \"Dragon\",\n" +
                "\t\t\"properties\": [7]\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"eType\": 4,\n" +
                "\t\t\"name\": \"Guild\",\n" +
                "\t\t\"properties\": [7]\n" +
                "\t}],\n" +
                "\t\"relationshipTypes\": [{\n" +
                "\t\t\"rType\": 1,\n" +
                "\t\t\"name\": \"own\",\n" +
                "\t\t\"directional\": true,\n" +
                "\t\t\"ePairs\": [{\n" +
                "\t\t\t\"eTypeA\": 1,\n" +
                "\t\t\t\"eTypeB\": 2\n" +
                "\t\t}],\n" +
                "\t\t\"properties\": [8,\n" +
                "\t\t9]\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"rType\": 2,\n" +
                "\t\t\"name\": \"memberOf\",\n" +
                "\t\t\"directional\": true,\n" +
                "\t\t\"ePairs\": [{\n" +
                "\t\t\t\"eTypeA\": 1,\n" +
                "\t\t\t\"eTypeB\": 4\n" +
                "\t\t}],\n" +
                "\t\t\"properties\": [8,\n" +
                "\t\t9]\n" +
                "\t}]\n" +
                "}";
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
        ontologyShortObj = OntologyTestUtils.createDragonsOntologyShort();
    }
}
