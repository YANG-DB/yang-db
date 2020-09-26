package com.yangdb.fuse.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.ontology.Ontology;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by benishue on 22-Feb-17.
 */
public class OntologyTest {

    private ObjectMapper mapper = new ObjectMapper();
    private static Ontology ontologyShortObj = new Ontology();
    private static Ontology ontologyWithCompositeObj = new Ontology();


    @Test
    public void testShortOntologyDeSerialization() throws Exception {
        String ontologyExpectedJson = readJsonToString("Dragons_Ontology_Short.json");
        Ontology resultObj = new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class);
        Assert.assertNotNull(resultObj);
        String ontologyActualJSON = mapper.writeValueAsString(resultObj);
        assertEquals(mapper.readTree(ontologyExpectedJson), mapper.readTree(ontologyActualJSON));

    }

    @Test
    public void testOntologyDeSerialization() throws Exception {
        String ontologyExpectedJson = readJsonToString("Dragons_Ontology.json");
        Ontology resultObj = new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class);
        Assert.assertNotNull(resultObj);
        assertEquals(resultObj.getEntityTypes().size(),5);
        assertEquals(resultObj.getRelationshipTypes().size(),7);
        assertEquals(resultObj.getProperties().size(),13);
        assertEquals(resultObj.getPrimitiveTypes().size(),8);
        assertEquals(resultObj.getEnumeratedTypes().size(),2);
        assertEquals(resultObj.getProperties().stream().flatMap(p -> p.getSearchType().stream()).count(),0);
    }

    @Test
    public void testOntologyWithSearchTypeDeSerialization() throws Exception {
        String ontologyExpectedJson = readJsonToString("Dragons_Ontology_search_type.json");
        Ontology resultObj = new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class);
        Assert.assertNotNull(resultObj);
        assertEquals(resultObj.getEntityTypes().size(),5);
        assertEquals(resultObj.getRelationshipTypes().size(),7);
        assertEquals(resultObj.getProperties().size(),13);
        assertEquals(resultObj.getPrimitiveTypes().size(),8);
        assertEquals(resultObj.getEnumeratedTypes().size(),2);
        assertEquals(resultObj.getProperties().stream().flatMap(p -> p.getSearchType().stream()).count(),9);
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
