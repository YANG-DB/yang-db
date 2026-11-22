package com.yangdb.fuse.graphql.cursor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory.Binding;
import com.yangdb.fuse.dispatcher.cursor.CreateCursorRequestDeserializer;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreatePathsCursorRequest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * The graphQL cursor request deserializes through the cursor bindings, like the core ones (moved from fuse-core's
 * CreateCursorRequestDeserializerTest with the class).
 */
public class CreateGraphQLCursorRequestDeserializerTest {
    @Test
    public void testCreateGraphCursorQLRequest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CreateCursorRequest.class, new CreateCursorRequestDeserializer(Arrays.asList(
                new Binding(CreatePathsCursorRequest.CursorType, CreatePathsCursorRequest.class, null),
                new Binding(CreateGraphQLCursorRequest.CursorType, CreateGraphQLCursorRequest.class, null))));
        objectMapper.registerModule(module);

        CreateCursorRequest createCursorRequest = objectMapper.readValue(
                objectMapper.writeValueAsString(new CreateGraphQLCursorRequest()),
                CreateCursorRequest.class);

        Assert.assertEquals(CreateGraphQLCursorRequest.CursorType, createCursorRequest.getCursorType());
        Assert.assertEquals(CreateGraphQLCursorRequest.class, createCursorRequest.getClass());
    }
}
