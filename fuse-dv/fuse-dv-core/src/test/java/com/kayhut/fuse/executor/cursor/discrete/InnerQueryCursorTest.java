package com.kayhut.fuse.executor.cursor.discrete;

import com.kayhut.fuse.model.results.*;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static com.kayhut.fuse.model.results.Assignment.Builder.instance;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

public class InnerQueryCursorTest {

    @Test
    public void testSingleElement(){
        PathsTraversalCursor cursorMock = Mockito.mock(PathsTraversalCursor.class);
        AssignmentsQueryResult res1 = AssignmentsQueryResult.Builder.instance().
                withAssignment(
                        instance().withEntity(Entity.Builder.instance().withEID("123")
                                .withETag(Stream.of("Child").toJavaSet())
                                .withEType("Entity").build()).build())
                .build();

        when(cursorMock.getNextResults(anyInt())).thenReturn(res1, AssignmentsQueryResult.Builder.instance().build());

        InnerQueryCursor cursor = new InnerQueryCursor(cursorMock, null);
        AssignmentsQueryResult nextResults = (AssignmentsQueryResult) cursor.getNextResults(1000);
        Assert.assertEquals(1, nextResults.getAssignments().size());
        Assert.assertEquals("\"123\",\"123\",\"0\"", nextResults.getAssignments().get(0));
    }

    @Test
    public void testOnlyRootsElement(){
        PathsTraversalCursor cursorMock = Mockito.mock(PathsTraversalCursor.class);
        AssignmentsQueryResult res1 = AssignmentsQueryResult.Builder.instance().
                withAssignment(
                        instance().
                                withEntity(Entity.Builder.instance().withEID("123").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build()).build()).build();
        AssignmentsQueryResult res2 = AssignmentsQueryResult.Builder.instance().
                withAssignment(
                        instance().
                                withEntity(Entity.Builder.instance().withEID("124").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build()).build()).build();

        when(cursorMock.getNextResults(anyInt())).thenReturn(res1, res2, AssignmentsQueryResult.Builder.instance().build());

        HierarchyFlattenCursor cursor = new HierarchyFlattenCursor(cursorMock, null);
        CsvQueryResult nextResults = (CsvQueryResult) cursor.getNextResults(1000);
        Assert.assertEquals(2, nextResults.getCsvLines().length);
        List<String> lines = Arrays.asList(nextResults.getCsvLines());
        Assert.assertTrue(lines.contains("\"123\",\"123\",\"0\""));
        Assert.assertTrue(lines.contains("\"124\",\"124\",\"0\""));
    }

