package com.kayhut.fuse.unipop;

import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.promise.TraversalPromise;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import com.sun.org.apache.regexp.internal.StreamCharacterIterator;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Assert;
import org.junit.Test;
import org.unipop.process.strategyregistrar.StandardStrategyProvider;
import org.unipop.structure.UniGraph;

/**
 * Created by User on 19/03/2017.
 */
public class TraversalTest {
    @Test
    public void g_V_hasXpromise_Promise_asXabcX_byX__hasXlabel_dragonXXX() throws Exception {
        UniGraph graph = new UniGraph(new TestControllerManagerFactory(), new StandardStrategyProvider());
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

        TraversalPromise traversalPromise = (TraversalPromise) promiseVertex.getPromise();
        Assert.assertTrue(traversalPromise.getTraversal().equals(dragonTraversal));
    }
}
