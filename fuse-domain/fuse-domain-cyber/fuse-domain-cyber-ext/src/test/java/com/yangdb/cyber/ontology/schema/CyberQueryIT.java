package com.yangdb.cyber.ontology.schema;

import com.github.sisyphsu.dateparser.DateParser;
import com.yangdb.cyber.ontology.CyberTestSuiteIndexProviderSuite;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.executor.ontology.schema.load.CSVTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.IndexProviderBasedCSVLoader;
import com.yangdb.fuse.executor.ontology.schema.load.LoadResponse;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.projection.IdentityProjection;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.resourceInfo.ResultResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.test.BaseITMarker;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.junit.*;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.yangdb.cyber.ontology.CyberTestSuiteIndexProviderSuite.*;
import static com.yangdb.cyber.ontology.CyberTestSuiteIndexProviderSuite.setup;
import static com.yangdb.fuse.client.FuseClientSupport.query;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class CyberQueryIT implements BaseITMarker {
    public static final String CYBER = "Cyber";
    static private SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
    static private DateParser parser = DateParser.newBuilder().build();

    @BeforeClass
    public static void setup() throws Exception {
        CyberTestSuiteIndexProviderSuite.setup(false, CYBER);//todo remove remark when running IT tests
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @AfterClass
    public static void after() {
//        Setup.cleanup();
        if (app != null) {
            app.stop();
        }
    }



    @Test
    public void testQueryTraces() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Assert.assertNotNull(fuseResourceInfo);


        Query query = Query.Builder.instance().withName("query").withOnt(CYBER)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "trace", "traces", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5)),
                        new EProp(3, "trace_status", Constraint.of(ConstraintOp.eq,0)),
                        new EProp(4, "trace_type", Constraint.of(ConstraintOp.eq,"Sequence based")),
                        new EProp(5, "status_update_time", Constraint.of(ConstraintOp.ge,parser.parseDate("2018-10-01 11:42")))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(17, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());
    }

    @Test
    public void testQueryBehaviors() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Assert.assertNotNull(fuseResourceInfo);


        Query query = Query.Builder.instance().withName("query").withOnt(CYBER)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "behavior", "behaviors", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4, 5)),
                        new EProp(3, "by_type_id", Constraint.of(ConstraintOp.eq,1)),
                        new EProp(4, "by_name", Constraint.of(ConstraintOp.like,"*.exe")),
                        new EProp(5, "insert_time", Constraint.of(ConstraintOp.ge,parser.parseDate("2018-10-01 11:42")))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, 1000, query);

        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
        Assert.assertEquals(472, ((Assignment) ((AssignmentsQueryResult) pageData).getAssignments().get(0)).getEntities().size());

    }

    @Test
    public void testQueryTraceToBehaviors() throws IOException, URISyntaxException {
        IdGeneratorDriver<Range> idGeneratorDriver = Mockito.mock(IdGeneratorDriver.class);
        when(idGeneratorDriver.getNext(anyString(), anyInt()))
                .thenAnswer(invocationOnMock -> new Range(0, 1000));

        String[] indices = StreamSupport.stream(schema.indices().spliterator(), false).map(String::toLowerCase).collect(Collectors.toSet()).toArray(new String[]{});
        CSVTransformer transformer = new CSVTransformer(config, ontologyProvider, providerFactory, schema, idGeneratorDriver, client);

        Assert.assertEquals(11, indices.length);

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
