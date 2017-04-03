package com.kayhut.fuse.unipop;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.SearchPromiseElementController;
import com.kayhut.fuse.unipop.promise.*;
import com.kayhut.fuse.unipop.schemaProviders.EmptyGraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by User on 19/03/2017.
 */
public class SearchPromiseElementControllerTest {
    Client client;
    ElasticGraphConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        client = mock(Client.class);
        configuration = mock(ElasticGraphConfiguration.class);
    }

    @Test
    public void testSingleIdPromiseVertexWithoutConstraint() {
        UniGraph graph = mock(UniGraph.class);

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(new HasContainer("promise", P.eq(Promise.as("A")))));

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Vertex.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);

        SearchPromiseElementController controller = new SearchPromiseElementController(client,configuration,graph,new EmptyGraphElementSchemaProvider());
        List<Vertex> vertices = Stream.ofAll(() -> (Iterator<Vertex>)controller.search(searchQuery)).toJavaList();

        Assert.assertTrue(vertices.size() == 1);
        Assert.assertTrue(vertices.get(0).id().equals("A"));
        Assert.assertTrue(vertices.get(0).label().equals("promise"));
        Assert.assertTrue(vertices.get(0).getClass().equals(PromiseVertex.class));

        PromiseVertex promiseVertex = (PromiseVertex)vertices.get(0);
        Assert.assertTrue(promiseVertex.getPromise().getId().equals("A"));
        Assert.assertTrue(promiseVertex.getPromise().getClass().equals(IdPromise.class));

        Assert.assertTrue(promiseVertex.getConstraint().equals(Optional.empty()));
    }

    @Test
    public void testSingleTraversalPromiseVertexWithoutConstraint() {
        UniGraph graph = mock(UniGraph.class);

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        Traversal dragonTraversal = __.has("label", P.eq("dragon"));
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(new HasContainer("promise", P.eq(Promise.as("A").by(dragonTraversal)))));

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Vertex.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);

        SearchPromiseElementController controller = new SearchPromiseElementController(client,configuration,graph,new EmptyGraphElementSchemaProvider());
        List<Vertex> vertices = Stream.ofAll(() -> (Iterator<Vertex>)controller.search(searchQuery)).toJavaList();

        Assert.assertTrue(vertices.size() == 1);
        Assert.assertTrue(vertices.get(0).id().equals("A"));
        Assert.assertTrue(vertices.get(0).label().equals("promise"));
        Assert.assertTrue(vertices.get(0).getClass().equals(PromiseVertex.class));

        PromiseVertex promiseVertex = (PromiseVertex)vertices.get(0);
        Assert.assertTrue(promiseVertex.getPromise().getId().equals("A"));
        Assert.assertTrue(promiseVertex.getPromise().getClass().equals(TraversalPromise.class));

        TraversalPromise traversalPromise = (TraversalPromise) promiseVertex.getPromise();
        Assert.assertTrue(traversalPromise.getTraversal() != null);
        Assert.assertEquals(dragonTraversal, traversalPromise.getTraversal());

        Assert.assertTrue(promiseVertex.getConstraint().equals(Optional.empty()));
    }

    @Test
    public void testMultipleIdPromiseVertexWithoutConstraint() {
        UniGraph graph = mock(UniGraph.class);

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(new HasContainer("promise", P.within(Promise.as("A"), Promise.as("B"), Promise.as("C")))));

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Vertex.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);

        SearchPromiseElementController controller = new SearchPromiseElementController(client,configuration,graph,new EmptyGraphElementSchemaProvider());
        List<Vertex> vertices = Stream.ofAll(() -> (Iterator<Vertex>)controller.search(searchQuery)).toJavaList();

        Assert.assertTrue(vertices.size() == 3);
        Assert.assertTrue(vertices.get(0).id().equals("A"));
        Assert.assertTrue(vertices.get(1).id().equals("B"));
        Assert.assertTrue(vertices.get(2).id().equals("C"));
        vertices.forEach(v -> Assert.assertTrue(v.label().equals("promise")));
        vertices.forEach(v -> Assert.assertTrue(v.getClass().equals(PromiseVertex.class)));

        Assert.assertTrue(((PromiseVertex)vertices.get(0)).getPromise().getId().equals("A"));
        Assert.assertTrue(((PromiseVertex)vertices.get(1)).getPromise().getId().equals("B"));
        Assert.assertTrue(((PromiseVertex)vertices.get(2)).getPromise().getId().equals("C"));
        vertices.forEach(v -> Assert.assertTrue(((PromiseVertex)v).getPromise().getClass().equals(IdPromise.class)));

        vertices.forEach(v -> Assert.assertTrue(((PromiseVertex)v).getConstraint().equals(Optional.empty())));
    }

    @Test
    public void testMultipleTraversalPromiseVertexWithoutConstraint() {
        UniGraph graph = mock(UniGraph.class);

        Traversal dragonTraversal = __.has("label", P.eq("dragon"));
        Traversal guildTraversal = __.has("label", P.eq("guild"));
        Traversal kingdomTraversal = __.has("label", P.eq("kingdom"));

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(new HasContainer("promise", P.within(
                Promise.as("A").by(dragonTraversal),
                Promise.as("B").by(guildTraversal),
                Promise.as("C").by(kingdomTraversal)))));

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Vertex.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);

        SearchPromiseElementController controller = new SearchPromiseElementController(client,configuration,graph,new EmptyGraphElementSchemaProvider());
        List<Vertex> vertices = Stream.ofAll(() -> (Iterator<Vertex>)controller.search(searchQuery)).toJavaList();

        Assert.assertTrue(vertices.size() == 3);
        Assert.assertTrue(vertices.get(0).id().equals("A"));
        Assert.assertTrue(vertices.get(1).id().equals("B"));
        Assert.assertTrue(vertices.get(2).id().equals("C"));
        vertices.forEach(v -> Assert.assertTrue(v.label().equals("promise")));
        vertices.forEach(v -> Assert.assertTrue(v.getClass().equals(PromiseVertex.class)));

        Assert.assertTrue(((PromiseVertex)vertices.get(0)).getPromise().getId().equals("A"));
        Assert.assertTrue(((PromiseVertex)vertices.get(1)).getPromise().getId().equals("B"));
        Assert.assertTrue(((PromiseVertex)vertices.get(2)).getPromise().getId().equals("C"));
        vertices.forEach(v -> Assert.assertTrue(((PromiseVertex)v).getPromise().getClass().equals(TraversalPromise.class)));
        Assert.assertTrue(((TraversalPromise)((PromiseVertex)vertices.get(0)).getPromise()).getTraversal().equals(dragonTraversal));
        Assert.assertTrue(((TraversalPromise)((PromiseVertex)vertices.get(1)).getPromise()).getTraversal().equals(guildTraversal));
        Assert.assertTrue(((TraversalPromise)((PromiseVertex)vertices.get(2)).getPromise()).getTraversal().equals(kingdomTraversal));

        vertices.forEach(v -> Assert.assertTrue(((PromiseVertex)v).getConstraint().equals(Optional.empty())));
    }

    @Test
    public void testSingleIdPromiseVertexWithConstraint() {
        UniGraph graph = mock(UniGraph.class);

        Traversal dragonTraversal = __.has("label", P.eq("dragon"));

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(
                new HasContainer("promise", P.eq(Promise.as("A"))),
                new HasContainer("constraint", P.eq(Constraint.by(dragonTraversal)))));

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Vertex.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);

        SearchPromiseElementController controller = new SearchPromiseElementController(client,configuration,graph,new EmptyGraphElementSchemaProvider());
        List<Vertex> vertices = Stream.ofAll(() -> (Iterator<Vertex>)controller.search(searchQuery)).toJavaList();

        Assert.assertTrue(vertices.size() == 1);
        Assert.assertTrue(vertices.get(0).id().equals("A"));
        Assert.assertTrue(vertices.get(0).label().equals("promise"));
        Assert.assertTrue(vertices.get(0).getClass().equals(PromiseVertex.class));

        PromiseVertex promiseVertex = (PromiseVertex)vertices.get(0);
        Assert.assertTrue(promiseVertex.getPromise().getId().equals("A"));
        Assert.assertTrue(promiseVertex.getPromise().getClass().equals(IdPromise.class));

        Assert.assertTrue(promiseVertex.getConstraint().get().getClass().equals(TraversalConstraint.class));
        Assert.assertTrue(((TraversalConstraint)promiseVertex.getConstraint().get()).getTraversal().equals(dragonTraversal));
    }

    @Test
    public void testSingleTraversalPromiseVertexWithConstraint() {
        UniGraph graph = mock(UniGraph.class);

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        Traversal dragonTraversal = __.has("label", P.eq("dragon"));
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(
                new HasContainer("promise", P.eq(Promise.as("A").by(dragonTraversal))),
                new HasContainer("constraint", P.eq(Constraint.by(dragonTraversal)))));

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Vertex.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);

        SearchPromiseElementController controller = new SearchPromiseElementController(client,configuration,graph,new EmptyGraphElementSchemaProvider());
        List<Vertex> vertices = Stream.ofAll(() -> (Iterator<Vertex>)controller.search(searchQuery)).toJavaList();

        Assert.assertTrue(vertices.size() == 1);
        Assert.assertTrue(vertices.get(0).id().equals("A"));
        Assert.assertTrue(vertices.get(0).label().equals("promise"));
        Assert.assertTrue(vertices.get(0).getClass().equals(PromiseVertex.class));

        PromiseVertex promiseVertex = (PromiseVertex)vertices.get(0);
        Assert.assertTrue(promiseVertex.getPromise().getId().equals("A"));
        Assert.assertTrue(promiseVertex.getPromise().getClass().equals(TraversalPromise.class));

        TraversalPromise traversalPromise = (TraversalPromise) promiseVertex.getPromise();
        Assert.assertTrue(traversalPromise.getTraversal() != null);
        Assert.assertEquals(dragonTraversal, traversalPromise.getTraversal());

        Assert.assertTrue(promiseVertex.getConstraint().get().getClass().equals(TraversalConstraint.class));
        Assert.assertTrue(((TraversalConstraint)promiseVertex.getConstraint().get()).getTraversal().equals(dragonTraversal));
    }

    @Test
    public void testMultipleIdPromiseVertexWithConstraint() {
        UniGraph graph = mock(UniGraph.class);

        Traversal dragonTraversal = __.has("label", P.eq("dragon"));

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(
                new HasContainer("promise", P.within(Promise.as("A"), Promise.as("B"), Promise.as("C"))),
                new HasContainer("constraint", P.eq(Constraint.by(dragonTraversal)))));

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Vertex.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);

        SearchPromiseElementController controller = new SearchPromiseElementController(client,configuration,graph,new EmptyGraphElementSchemaProvider());
        List<Vertex> vertices = Stream.ofAll(() -> (Iterator<Vertex>)controller.search(searchQuery)).toJavaList();

        Assert.assertTrue(vertices.size() == 3);
        Assert.assertTrue(vertices.get(0).id().equals("A"));
        Assert.assertTrue(vertices.get(1).id().equals("B"));
        Assert.assertTrue(vertices.get(2).id().equals("C"));
        vertices.forEach(v -> Assert.assertTrue(v.label().equals("promise")));
        vertices.forEach(v -> Assert.assertTrue(v.getClass().equals(PromiseVertex.class)));

        Assert.assertTrue(((PromiseVertex)vertices.get(0)).getPromise().getId().equals("A"));
        Assert.assertTrue(((PromiseVertex)vertices.get(1)).getPromise().getId().equals("B"));
        Assert.assertTrue(((PromiseVertex)vertices.get(2)).getPromise().getId().equals("C"));
        vertices.forEach(v -> Assert.assertTrue(((PromiseVertex)v).getPromise().getClass().equals(IdPromise.class)));

        vertices.forEach(v -> Assert.assertTrue(((PromiseVertex)v).getConstraint().get().getClass().equals(TraversalConstraint.class)));
        vertices.forEach(v -> Assert.assertTrue(((TraversalConstraint)((PromiseVertex)v).getConstraint().get()).getTraversal().equals(dragonTraversal)));
    }

    @Test
    public void testMultipleTraversalPromiseVertexWithConstraint() {
        UniGraph graph = mock(UniGraph.class);

        Traversal dragonTraversal = __.has("label", P.eq("dragon"));
        Traversal guildTraversal = __.has("label", P.eq("guild"));
        Traversal kingdomTraversal = __.has("label", P.eq("kingdom"));

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Arrays.asList(
                new HasContainer("promise", P.within(
                        Promise.as("A").by(dragonTraversal),
                        Promise.as("B").by(guildTraversal),
                        Promise.as("C").by(kingdomTraversal))),
                new HasContainer("constraint", P.eq(Constraint.by(dragonTraversal)))));

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getReturnType()).thenReturn(Vertex.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);

        SearchPromiseElementController controller = new SearchPromiseElementController(client,configuration,graph,new EmptyGraphElementSchemaProvider());
        List<Vertex> vertices = Stream.ofAll(() -> (Iterator<Vertex>)controller.search(searchQuery)).toJavaList();

        Assert.assertTrue(vertices.size() == 3);
        Assert.assertTrue(vertices.get(0).id().equals("A"));
        Assert.assertTrue(vertices.get(1).id().equals("B"));
        Assert.assertTrue(vertices.get(2).id().equals("C"));
        vertices.forEach(v -> Assert.assertTrue(v.label().equals("promise")));
        vertices.forEach(v -> Assert.assertTrue(v.getClass().equals(PromiseVertex.class)));

        Assert.assertTrue(((PromiseVertex)vertices.get(0)).getPromise().getId().equals("A"));
        Assert.assertTrue(((PromiseVertex)vertices.get(1)).getPromise().getId().equals("B"));
        Assert.assertTrue(((PromiseVertex)vertices.get(2)).getPromise().getId().equals("C"));
        vertices.forEach(v -> Assert.assertTrue(((PromiseVertex)v).getPromise().getClass().equals(TraversalPromise.class)));
        Assert.assertTrue(((TraversalPromise)((PromiseVertex)vertices.get(0)).getPromise()).getTraversal().equals(dragonTraversal));
        Assert.assertTrue(((TraversalPromise)((PromiseVertex)vertices.get(1)).getPromise()).getTraversal().equals(guildTraversal));
        Assert.assertTrue(((TraversalPromise)((PromiseVertex)vertices.get(2)).getPromise()).getTraversal().equals(kingdomTraversal));

        vertices.forEach(v -> Assert.assertTrue(((PromiseVertex)v).getConstraint().get().getClass().equals(TraversalConstraint.class)));
        vertices.forEach(v -> Assert.assertTrue(((TraversalConstraint)((PromiseVertex)v).getConstraint().get()).getTraversal().equals(dragonTraversal)));
    }
}