    @Test
    public void testSinglePath(){
        PathsTraversalCursor cursorMock = Mockito.mock(PathsTraversalCursor.class);
        AssignmentsQueryResult res1 = AssignmentsQueryResult.Builder.instance().
                withAssignment(
                        instance().
                                withEntity(Entity.Builder.instance().withEID("123").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build())
                                .withEntity(Entity.Builder.instance().withEID("124").withETag(Stream.of("Parent").toJavaSet()).withEType("Entity").build()).build())
                .withAssignment(instance().
                        withEntity(Entity.Builder.instance().withEID("124").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build()).build()).build();


        when(cursorMock.getNextResults(anyInt())).thenReturn(res1, AssignmentsQueryResult.Builder.instance().build());

        HierarchyFlattenCursor cursor = new HierarchyFlattenCursor(cursorMock, null);
        CsvQueryResult nextResults = (CsvQueryResult) cursor.getNextResults(1000);
        Assert.assertEquals(4, nextResults.getCsvLines().length);
        List<String> lines = Arrays.asList(nextResults.getCsvLines());
        Assert.assertTrue(lines.contains("\"124\",\"123\",\"1\""));
        Assert.assertTrue(lines.contains("\"123\",\"124\",\"-1\""));
        Assert.assertTrue(lines.contains("\"124\",\"124\",\"0\""));
        Assert.assertTrue(lines.contains("\"123\",\"123\",\"0\""));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testSinglePathAndCycle(){
        PathsTraversalCursor cursorMock = Mockito.mock(PathsTraversalCursor.class);
        AssignmentsQueryResult res1 = AssignmentsQueryResult.Builder.instance().
                withAssignment(
                        instance().
                                withEntity(Entity.Builder.instance().withEID("123").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build())
                                .withEntity(Entity.Builder.instance().withEID("124").withETag(Stream.of("Parent").toJavaSet()).withEType("Entity").build()).build())
                .withAssignment(instance().
                        withEntity(Entity.Builder.instance().withEID("124").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build()).build())
                .withAssignment(instance().
                        withEntity(Entity.Builder.instance().withEID("125").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build())
                        .withEntity(Entity.Builder.instance().withEID("126").withETag(Stream.of("Parent").toJavaSet()).withEType("Entity").build()).build())
                .withAssignment(instance().
                        withEntity(Entity.Builder.instance().withEID("126").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build())
                        .withEntity(Entity.Builder.instance().withEID("125").withETag(Stream.of("Parent").toJavaSet()).withEType("Entity").build()).build())
                .build();


        when(cursorMock.getNextResults(anyInt())).thenReturn(res1, AssignmentsQueryResult.Builder.instance().build());

        HierarchyFlattenCursor cursor = new HierarchyFlattenCursor(cursorMock, null);
        cursor.getNextResults(1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInternalCycle(){
        PathsTraversalCursor cursorMock = Mockito.mock(PathsTraversalCursor.class);
        AssignmentsQueryResult res1 = AssignmentsQueryResult.Builder.instance().
                withAssignment(
                        instance().
                                withEntity(Entity.Builder.instance().withEID("124").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build())
                                .withEntity(Entity.Builder.instance().withEID("123").withETag(Stream.of("Parent").toJavaSet()).withEType("Entity").build()).build())
                .withAssignment(instance().
                        withEntity(Entity.Builder.instance().withEID("123").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build()).build())
                .withAssignment(instance().
                        withEntity(Entity.Builder.instance().withEID("125").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build())
                        .withEntity(Entity.Builder.instance().withEID("124").withETag(Stream.of("Parent").toJavaSet()).withEType("Entity").build()).build())
                .withAssignment(instance().
                        withEntity(Entity.Builder.instance().withEID("124").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build())
                        .withEntity(Entity.Builder.instance().withEID("125").withETag(Stream.of("Parent").toJavaSet()).withEType("Entity").build()).build())
                .build();


        when(cursorMock.getNextResults(anyInt())).thenReturn(res1, AssignmentsQueryResult.Builder.instance().build());

        HierarchyFlattenCursor cursor = new HierarchyFlattenCursor(cursorMock, null);
        cursor.getNextResults(1000);
    }

    @Test
    public void testTree2Levels(){
        PathsTraversalCursor cursorMock = Mockito.mock(PathsTraversalCursor.class);
        AssignmentsQueryResult res1 = AssignmentsQueryResult.Builder.instance().
                withAssignment(
                        instance().
                                withEntity(Entity.Builder.instance().withEID("124").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build())
                                .withEntity(Entity.Builder.instance().withEID("123").withETag(Stream.of("Parent").toJavaSet()).withEType("Entity").build()).build())
                .withAssignment(instance().
                        withEntity(Entity.Builder.instance().withEID("123").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build()).build())
                .withAssignment(instance().
                        withEntity(Entity.Builder.instance().withEID("125").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build())
                        .withEntity(Entity.Builder.instance().withEID("124").withETag(Stream.of("Parent").toJavaSet()).withEType("Entity").build()).build())
                .withAssignment(instance().
                        withEntity(Entity.Builder.instance().withEID("126").withETag(Stream.of("Child").toJavaSet()).withEType("Entity").build())
                        .withEntity(Entity.Builder.instance().withEID("124").withETag(Stream.of("Parent").toJavaSet()).withEType("Entity").build()).build())
                .build();


        when(cursorMock.getNextResults(anyInt())).thenReturn(res1, AssignmentsQueryResult.Builder.instance().build());

        HierarchyFlattenCursor cursor = new HierarchyFlattenCursor(cursorMock, null);
        QueryResultBase nextResults = cursor.getNextResults(1000);
        Assert.assertEquals(14, nextResults.getSize());

    }



}
