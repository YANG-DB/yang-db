package com.yangdb.cyber.ontology.schema;

import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.schema.load.CSVTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.IndexProviderBasedCSVLoader;
import com.yangdb.fuse.executor.ontology.schema.load.LoadResponse;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.test.BaseITMarker;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.yangdb.cyber.ontology.CyberTestSuiteIndexProviderSuite.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class CyberIndexProviderBasedCSVLoaderIT implements BaseITMarker {
    public static final String CYBER = "Cyber";

    @Before
    public void setUp() throws Exception {
        setup(false, CYBER);//todo remove remark when running IT tests
    }

    @Test
    public void testSchema() {
        Set<String> strings = Stream.of("events_analysis", "behaviors", "alertstobehaviors", "lov_cyberobjecttypes", "traces", "traceevents", "behavior_to_behavior", "tracestobehaviors", "enrichmentevents", "behaviorentities", "lov_behaviorstypes", "alerts", "entities", "entitiesprocessandfiles", "lov_eventstypes", "traceentities").collect(Collectors.toSet());
        Assert.assertEquals(strings, StreamSupport.stream(schema.indices().spliterator(), false).collect(Collectors.toSet()));
    }


    @Test
    public void testLoadTraces() throws IOException, URISyntaxException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        String[] indices = StreamSupport.stream(schema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerFactory, schema, idGeneratorDriver, client);

        Assert.assertEquals(17, indices.length);

        IndexProviderBasedCSVLoader csvLoader = new IndexProviderBasedCSVLoader(client, transformer, providerFactory, schema);
        // for stand alone test
//        Assert.assertEquals(19,graphLoader.init());

        URL resource = Thread.currentThread().getContextClassLoader().getResource("sample/data/Traces.csv");
        LoadResponse<String, FuseError> response = csvLoader.load("Entity", "traces", new File(resource.toURI()), GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2, response.getResponses().size());
        Assert.assertEquals(31, response.getResponses().get(1).getSuccesses().size());

        RefreshResponse actionGet = client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices("traces");
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(31, resp.getHits().getTotalHits());

    }

    @Test
    public void testLoadTraceToBehaviors() throws IOException, URISyntaxException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        String[] indices = StreamSupport.stream(schema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerFactory, schema, idGeneratorDriver, client);

        Assert.assertEquals(17, indices.length);

        IndexProviderBasedCSVLoader csvLoader = new IndexProviderBasedCSVLoader(client, transformer, providerFactory, schema);
        // for stand alone test
//        Assert.assertEquals(19,graphLoader.init());

        URL resource = Thread.currentThread().getContextClassLoader().getResource("sample/data/TraceToBehaviors.csv");
        LoadResponse<String, FuseError> response = csvLoader.load("Relation", "tracestobehaviors", new File(resource.toURI()), GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2, response.getResponses().size());
        Assert.assertEquals(2374, response.getResponses().get(1).getSuccesses().size());

        RefreshResponse actionGet = client.admin().indices().refresh(new RefreshRequest("_all")).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices("tracestobehaviors");
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(2374, resp.getHits().getTotalHits());

    }
}
