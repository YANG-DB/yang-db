package com.kayhut.fuse.model.logical;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class LogicalTransformerTest {

    private ObjectMapper mapper = new ObjectMapper();


    @Test
    public void testLogicalGraphSerialization() throws Exception {
        final LogicalGraphModel graphModel = mapper.readValue(readJsonToString("logical_graph.json"), LogicalGraphModel.class);
        assertEquals(2,graphModel.getEdges().size());
        assertEquals("100",graphModel.getEdges().get(0).getId());
        assertEquals("101",graphModel.getEdges().get(1).getId());
        assertEquals(2,graphModel.getEdges().get(0).getMetadata().getProperties().size());
        assertEquals(2,graphModel.getEdges().get(0).getProperties().getProperties().size());
        assertEquals(2,graphModel.getEdges().get(1).getMetadata().getProperties().size());
        assertEquals(5,graphModel.getEdges().get(1).getProperties().getProperties().size());


        assertEquals(2,graphModel.getNodes().size());
        assertEquals("0",graphModel.getNodes().get(0).getId());
        assertEquals("10",graphModel.getNodes().get(1).getId());
        assertEquals(3,graphModel.getNodes().get(0).getMetadata().getProperties().size());
        assertEquals(6,graphModel.getNodes().get(0).getProperties().getProperties().size());
        assertEquals(2,graphModel.getNodes().get(1).getMetadata().getProperties().size());
        assertEquals(6,graphModel.getNodes().get(1).getProperties().getProperties().size());
    }


    private String readJsonToString(String jsonFileName) throws Exception {
        String result = "";
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream("logical/" + jsonFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
