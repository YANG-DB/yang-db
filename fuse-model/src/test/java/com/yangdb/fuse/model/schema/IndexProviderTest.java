package com.yangdb.fuse.model.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class IndexProviderTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testIndexProviderSerialization() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("schema/DragonsIndexProvider.conf");
        IndexProvider indexProvider = mapper.readValue(resource, IndexProvider.class);
        Assert.assertEquals(4,indexProvider.getEntities().size());
        Assert.assertEquals(9,indexProvider.getRelations().size());

        String expected = IOUtils.toString(resource);
        Assert.assertEquals(mapper.readTree(expected),mapper.readTree(mapper.writeValueAsString(indexProvider)));
    }

}
