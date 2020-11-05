package com.yangdb.fuse.executor.ontology.schema;

import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.schema.load.EntityTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.IndexProviderBasedGraphLoader;
import com.yangdb.fuse.executor.ontology.schema.load.LoadResponse;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.test.BaseITMarker;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.executor.TestSuiteIndexProviderSuite.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class IndexProviderBasedGraphLoaderIT implements BaseITMarker {


    @Test
    public void testSchema() throws IOException {
        Set<String> strings = Arrays.asList("idx_fire_500","idx_freeze_2000","idx_fire_1500","idx_freeze_1000","guilds","own","subjectof","idx_freeze_1500","idx_fire_2000","people","idx_fire_1000","idx_freeze_500","kingdoms","know","originatedin","registeredin","memberof","horses","dragons").stream().collect(Collectors.toSet());
        Assert.assertEquals(strings,StreamSupport.stream(nestedSchema.indices().spliterator(),false).collect(Collectors.toSet()));
    }

    @Test
    public void testLoad() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        String[] indices = StreamSupport.stream(nestedSchema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        EntityTransformer transformer = new EntityTransformer(config, ontologyProvider,nestedProviderIfc, nestedSchema, idGeneratorDriver, client);

        Assert.assertEquals(19,indices.length);

        IndexProviderBasedGraphLoader graphLoader = new IndexProviderBasedGraphLoader(client, transformer,nestedSchema, idGeneratorDriver);
        // for stand alone test
//        Assert.assertEquals(19,graphLoader.init());

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/LogicalDragonsGraph.json");
        LogicalGraphModel graphModel = mapper.readValue(stream, LogicalGraphModel.class);
        LoadResponse<String, FuseError> response = graphLoader.load(ontology.getOnt(), graphModel, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2,response.getResponses().size());

        Assert.assertEquals(64,response.getResponses().get(0).getSuccesses().size());
        Assert.assertEquals(64,response.getResponses().get(1).getSuccesses().size());

        Assert.assertEquals(0,response.getResponses().get(0).getFailures().size());
        Assert.assertEquals(0,response.getResponses().get(1).getFailures().size());


        RefreshResponse actionGet = client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices(indices);
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(graphModel.getNodes().size() + 2*graphModel.getEdges().size(),resp.getHits().getTotalHits().value);

    }

    @Test
    public void testLoadWithNestedData() throws IOException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        String[] indices = StreamSupport.stream(nestedSchema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        EntityTransformer transformer = new EntityTransformer(config, ontologyProvider,nestedProviderIfc, nestedSchema, idGeneratorDriver, client);

        Assert.assertEquals(19,indices.length);

        IndexProviderBasedGraphLoader graphLoader = new IndexProviderBasedGraphLoader(client, transformer,nestedSchema, idGeneratorDriver);
        // for stand alone test
//        Assert.assertEquals(19,graphLoader.init());

        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/LogicalDragonsGraphWithNested.json");
        LogicalGraphModel graphModel = mapper.readValue(stream, LogicalGraphModel.class);
        LoadResponse<String, FuseError> response = graphLoader.load(ontology.getOnt(), graphModel, GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2,response.getResponses().size());

        Assert.assertEquals(64,response.getResponses().get(0).getSuccesses().size());
        Assert.assertEquals(64,response.getResponses().get(1).getSuccesses().size());

        Assert.assertEquals(0,response.getResponses().get(0).getFailures().size());
        Assert.assertEquals(0,response.getResponses().get(1).getFailures().size());


        RefreshResponse actionGet = client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices(indices);
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(graphModel.getNodes().size() + 2*graphModel.getEdges().size(),resp.getHits().getTotalHits().value);

    }
}
