package com.yangdb.fuse.model.results;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class AssignmentsDescritorTest {
    ObjectMapper mapper =  new ObjectMapper();

    @Test
    public void testDeSerialization() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ResultsJsons/findPathResults.json");
        Assignment<Entity,Relationship>[] resultObj = mapper.readValue(stream, new TypeReference<Assignment<Entity,Relationship>[]>(){});
        Assert.assertNotNull(resultObj);

        List<Assignment> assignments = Arrays.asList(resultObj);
        Assert.assertEquals("[e00000143,source, Entity]-(relatedEntitye00000143e00000145, relatedEntity)-[e00000145,end, Entity]-[e00000145,end, Entity]-(relatedEntitye00000145e00000144, relatedEntity)-[e00000144,target, Entity]",
                AssignmentDescriptor.print(assignments.get(0)));
        Assert.assertEquals("[e00000143,source, Entity]-(relatedEntitye00000143e00000081, relatedEntity)-[e00000143,, ???]-[e00000081,, ???]-(relatedEntitye00000081e00000145, relatedEntity)-[e00000145,end, Entity]-[e00000145,end, Entity]-(relatedEntitye00000145e00000144, relatedEntity)-[e00000144,target, Entity]",
                AssignmentDescriptor.print(assignments.get(1)));
    }



}
