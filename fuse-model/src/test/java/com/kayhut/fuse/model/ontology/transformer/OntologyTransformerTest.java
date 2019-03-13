package com.kayhut.fuse.model.ontology.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


public class OntologyTransformerTest {

    private ObjectMapper mapper = new ObjectMapper();


    @Test
    public void testOntologyTransformerSerialization() throws Exception {
        final OntologyTransformer transformer = mapper.readValue(readJsonToString("Knowledge_Transformation.json"), OntologyTransformer.class);
        assertEquals(1,transformer.getEntityTypes().size());
        assertEquals(1,transformer.getRelationTypes().size());
        assertEquals("Entity",transformer.getEntityTypes().get(0).geteType());
        assertEquals(1,transformer.getEntityTypes().get(0).getProperties().getKeys().size());
        assertEquals(4,transformer.getEntityTypes().get(0).getProperties().getValuePatterns().size());
        assertEquals(2,transformer.getEntityTypes().get(0).getMetadataProperties().size());
        assertEquals("Relation",transformer.getRelationTypes().get(0).getrType());
        assertEquals(1,transformer.getRelationTypes().get(0).getProperties().getKeys().size());
        assertEquals(4,transformer.getRelationTypes().get(0).getProperties().getValuePatterns().size());
        assertEquals(4,transformer.getRelationTypes().get(0).getMetadataProperties().size());
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

}
