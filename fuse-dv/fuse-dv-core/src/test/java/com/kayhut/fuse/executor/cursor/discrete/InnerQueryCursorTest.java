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
        Assert.assertEquals(1, nextResults.getAssignments().get(0).getEntities().size());
        Assert.assertEquals("123", nextResults.getAssignments().get(0).getEntities().get(0).geteID());
        Assert.assertTrue( nextResults.getAssignments().get(0).getEntities().get(0).geteTag().contains("Child"));
        Assert.assertEquals("Entity", nextResults.getAssignments().get(0).getEntities().get(0).geteType());
    }

    @Test
    public void testOnlyRootsElement(){}

    @Test
    public void testSinglePath(){}



}
