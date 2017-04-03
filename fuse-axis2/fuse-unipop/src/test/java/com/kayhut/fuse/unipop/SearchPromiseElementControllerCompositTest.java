package com.kayhut.fuse.unipop;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.SearchPromiseElementController;
import com.kayhut.fuse.unipop.promise.IdPromise;
import com.kayhut.fuse.unipop.schemaProviders.EmptyGraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by User on 19/03/2017.
 */
public class SearchPromiseElementControllerCompositTest {
    Client client;
    ElasticGraphConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        client = mock(Client.class);
        configuration = mock(ElasticGraphConfiguration.class);
        SearchAction searchAction = mock(SearchAction.class);
        SearchRequestBuilder requestBuilder = new SearchRequestBuilder(client, searchAction);

        when(client.prepareSearch()).thenReturn(requestBuilder);
        when(client.prepareSearch(Matchers.any(String[].class))).then(invocationOnMock -> requestBuilder);

    }

    @Test
    @Ignore
    public void testSingleIdPromiseVertexWithoutConstraint() {
        UniGraph graph = mock(UniGraph.class);

        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Collections.emptyList());

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
}
