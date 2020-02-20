package com.yangdb.fuse.dispatcher.cursor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory.Binding;
import com.yangdb.fuse.model.transport.cursor.*;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Roman on 7/7/2018.
 */
public class CreateCursorRequestDeserializerTest {
    @BeforeClass
    public static void setup() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CreateCursorRequest.class, new CreateCursorRequestDeserializer(
                Stream.of(
                        new Binding(CreatePathsCursorRequest.CursorType, CreatePathsCursorRequest.class, null),
                        new Binding(CreateGraphCursorRequest.CursorType, CreateGraphCursorRequest.class, null),
                        new Binding(CreateGraphQLCursorRequest.CursorType, CreateGraphQLCursorRequest.class, null),
                        new Binding(CreateGraphHierarchyCursorRequest.CursorType, CreateGraphHierarchyCursorRequest.class, null))
                .toJavaList()));
        objectMapper.registerModule(module);
    }

    @Test
    public void testCreatePathsCursorRequest() throws IOException {
        CreateCursorRequest createCursorRequest = objectMapper.readValue(
                objectMapper.writeValueAsString(new CreatePathsCursorRequest()),
                CreateCursorRequest.class);

        Assert.assertEquals(CreatePathsCursorRequest.CursorType, createCursorRequest.getCursorType());
        Assert.assertEquals(CreatePathsCursorRequest.class, createCursorRequest.getClass());
    }

    @Test
    public void testCreateGraphCursorRequest() throws IOException {
        CreateCursorRequest createCursorRequest = objectMapper.readValue(
                objectMapper.writeValueAsString(new CreateGraphCursorRequest()),
                CreateCursorRequest.class);

        Assert.assertEquals(CreateGraphCursorRequest.CursorType, createCursorRequest.getCursorType());
        Assert.assertEquals(CreateGraphCursorRequest.class, createCursorRequest.getClass());
    }

    @Test
    public void testCreateGraphCursorQLRequest() throws IOException {
        CreateCursorRequest createCursorRequest = objectMapper.readValue(
                objectMapper.writeValueAsString(new CreateGraphQLCursorRequest()),
                CreateCursorRequest.class);

        Assert.assertEquals(CreateGraphQLCursorRequest.CursorType, createCursorRequest.getCursorType());
        Assert.assertEquals(CreateGraphQLCursorRequest.class, createCursorRequest.getClass());
    }

    @Test
    public void testCreateGraphHierarchyCursorRequest() throws IOException {
        CreateCursorRequest createCursorRequest = objectMapper.readValue(
                objectMapper.writeValueAsString(new CreateGraphHierarchyCursorRequest(Arrays.asList("A", "B"))),
                CreateCursorRequest.class);

        Assert.assertEquals(CreateGraphHierarchyCursorRequest.CursorType, createCursorRequest.getCursorType());
        Assert.assertEquals(CreateGraphHierarchyCursorRequest.class, createCursorRequest.getClass());
        Assert.assertEquals(Arrays.asList("A", "B"), ((CreateGraphHierarchyCursorRequest)createCursorRequest).getCountTags());
    }

    //region Fields
    private static ObjectMapper objectMapper;
    //endregion
}
