package com.kayhut.fuse.unipop;

import com.google.common.collect.ImmutableSet;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.PromiseElementController;
import com.kayhut.fuse.unipop.controller.PromiseVertexController;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalHashProvider;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.promise.TraversalPromise;
import com.kayhut.fuse.unipop.schemaProviders.EmptyGraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unipop.process.strategyregistrar.StandardStrategyProvider;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.mock;

/**
 * Created by User on 19/03/2017.
 */
public class TraversalTest {
    Client client;
    ElasticGraphConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        client = mock(Client.class);
        configuration = mock(ElasticGraphConfiguration.class);

    }

    @Test
    public void g_V_hasXpromise_Promise_asXabcX_byX__hasXlabel_dragonXXX() throws Exception {
        //region ControllerManagerFactory Implementation
        UniGraph graph = new UniGraph(null, graph1 -> new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return ImmutableSet.of(
                        new PromiseElementController(client, configuration, graph1, new EmptyGraphElementSchemaProvider()),
                        new PromiseVertexController(client, configuration, graph1, new EmptyGraphElementSchemaProvider()));
            }

            @Override
            public void close() {

            }
        }, new StandardStrategyProvider());
        GraphTraversalSource g = graph.traversal();

        Traversal dragonTraversal = __.has("label", "dragon");

        Traversal<Vertex, Vertex> traversal = g.V().has("promise", Promise.as("A").by(dragonTraversal));
        Vertex vertex = traversal.next();

        Assert.assertTrue(!traversal.hasNext());
        Assert.assertTrue(vertex.getClass().equals(PromiseVertex.class));

        PromiseVertex promiseVertex = (PromiseVertex)vertex;
        Assert.assertTrue(promiseVertex.id().equals("A"));
        Assert.assertTrue(promiseVertex.label().equals("promise"));
        Assert.assertTrue(promiseVertex.getPromise().getClass().equals(TraversalPromise.class));
        Assert.assertTrue(promiseVertex.getConstraint().equals(Optional.empty()));

        TraversalPromise traversalPromise = (TraversalPromise) promiseVertex.getPromise();
        Assert.assertTrue(traversalPromise.getTraversal().equals(dragonTraversal));
    }

    @Test
    public void g_V_hasXpromise_Promise_asXabcX_byX__hasXlabel_dragonXXX_hasXconstraint_Constraint_byX__hasXlabel_dragonXXX() throws Exception {
        //region ControllerManagerFactory Implementation
        UniGraph graph = new UniGraph(null, graph1 -> new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return ImmutableSet.of(
                        new PromiseElementController(client,configuration,graph1,new EmptyGraphElementSchemaProvider()),
                        new PromiseVertexController(client,configuration,graph1,new EmptyGraphElementSchemaProvider()));
            }

            @Override
            public void close() {

            }
        }, new StandardStrategyProvider());
        GraphTraversalSource g = graph.traversal();

        Traversal dragonTraversal = __.has("label", "dragon");

        Traversal<Vertex, Vertex> traversal = g.V().has("promise", Promise.as("A").by(dragonTraversal)).has("constraint", Constraint.by(dragonTraversal));
        Vertex vertex = traversal.next();

        Assert.assertTrue(!traversal.hasNext());
        Assert.assertTrue(vertex.getClass().equals(PromiseVertex.class));

        PromiseVertex promiseVertex = (PromiseVertex)vertex;
        Assert.assertTrue(promiseVertex.id().equals("A"));
        Assert.assertTrue(promiseVertex.label().equals("promise"));
        Assert.assertTrue(promiseVertex.getPromise().getClass().equals(TraversalPromise.class));
        Assert.assertTrue(promiseVertex.getConstraint().get().getClass().equals(TraversalConstraint.class));

        TraversalPromise traversalPromise = (TraversalPromise) promiseVertex.getPromise();
        Assert.assertTrue(traversalPromise.getTraversal().equals(dragonTraversal));

        TraversalConstraint traversalConstraint = (TraversalConstraint) promiseVertex.getConstraint().get();
        Assert.assertTrue(traversalConstraint.getTraversal().equals(dragonTraversal));
    }
}
