package com.yangdb.fuse.client.export;

import com.yangdb.fuse.client.export.graphml.GraphMLWriter;
import com.yangdb.fuse.model.results.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;

public class GraphMLWriterTests {
    private AssignmentsQueryResult assignment;
    public static final String EXPECTED_GRAPHML = "<?xml version=\"1.0\" ?>\n" +
            "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns www.yangdb.org\">\n" +
            "    <key id=\"Entity\" for=\"node\" attr.name=\"Entity\" attr.type=\"string\"></key>\n" +
            "    <key id=\"Relation\" for=\"edge\" attr.name=\"Relation\" attr.type=\"string\"></key>\n" +
            "    <graph id=\"G\" edgedefault=\"directed\">\n" +
            "        <node id=\"e0000002\">\n" +
            "            <data key=\"Entity\">EValue</data>\n" +
            "        </node>\n" +
            "        <node id=\"e0000001\">\n" +
            "            <data key=\"Entity\">Entity</data>\n" +
            "        </node>\n" +
            "        <edge id=\"r000001\" source=\"e0000001\" target=\"e0000002\">\n" +
            "            <data key=\"Relation\">hasEvalue</data>\n" +
            "        </edge>\n" +
            "    </graph>\n" +
            "</graphml>";

    @Before
    public void setUp() throws Exception {

        final Entity entity1 = new Entity();
        entity1.seteID("e0000001");
        entity1.seteTag(singleton("A"));
        entity1.seteType("Entity");
        entity1.setProperty(new Property("name", "me"));
        final Entity entity2 = new Entity();
        entity2.seteID("e0000002");
        entity2.seteTag(singleton("B"));
        entity2.seteType("EValue");
        entity2.setProperty(new Property("name", "you"));

        final Relationship relationship = new Relationship();
        relationship.setrID("r000001");
        relationship.setrType("hasEvalue");
        relationship.seteID1(entity1.geteID());
        relationship.seteID2(entity2.geteID());

        assignment = AssignmentsQueryResult.Builder.instance()
                .withAssignment(
                        Assignment.Builder.instance()
                                .withEntity(entity1)//context entity
                                .withEntity(entity2)//context entity
                                .withRelationships(Collections.singletonList(relationship))//relationships
                                .build())
                .build();

    }

    @Test
    public void simpleEntityWithRelationExport() throws IOException {
        GraphMLWriter writer = new GraphMLWriter(true, emptyMap(), emptyMap(), "www.yangdb.org", "Relation", "Entity");
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final List<Assignment> assignments = assignment.getAssignments();
        writer.writeGraph(stream, assignments.get(0));
        String finalString = new String(stream.toByteArray());
        Assert.assertNotNull(finalString);
        Assert.assertEquals(EXPECTED_GRAPHML,finalString);

    }
}
