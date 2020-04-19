package com.yangdb.fuse.executor.ontology.schema;

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
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.executor.TestSuiteIndexProviderSuite.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class IndexProviderBasedCSVLoaderIT implements BaseITMarker {

    @Test
    public void testSchema() throws IOException {
        Set<String> strings = Arrays.asList("idx_fire_500","idx_freeze_2000","idx_fire_1500","idx_freeze_1000","guilds","own","subjectof","idx_freeze_1500","idx_fire_2000","people","idx_fire_1000","idx_freeze_500","kingdoms","know","originatedin","registeredin","memberof","horses","dragons").stream().collect(Collectors.toSet());
        Assert.assertEquals(strings,StreamSupport.stream(nestedSchema.indices().spliterator(),false).collect(Collectors.toSet()));
    }


    @Test
    public void testLoadDragon() throws IOException, URISyntaxException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        String[] indices = StreamSupport.stream(nestedSchema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider,nestedProviderIfc, nestedSchema, idGeneratorDriver, client);

        Assert.assertEquals(19,indices.length);

        IndexProviderBasedCSVLoader csvLoader = new IndexProviderBasedCSVLoader(client, transformer,nestedSchema);
        // for stand alone test
//        Assert.assertEquals(19,graphLoader.init());

        URL resource = Thread.currentThread().getContextClassLoader().getResource("schema/csv/Dragons.csv");
        LoadResponse<String, FuseError> response = csvLoader.load("Entity","Dragon",new File(resource.toURI()), GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2,response.getResponses().size());
        Assert.assertEquals(3,response.getResponses().get(1).getSuccesses().size());

        RefreshResponse actionGet = client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices("dragons*");
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(3,resp.getHits().getTotalHits().value);

    }
    @Test
    public void testLoadFire() throws IOException, URISyntaxException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(),anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0,1000));

        String[] indices = StreamSupport.stream(nestedSchema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider,nestedProviderIfc, nestedSchema, idGeneratorDriver, client);

        Assert.assertEquals(19,indices.length);

        IndexProviderBasedCSVLoader csvLoader = new IndexProviderBasedCSVLoader(client, transformer,nestedSchema);
        // for stand alone test
//        Assert.assertEquals(19,graphLoader.init());

        URL resource  = Thread.currentThread().getContextClassLoader().getResource("schema/csv/Fire.csv");
        LoadResponse<String, FuseError> response = csvLoader.load("Relation","Fire",new File(resource.toURI()), GraphDataLoader.Directive.INSERT);
        Assert.assertEquals(2,response.getResponses().size());
        Assert.assertEquals(4,response.getResponses().get(1).getSuccesses().size());

        RefreshResponse actionGet = client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        Assert.assertNotNull(actionGet);

        SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices("idx_fire*");
        SearchResponse resp = builder.setSize(1000).setQuery(new MatchAllQueryBuilder()).get();
        Assert.assertEquals(4,resp.getHits().getTotalHits().value);

    }
}
