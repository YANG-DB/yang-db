package com.kayhut.fuse.unipop.process;

import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import com.kayhut.fuse.unipop.process.traversal.strategy.decoration.ForceRequirementsStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unipop.structure.UniVertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roman on 04/07/2017.
 */
public class JoinStepTest {
    @Before
    public void setup() {
        ForceRequirementsStrategy.addRequirements(
                TraversalStrategies.GlobalCache.getStrategies(EmptyGraph.class),
                TraverserRequirement.PATH);
    }

    @Test
    public void testSingleLeftSimpleMultipleRightSimple() {
        GraphTraversal<Object, Object> traversal = __.start();
        traversal.asAdmin().addStep(new JoinStep<>(__.start().asAdmin()));
        traversal.by(__.start().inject(
                new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "1").get(), null))
                .as("A").asAdmin());

        traversal.by(__.start().inject(
                        new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "1").get(), null),
                        new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "2").get(), null),
                        new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "3").get(), null))
                .as("B").asAdmin());

        Traverser.Admin<Object> joinTraverser = traversal.asAdmin().nextTraverser();
        Vertex joinVertex = (Vertex)joinTraverser.get();
        Path joinPath = joinTraverser.path();

        Assert.assertEquals("1", joinVertex.id());

        Assert.assertEquals(1, joinPath.size());
        Assert.assertEquals("1", ((Vertex)joinPath.get("A")).id());
    }

    @Test
    public void testMultipleLeftSimpleMultipleRightSimple() {
        GraphTraversal<Object, Object> traversal = __.start();
        traversal.asAdmin().addStep(new JoinStep<>(__.start().asAdmin()));
        traversal.by(__.start().inject(
                new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "1").get(), null),
                new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "3").get(), null))
                .as("A").asAdmin());

        traversal.by(__.start().inject(
                new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "1").get(), null),
                new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "2").get(), null),
                new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "3").get(), null))
                .as("B").asAdmin());

        List<Traverser<Object>> traversers = new ArrayList<>();
        while(traversal.asAdmin().hasNext()) {
            traversers.add(traversal.asAdmin().nextTraverser());
        }

        Assert.assertEquals(2, traversers.size());
        Assert.assertEquals("1", ((Vertex)traversers.get(0).get()).id());
        Assert.assertEquals("3", ((Vertex)traversers.get(1).get()).id());

        Assert.assertEquals(1, traversers.get(0).path().size());
        Assert.assertEquals(1, traversers.get(1).path().size());

        Assert.assertEquals("1", ((Vertex)traversers.get(0).path().get("A")).id());
        Assert.assertEquals("3", ((Vertex)traversers.get(1).path().get("A")).id());
    }

    @Test
    public void testSingleLeftComplexMultipleRightSimple() {
        GraphTraversal<Object, Object> traversal = __.start();
        traversal.asAdmin().addStep(new JoinStep<>(__.start().asAdmin()));
        traversal.by(__.start().inject(1).as("A")
                .map(traverser -> Integer.toString((int)traverser.get())).as("B")
                .map(traverser -> new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), traverser.get()).get(), null)).as("C"));

        traversal.by(__.start().inject(
                new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "1").get(), null),
                new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "2").get(), null),
                new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "3").get(), null)).as("B"));

        Traverser.Admin<Object> joinTraverser = traversal.asAdmin().nextTraverser();
        Vertex joinVertex = (Vertex)joinTraverser.get();
        Path joinPath = joinTraverser.path();

        Assert.assertEquals("1", joinVertex.id());

        Assert.assertEquals(3, joinPath.size());
        Assert.assertEquals(1, (int)joinPath.get("A"));
        Assert.assertEquals("1", joinPath.get("B"));
        Assert.assertEquals("1", ((Vertex)joinPath.get("C")).id());
    }

    @Test
    public void testSingleLeftSimpleMultipleRightComplexWithFullLabels() {
        GraphTraversal<Object, Object> traversal = __.start();
        traversal.asAdmin().addStep(new JoinStep<>(__.start().asAdmin()));
        traversal.by(__.start().inject(
                new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "1").get(), null)).as("A"));

        traversal.by(__.start().inject(1, 2, 3).as("D")
                .map(traverser -> Integer.toString((int)traverser.get())).as("E")
                .map(traverser -> new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), traverser.get()).get(), null)).as("F"));

        Traverser.Admin<Object> joinTraverser = traversal.asAdmin().nextTraverser();
        Vertex joinVertex = (Vertex)joinTraverser.get();
        Path joinPath = joinTraverser.path();

        Assert.assertEquals("1", joinVertex.id());

        Assert.assertEquals(4, joinPath.size());
        Assert.assertEquals("1", ((Vertex)joinPath.head()).id());
        Assert.assertEquals("1", ((Vertex)joinPath.get("A")).id());
        Assert.assertEquals(1, (int)joinPath.get("D"));
        Assert.assertEquals("1", joinPath.get("E"));
    }

    @Test
    public void testSingleLeftSimpleMultipleRightComplexWithEmptyLabels() {
        GraphTraversal<Object, Object> traversal = __.start();
        traversal.asAdmin().addStep(new JoinStep<>(__.start().asAdmin()));
        traversal.by(__.start().inject(
                new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), "1").get(), null)).as("A"));

        traversal.by(__.start().inject(1, 2, 3)
                .map(traverser -> Integer.toString((int)traverser.get())).as("E")
                .map(traverser -> new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), traverser.get()).get(), null)).as("F"));

        Traverser.Admin<Object> joinTraverser = traversal.asAdmin().nextTraverser();
        Vertex joinVertex = (Vertex)joinTraverser.get();
        Path joinPath = joinTraverser.path();

        Assert.assertEquals("1", joinVertex.id());

        Assert.assertEquals(3, joinPath.size());
        Assert.assertEquals("1", ((Vertex)joinPath.head()).id());
        Assert.assertEquals("1", ((Vertex)joinPath.get("A")).id());
        Assert.assertEquals("1", joinPath.get("E"));
    }

    @Test
    public void testSingleLeftComplexMultipleRightComplex() {
        GraphTraversal<Object, Object> traversal = __.start();
        traversal.asAdmin().addStep(new JoinStep<>(__.start().asAdmin()));
        traversal.by(__.start().inject(1).as("A")
                .map(traverser -> Integer.toString((int)traverser.get())).as("B")
                .map(traverser -> new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), traverser.get()).get(), null)).as("C"));


        traversal.by(__.start().inject(1, 2, 3).as("D")
                .map(traverser -> Integer.toString((int)traverser.get())).as("E")
                .map(traverser -> new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), traverser.get()).get(), null)).as("F"));

        Traverser.Admin<Object> joinTraverser = traversal.asAdmin().nextTraverser();
        Vertex joinVertex = (Vertex)joinTraverser.get();
        Path joinPath = joinTraverser.path();

        Assert.assertEquals("1", joinVertex.id());

        Assert.assertEquals(6, joinPath.size());
        Assert.assertEquals("1", ((Vertex)joinPath.head()).id());
        Assert.assertEquals(1, (int)joinPath.get("A"));
        Assert.assertEquals("1", joinPath.get("B"));
        Assert.assertEquals("1", ((Vertex)joinPath.get("C")).id());
        Assert.assertEquals(1, (int)joinPath.get("D"));
        Assert.assertEquals("1", joinPath.get("E"));
    }

    @Test
    public void testMultipleLeftComplexMultipleRightComplex() {
        GraphTraversal<Object, Object> traversal = __.start();
        traversal.asAdmin().addStep(new JoinStep<>(__.start().asAdmin()));
        traversal.by(__.start().inject(1, 3).as("A")
                .map(traverser -> Integer.toString((int)traverser.get())).as("B")
                .map(traverser -> new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), traverser.get()).get(), null)).as("C"));


        traversal.by(__.start().inject(1, 2, 3).as("D")
                .map(traverser -> Integer.toString((int)traverser.get())).as("E")
                .map(traverser -> new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), traverser.get()).get(), null)).as("F"));

        List<Traverser<Object>> traversers = new ArrayList<>();
        while(traversal.asAdmin().hasNext()) {
            traversers.add(traversal.asAdmin().nextTraverser());
        }

        Assert.assertEquals("1", ((Vertex)traversers.get(0).get()).id());
        Assert.assertEquals("3", ((Vertex)traversers.get(1).get()).id());

        Assert.assertEquals(6, traversers.get(0).path().size());
        Assert.assertEquals("1", ((Vertex)traversers.get(0).path().head()).id());
        Assert.assertEquals(1, (int)traversers.get(0).path().get("A"));
        Assert.assertEquals("1", traversers.get(0).path().get("B"));
        Assert.assertEquals("1", ((Vertex)traversers.get(0).path().get("C")).id());
        Assert.assertEquals(1, (int)traversers.get(0).path().get("D"));
        Assert.assertEquals("1", traversers.get(0).path().get("E"));

        Assert.assertEquals(6, traversers.get(1).path().size());
        Assert.assertEquals("3", ((Vertex)traversers.get(1).path().head()).id());
        Assert.assertEquals(3, (int)traversers.get(1).path().get("A"));
        Assert.assertEquals("3", traversers.get(1).path().get("B"));
        Assert.assertEquals("3", ((Vertex)traversers.get(1).path().get("C")).id());
        Assert.assertEquals(3, (int)traversers.get(1).path().get("D"));
        Assert.assertEquals("3", traversers.get(1).path().get("E"));
    }

    @Test
    public void testMultipleLeftComplexMultipleRightComplexWithIdIntegration() {
        GraphTraversal<Object, Object> traversal = __.start();
        JoinStep<Object, Vertex> joinStep = new JoinStep<>(__.start().asAdmin());
        joinStep.setIntegrateIdsTraversalFunction((leftTraversal, ids) ->
                        leftTraversal.addStep(new HasStep<>(leftTraversal, new HasContainer(T.id.getAccessor(), P.within(ids)))));

        traversal.asAdmin().addStep(joinStep);
        traversal.by(__.start().inject(1, 3).as("A")
                .map(traverser -> Integer.toString((int)traverser.get())).as("B")
                .map(traverser -> new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), traverser.get()).get(), null)).as("C"));


        traversal.by(__.start().inject(1, 2, 3).as("D")
                .map(traverser -> Integer.toString((int)traverser.get())).as("E")
                .map(traverser -> new UniVertex(new MapBuilder<String, Object>().put(T.id.getAccessor(), traverser.get()).get(), null)).as("F"));

        List<Traverser<Object>> traversers = new ArrayList<>();
        while(traversal.asAdmin().hasNext()) {
            traversers.add(traversal.asAdmin().nextTraverser());
        }

        Assert.assertEquals("1", ((Vertex)traversers.get(0).get()).id());
        Assert.assertEquals("3", ((Vertex)traversers.get(1).get()).id());

        Assert.assertEquals(6, traversers.get(0).path().size());
        Assert.assertEquals("1", ((Vertex)traversers.get(0).path().head()).id());
        Assert.assertEquals(1, (int)traversers.get(0).path().get("A"));
        Assert.assertEquals("1", traversers.get(0).path().get("B"));
        Assert.assertEquals("1", ((Vertex)traversers.get(0).path().get("C")).id());
        Assert.assertEquals(1, (int)traversers.get(0).path().get("D"));
        Assert.assertEquals("1", traversers.get(0).path().get("E"));

        Assert.assertEquals(6, traversers.get(1).path().size());
        Assert.assertEquals("3", ((Vertex)traversers.get(1).path().head()).id());
        Assert.assertEquals(3, (int)traversers.get(1).path().get("A"));
        Assert.assertEquals("3", traversers.get(1).path().get("B"));
        Assert.assertEquals("3", ((Vertex)traversers.get(1).path().get("C")).id());
        Assert.assertEquals(3, (int)traversers.get(1).path().get("D"));
        Assert.assertEquals("3", traversers.get(1).path().get("E"));
    }
}
