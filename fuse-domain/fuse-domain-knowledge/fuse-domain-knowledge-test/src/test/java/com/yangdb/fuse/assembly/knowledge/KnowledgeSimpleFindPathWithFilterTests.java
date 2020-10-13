package com.yangdb.fuse.assembly.knowledge;

import com.typesafe.config.Config;
import com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeConfigManager;
import com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder;
import com.yangdb.fuse.client.BaseFuseClient;
import com.yangdb.fuse.client.FuseClient;
import com.yangdb.fuse.core.driver.BasicIdGenerator;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.services.FuseUtils;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import static com.yangdb.fuse.assembly.knowledge.KnowledgeSimpleFindPathWithFilterTests.Setup.*;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.yangdb.fuse.client.FuseClientSupport.query;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.services.controllers.IdGeneratorController.IDGENERATOR_INDEX;


public class KnowledgeSimpleFindPathWithFilterTests {

    static KnowledgeWriterContext ctx;
    static EntityBuilder e1, e2, e3, e4, e5, e6, e7, e8, e9, e10;
    static RelationBuilder rel1, rel2, rel3, rel4, rel5;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup(true,true);
        //init data
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        // Entities for tests
        e1 = _e(ctx.nextLogicalId()).cat("opel").ctx("context1").creationTime(sdf.parse("2018-01-28 14:33:53.567"))
                .lastUpdateTime(sdf.parse("2014-12-20 12:17:47.791")).deleteTime(sdf.parse("2018-07-09 02:02:02.222"))
                .lastUpdateUser("Kobi").creationUser("Dudi Haim");
        e2 = _e(ctx.nextLogicalId()).cat("opel").ctx("context2").lastUpdateTime(sdf.parse("2017-03-20 12:12:35.111"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222")).creationTime(sdf.parse("2012-04-27 19:38:33.797"))
                .lastUpdateUser("Yael").creationUser("Yael Gabai");
        e3 = _e(ctx.nextLogicalId()).cat("citroen").ctx("context3").lastUpdateUser("Avi Bucavza").creationUser("Yael Gabai")
                .deleteTime(sdf.parse("2018-02-09 02:02:02.222")).lastUpdateTime(sdf.parse("2017-03-20 12:12:35.111"))
                .creationTime(sdf.parse("2012-04-27 19:38:33.797"));
        e4 = _e(e3.logicalId).cat("mazda").ctx("context1").creationUser("Dudi Frid").lastUpdateUser("Avi Bucavza")
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222")).lastUpdateTime(sdf.parse("2025-01-11 23:22:25.221"))
                .creationTime(sdf.parse("2000-01-20 10:08:03.001"));
        e5 = _e(ctx.nextLogicalId()).cat("mitsubishi").ctx("context5").lastUpdateUser("Dudi Frid")
                .creationUser("Kobi Shaul").creationTime(sdf.parse("2018-02-28 23:55:13.899"))
                .lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999")).deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e6 = _e(ctx.nextLogicalId()).cat("lexus").ctx("context6").creationUser("Kobi").lastUpdateUser("Kobi")
                .creationTime(sdf.parse("2018-02-28 23:55:13.899")).lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e7 = _e(ctx.nextLogicalId()).cat("toyota").ctx("context6").creationUser("Haim").lastUpdateUser("Kobi")
                .creationTime(sdf.parse("2018-02-28 23:55:13.899")).lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e8 = _e(ctx.nextLogicalId()).cat("lexus").ctx("context7").lastUpdateUser("Haim").creationUser("Yael Gabai")
                .creationTime(sdf.parse("2018-05-12 13:05:13.000")).lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e9 = _e(ctx.nextLogicalId()).cat("toyota").ctx("context2").lastUpdateUser("Dudi").creationUser("Yael Gabai")
                .creationTime(sdf.parse("2018-05-12 13:05:13.000")).lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        e10 = _e(ctx.nextLogicalId()).cat("toyota").ctx("context10").lastUpdateUser("Kobi").creationUser("Kobi Shaul")
                .creationTime(sdf.parse("2018-05-12 13:05:13.000")).lastUpdateTime(sdf.parse("2016-09-20 01:59:59.999"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));
        // Relation entities for tests
        rel1 = _rel(ctx.nextRelId()).ctx("Car companies").cat("Cars").creationUser("Liat Plesner")
                .lastUpdateUser("Yael Pery").creationTime(sdf.parse("2010-04-31 11:04:29.089"))
                .lastUpdateTime(sdf.parse("2018-01-01 00:39:56.000")).deleteTime(sdf.parse("2018-02-02 22:22:22.222"));
        rel2 = _rel(ctx.nextRelId()).ctx("Car Companies").cat("cars").creationUser("liat plesner")
                .lastUpdateUser("Yael pery").creationTime(sdf.parse("1990-00-00 00:00:00.400"))
                .lastUpdateTime(sdf.parse("2018-01-01 00:39:56.000")).deleteTime(sdf.parse("2018-05-03 19:19:19.192"));
        rel3 = _rel(ctx.nextRelId()).ctx("Number of wheels").cat("Wheels").creationUser("Liat Moshe")
                .lastUpdateUser("yael pery").creationTime(sdf.parse("2010-04-31 11:04:29.089"))
                .lastUpdateTime(sdf.parse("2017-02-29 02:41:41.489")).deleteTime(sdf.parse("2010-09-09 19:19:11.999"));
        rel4 = _rel(ctx.nextRelId()).ctx("Quantity of wheels").cat("wheels").creationUser("Yaacov Gabuy")
                .lastUpdateUser("Meir Pery").creationTime(sdf.parse("1999-01-01 00:00:00.400"))
                .lastUpdateTime(sdf.parse("2017-02-29 02:41:42.489")).deleteTime(sdf.parse("2008-08-08 88:88:88.888"));
        rel5 = _rel(ctx.nextRelId()).ctx("Quantity of Wheels").cat("Wheels").creationUser("Yaacov")
                .lastUpdateUser("Moshe").creationTime(sdf.parse("2009-01-01 00:00:00.400"))
                .lastUpdateTime(sdf.parse("2006-06-07 05:45:55.565")).deleteTime(sdf.parse("2004-02-03 11:11:11.022"));
        // Add Relation between two Entities
        rel1.sideA(e1).sideB(e2);
        e1.rel(rel1, "out");
        e2.rel(rel1, "in");
        rel2.sideA(e3).sideB(e4);
        e3.rel(rel2, "out");
        e4.rel(rel2, "in");
        rel3.sideA(e5).sideB(e6);
        e5.rel(rel3, "out");
        e6.rel(rel3, "in");
        rel4.sideA(e7).sideB(e8);
        e7.rel(rel4, "out");
        e8.rel(rel4, "in");
        rel5.sideA(e9).sideB(e10);
        e9.rel(rel5, "out");
        e10.rel(rel5, "in");

        // Insert Entity and Reference entities to ES
        Assert.assertEquals("error loading data ", 20, commit(ctx, INDEX, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10));
        Assert.assertEquals("error loading data ", 5, commit(ctx, REL_INDEX, rel1, rel2, rel3, rel4, rel5));
    }

    @AfterClass
    public static void after() throws Exception {
        if (ctx != null)
            Assert.assertEquals(25, ctx.removeCreated());
        //tear down
        Setup.tearDown(false,true);

    }

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper

    // Start Tests:
    @Test
    public void testGetSomePathRelationCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("query/findPath.json");
        Query findPathQuery = new com.fasterxml.jackson.databind.ObjectMapper().readValue(resource, Query.class);
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();

        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, findPathQuery);
        // Check if expected results and actual results are equal
        Assert.assertEquals(1, pageData.getSize());

    }

    @Test
    public void testGetSomeRelationCategory() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "Start", "Entity", 2, 0),
                        new Quant1(2, QuantType.some, Arrays.asList(3, 4, 9), 0),
                        new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                        new Rel(4, "hasRelation", R, null, 5, 0),
                        new ETyped(5, "R-1", "Relation", 6, 0),
                        new Rel(6, "hasRelation", L, null, 7),
                        new ETyped(7, "End-1", "Entity", 8, -1),
                        new EProp(8, "category", Constraint.of(ConstraintOp.eq, e2.category)),
                        new Rel(9, "hasRelation", R, null, 10, 0),
                        new ETyped(10, "R-2", "Relation", 11, 0),
                        new Rel(11, "hasRelation", L, null, 12),
                        new ETyped(12, "Middle-2", "Entity", 13, 0),
                        new Rel(13, "hasRelation", R, null, 14, 0),
                        new ETyped(14, "R-3", "Relation", 15, 0),
                        new Rel(15, "hasRelation", L, null, 16),
                        new ETyped(16, "End-2", "Entity", 17, 0),
                        new EProp(17, "category", Constraint.of(ConstraintOp.eq, e2.category))
                )).build();
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query);

        // Check if expected results and actual results are equal
        Assert.assertEquals(1, pageData.getSize());
    }

    public static class Setup {
        public static final Path path = Paths.get("resources", "assembly", "Knowledge", "config", "application.test.engine3.m1.dfs.knowledge.public.conf");
        public static FuseApp app = null;
        public static ElasticEmbeddedNode elasticEmbeddedNode = null;
        public static KnowledgeConfigManager manager = null;
        public static FuseClient fuseClient = null;
        public static TransportClient client = null;

        public static void tearDown(boolean fuse,boolean elastic) throws Exception {
            cleanup(fuse,elastic);
        }

        public static void setup() throws Exception {
            setup(true);
        }

        public static void setup(boolean embedded) throws Exception {
            setup(embedded, true);
        }

        public static void setup(boolean embedded, boolean init) throws Exception {
            init(embedded, init, true);
            fuseClient = new BaseFuseClient("http://localhost:8888/fuse");
        }

        public static void setup(boolean embedded, boolean init, boolean startFuse) throws Exception {
            init(embedded, init, startFuse);
            fuseClient = new BaseFuseClient("http://localhost:8888/fuse");
        }

        public static void setup(boolean embedded, boolean init, boolean startFuse, FuseClient givenFuseClient) throws Exception {
            init(embedded, init, startFuse);
            //set fuse client
            fuseClient = givenFuseClient;
        }

        private static void init(boolean embedded, boolean init, boolean startFuse) throws Exception {
            // Start embedded ES
            if (embedded) {
                elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance("knowledge");
                client = elasticEmbeddedNode.getClient();
                try {
                    new BasicIdGenerator(client, IDGENERATOR_INDEX).init(Arrays.asList("Entity", "Relation", "Evalue", "Rvalue", "workerId"));
                } catch (Exception e) {
                    //probably index already exists
                    System.out.println(e.getMessage());
                }
            } else {
                //use existing running ES
                client = elasticEmbeddedNode.getClient("knowledge", 9300);
            }
            // Load fuse engine config file
            String confFilePath = path.toString();
            // Start elastic data manager
            manager = new KnowledgeConfigManager(confFilePath, client);
            // Connect to elastic
            // Create indexes by templates
            if (init) {
                manager.init();
            }

            //load configuration
            Config config = FuseUtils.loadConfig(new File(confFilePath), "activeProfile");
            String[] joobyArgs = new String[]{
                    "logback.configurationFile=" + Paths.get("src", "test", "resources", "config", "logback.xml").toString(),
                    "server.join=false"
            };


            // Start fuse app (based on Jooby app web server)
            if (startFuse) {
                app = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                        .conf(path.toFile(), "activeProfile");
                app.start("server.join=false");
            }
        }

        public static void cleanup(boolean fuse, boolean elastic) throws Exception {
            if (manager != null) {
                manager.drop();
            }
            if (fuse) {
                if (app != null) {
                    app.stop();
                }
            }
            if (elastic) {
                if (elasticEmbeddedNode != null)
                    elasticEmbeddedNode.close();
            }
        }
    }
}
