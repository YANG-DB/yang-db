package com.kayhut.fuse.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.ontology.Ontology;
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
public class QueryOntologyTest {

    private ObjectMapper mapper = new ObjectMapper();
    private static Ontology ontology = new Ontology();


    @Test
    @Ignore
    public void testResults1Serialization() throws IOException, JSONException {
        String ontologyActualJSON = mapper.writeValueAsString(ontology);
        String ontologyExpectedJSONString = "{\"ont\":\"Dragons\",\"entityTypes\":[{\"eType\":1,\"name\":\"Person\",\"properties\":[{\"pType\":1,\"name\":\"first name\",\"type\":\"string\",\"report\":[\"raw\"]},{\"pType\":2,\"name\":\"last name\",\"type\":\"string\",\"report\":[\"raw\"]},{\"pType\":3,\"name\":\"gender\",\"type\":\"TYPE_Gender\"},{\"pType\":4,\"name\":\"birth date\",\"type\":\"date\",\"report\":[\"raw\"]},{\"pType\":5,\"name\":\"death date\",\"type\":\"date\"},{\"pType\":6,\"height\":\"height\",\"type\":\"int\",\"units\":\"cm\",\"report\":[\"raw\"]}],\"display\":[\"%1 %2\",\"%4\",\"%6\"]},{\"eType\":2,\"name\":\"Dragon\",\"properties\":[{\"pType\":1,\"name\":\"name\",\"type\":\"string\",\"report\":[\"raw\"]}],\"display\":[\"name: %1\"]},{\"eType\":3,\"name\":\"Horse\",\"properties\":[{\"pType\":1,\"name\":\"name\",\"type\":\"string\",\"report\":[\"raw\"]},{\"pType\":2,\"name\":\"color\",\"type\":\"TYPE_HorseColor\",\"report\":[\"raw\"]},{\"pType\":3,\"height\":\"weight\",\"type\":\"int\",\"units\":\"Kg\",\"report\":[\"raw\"]}],\"display\":[\"%1\",\"%2\",\"%3\"]},{\"eType\":4,\"name\":\"Guild\",\"properties\":[{\"pType\":1,\"name\":\"name\",\"type\":\"string\",\"report\":[\"raw\"]}]},{\"eType\":5,\"name\":\"Kingdom\",\"properties\":[{\"pType\":1,\"name\":\"name\",\"type\":\"string\",\"report\":[\"raw\"]}],\"display\":[\"%1\"]}],\"relationshipTypes\":[{\"rType\":1,\"name\":\"own\",\"directional\":true,\"ePairs\":[{\"eTypeA\":1,\"eTypeB\":2},{\"eTypeA\":1,\"eTypeB\":3}],\"properties\":[{\"pType\":1,\"name\":\"since\",\"type\":\"date\",\"report\":[\"min\",\"max\"]},{\"pType\":2,\"name\":\"till\",\"type\":\"date\",\"report\":[\"min\",\"max\"]}]},{\"rType\":2,\"name\":\"fires at\",\"directional\":true,\"ePairs\":[{\"eTypeA\":2,\"eTypeB\":2}],\"properties\":[{\"pType\":1,\"name\":\"time\",\"type\":\"datetime\",\"report\":[\"min\",\"max\"]}]},{\"rType\":3,\"name\":\"freezes\",\"directional\":true,\"ePairs\":[{\"eTypeA\":2,\"eTypeB\":2}],\"properties\":[{\"pType\":1,\"name\":\"time\",\"type\":\"datetime\",\"report\":[\"min\",\"max\"]},{\"pType\":2,\"name\":\"duration\",\"type\":\"int\",\"units\":\"min\",\"report\":[\"avg\",\"min,\",\"max\"]}]},{\"rType\":4,\"name\":\"offspring of\",\"directional\":true,\"ePairs\":[{\"eTypeA\":1,\"eTypeB\":1}]},{\"rType\":5,\"name\":\"knows\",\"directional\":false,\"ePairs\":[{\"eTypeA\":1,\"eTypeB\":1}],\"properties\":[{\"pType\":1,\"name\":\"since\",\"type\":\"date\",\"report\":[\"raw\"]}]},{\"rType\":6,\"name\":\"member of\",\"directional\":true,\"ePairs\":[{\"eTypeA\":1,\"eTypeB\":4}],\"properties\":[{\"pType\":1,\"name\":\"since\",\"type\":\"date\",\"report\":[\"raw\"]},{\"pType\":2,\"name\":\"till\",\"type\":\"date\",\"report\":[\"raw\"]}]},{\"rType\":7,\"name\":\"subject of\",\"directional\":true,\"ePairs\":[{\"eTypeA\":1,\"eTypeB\":5}]},{\"rType\":8,\"name\":\"registered in\",\"directional\":true,\"ePairs\":[{\"eTypeA\":4,\"eTypeB\":5}]},{\"rType\":9,\"name\":\"originated in\",\"directional\":true,\"ePairs\":[{\"eTypeA\":2,\"eTypeB\":5},{\"eTypeA\":3,\"eTypeB\":5}]}],\"enumeratedTypes\":[{\"eType\":\"TYPE_Gender\",\"values\":[{\"val\":1,\"name\":\"Female\"},{\"val\":2,\"name\":\"Male\"},{\"val\":3,\"name\":\"Other\"}]},{\"eType\":\"TYPE_HorseColor\",\"values\":[{\"val\":1,\"name\":\"Black\"},{\"val\":2,\"name\":\"White\"},{\"val\":3,\"name\":\"Brown\"}]}]}";
        System.out.println("ontologyExpectedJSONString:" + ontologyExpectedJSONString);
        System.out.println("ontologyActualJSON:" + ontologyActualJSON);
        JSONAssert.assertEquals(ontologyExpectedJSONString, ontologyActualJSON,false);
    }


    @Test
    @Ignore
    public void testDeSerialization() throws Exception {
        String ontologyExpectedJson = readJsonToString("Dragons_Ontology.json");
        Ontology resultObj = new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class);
        Assert.assertNotNull(resultObj);
        String ontologyActualJSON = mapper.writeValueAsString(resultObj);
        JSONAssert.assertEquals(ontologyExpectedJson, ontologyActualJSON,false);
    }

    private static void createResults1()
    {
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
