package com.yangdb.fuse.model.logical;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.results.Property;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.Assert.assertEquals;


public class LogicalTransformerTest {

    private ObjectMapper mapper = new ObjectMapper();


    @Test
    public void testLogicalGraphSerialization() throws Exception {
        LogicalGraphModel model = new LogicalGraphModel();
        model.with(new LogicalNode("p1","Person")
                .withMetadata(Collections.singleton(new Property("user-defined","context")))
                .withProperty("first name","first name")
                .withProperty("last name","last name")
                .withProperty("born","28/10/1999")
                .withProperty("age","22")
                .withProperty("email","notMyName@yangdb.com")
                .withProperty("address","{\n" +
                        "            \"state\": \"not my state\",\n" +
                        "            \"street\": \"not my street\",\n" +
                        "            \"city\": \"not my city\",\n" +
                        "            \"zip\": \"not my gZip\"\n" +
                        "          }\n")
        );
        model.with(new LogicalNode("p2","Person")
                .withMetadata(Collections.singleton(new Property("user-defined","context")))
                .withProperty("first name","his first name")
                .withProperty("last name","his last name")
                .withProperty("born","28/10/1999")
                .withProperty("age","22")
                .withProperty("email","notHisName@yangdb.com")
                .withProperty("address","{\n" +
                        "            \"state\": \"not his state\",\n" +
                        "            \"street\": \"not his street\",\n" +
                        "            \"city\": \"not his city\",\n" +
                        "            \"zip\": \"not his gZip\"\n" +
                        "          }\n")
        );
        model.with(new LogicalEdge("know","know","p1","p2",true)
                .withMetadata(Collections.singleton(new Property("user-defined","context")))
                .withProperty("since","01/01/2000")
                .withProperty("medium","facebook")
        );
        model.with(new LogicalEdge("call","call","p2","p1",true)
                .withMetadata(Collections.singleton(new Property("user-defined","context")))
                .withProperty("time","01/01/2000")
                .withProperty("medium", "cellular")
                .withProperty("sourceLocation","40.06,-71.34")
                .withProperty("sourceTarget", "41.12,-70.9")
        );

        String path = this.getClass().getClassLoader().getResource("logical/").getPath();
        File file = new File(path + "/" + "logical_graph.json");
        mapper.writeValue(file, model);

        Assert.assertTrue(Files.exists(file.toPath()));
    }

    @Test
    public void testLogicalGraphDeSerialization() throws Exception {
        final LogicalGraphModel graphModel = mapper.readValue(readJsonToString("logical_graph.json"), LogicalGraphModel.class);
        assertEquals(2,graphModel.getEdges().size());
        assertEquals("know",graphModel.getEdges().get(0).getId());
        assertEquals("call",graphModel.getEdges().get(1).getId());
        assertEquals(1,graphModel.getEdges().get(0).getMetadata().getProperties().size());
        assertEquals(2,graphModel.getEdges().get(0).getProperties().getProperties().size());
        assertEquals(1,graphModel.getEdges().get(1).getMetadata().getProperties().size());
        assertEquals(4,graphModel.getEdges().get(1).getProperties().getProperties().size());


        assertEquals(2,graphModel.getNodes().size());
        assertEquals("p1",graphModel.getNodes().get(0).getId());
        assertEquals("p2",graphModel.getNodes().get(1).getId());
        assertEquals(1,graphModel.getNodes().get(0).metadata().size());
        assertEquals(6,graphModel.getNodes().get(0).getProperties().getProperties().size());
        assertEquals(1,graphModel.getNodes().get(1).metadata().size());
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
