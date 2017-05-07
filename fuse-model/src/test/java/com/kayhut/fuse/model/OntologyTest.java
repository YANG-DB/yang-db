package com.kayhut.fuse.model;

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


    @Test
    public void testShortOntologySerialization() throws IOException, JSONException {
        String ontologyActualJSON = mapper.writeValueAsString(ontologyShortObj);
        String ontologyExpectedJSONString = "{\"ont\":\"Dragons\",\"enumeratedTypes\":[{\"eType\":\"TYPE_Gender\",\"values\":[{\"val\":1,\"name\":\"Female\"},{\"val\":2,\"name\":\"Male\"},{\"val\":3,\"name\":\"Other\"}]}],\"properties\":[{\"pType\":1,\"name\":\"firstName\",\"type\":\"string\"},{\"pType\":2,\"name\":\"lastName\",\"type\":\"string\"},{\"pType\":3,\"name\":\"gender\",\"type\":\"TYPE_Gender\"},{\"pType\":4,\"name\":\"birthDate\",\"type\":\"date\"},{\"pType\":5,\"name\":\"deathDate\",\"type\":\"date\"},{\"pType\":6,\"name\":\"height\",\"type\":\"int\",\"units\":\"cm\"},{\"pType\":7,\"name\":\"name\",\"type\":\"string\"},{\"pType\":8,\"name\":\"startDate\",\"type\":\"date\"},{\"pType\":9,\"name\":\"endDate\",\"type\":\"date\"}],\"entityTypes\":[{\"eType\":1,\"name\":\"Person\",\"properties\":[1,2,3,4,5,6]},{\"eType\":2,\"name\":\"Dragon\",\"properties\":[7]},{\"eType\":4,\"name\":\"Guild\",\"properties\":[7]}],\"relationshipTypes\":[{\"rType\":1,\"name\":\"own\",\"directional\":true,\"ePairs\":[{\"eTypeA\":1,\"eTypeB\":2}],\"properties\":[8,9]},{\"rType\":2,\"name\":\"memberOf\",\"directional\":true,\"ePairs\":[{\"eTypeA\":1,\"eTypeB\":4}],\"properties\":[8,9]}]}";
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
