package com.kayhut.fuse.unipop;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.SearchPromiseElementController;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.IdPromise;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.schemaProviders.EmptyGraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.PromiseVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.internal.InternalSearchHit;
import org.elasticsearch.search.internal.InternalSearchHitField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.search.SearchQuery;
import org.unipop.structure.UniGraph;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
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
        SearchHits searchHits = mock(SearchHits.class);
        SearchResponse searchResponse = mock(SearchResponse.class);
        ListenableActionFuture actionFuture = mock(ListenableActionFuture.class);
        SearchRequestBuilder requestBuilder = mock(SearchRequestBuilder.class);//new SearchRequestBuilder(client, searchAction);
        SearchScrollRequestBuilder scrollRequestBuilder = mock(SearchScrollRequestBuilder.class);
        //client
        when(client.prepareSearchScroll(anyString())).thenReturn(scrollRequestBuilder);
        when(client.prepareSearch()).thenReturn(requestBuilder);
        when(client.prepareSearch(Matchers.any(String[].class))).then(invocationOnMock -> requestBuilder);

        //SearchRequestBuilder
        when(requestBuilder.setScroll(Matchers.any(TimeValue.class))).thenReturn(requestBuilder);
        when(requestBuilder.setSize(anyInt())).thenReturn(requestBuilder);
        when(requestBuilder.setSearchType(Matchers.any(SearchType.class))).thenReturn(requestBuilder);
        when(requestBuilder.execute()).thenReturn(actionFuture);

        //scrollRequestBuilder
        when(scrollRequestBuilder.setScroll(Matchers.any(TimeValue.class))).thenReturn(scrollRequestBuilder);
        when(scrollRequestBuilder.execute()).thenReturn(actionFuture);

        //ListenableActionFuture
        when(actionFuture.actionGet()).thenReturn(searchResponse);
        //searchResponse
        when(searchResponse.getScrollId()).thenReturn("a");

        //search hits
        Map<String, SearchHitField> fields = Collections.singletonMap("hi", new InternalSearchHitField("name", new ArrayList()));
        InternalSearchHit[] tests = (InternalSearchHit[]) Arrays.asList(new InternalSearchHit(1, "1", new Text("test"), fields)).toArray();
        when(searchHits.getHits()).thenReturn(tests);
        when(searchResponse.getHits()).thenReturn(searchHits);


    }

    @Test
    public void testSingleIdPromiseVertexWithLimit() {
        UniGraph graph = mock(UniGraph.class);
        PredicatesHolder predicatesHolder = mock(PredicatesHolder.class);
        when(predicatesHolder.getPredicates()).thenReturn(Collections.emptyList());

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getLimit()).thenReturn(10);
        when(searchQuery.getReturnType()).thenReturn(Vertex.class);
        when(searchQuery.getPredicates()).thenReturn(predicatesHolder);

        SearchPromiseElementController controller = new SearchPromiseElementController(client,configuration,graph,new EmptyGraphElementSchemaProvider());
        List<Vertex> vertices = Stream.ofAll(() -> (Iterator<Vertex>)controller.search(searchQuery)).toJavaList();

        Assert.assertTrue(vertices.size() == 10);
        Assert.assertTrue(vertices.get(0).id().equals("1"));
        Assert.assertTrue(vertices.get(0).label().equals("promise"));
        Assert.assertTrue(vertices.get(0).getClass().equals(PromiseVertex.class));

        PromiseVertex promiseVertex = (PromiseVertex)vertices.get(0);
        Assert.assertTrue(promiseVertex.getPromise().getId().equals("1"));
        Assert.assertTrue(promiseVertex.getPromise().getClass().equals(IdPromise.class));
    }
}
