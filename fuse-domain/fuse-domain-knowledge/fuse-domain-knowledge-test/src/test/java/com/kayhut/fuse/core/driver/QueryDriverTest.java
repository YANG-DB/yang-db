package com.kayhut.fuse.core.driver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.kayhut.fuse.assembly.knowledge.domain.FileBuilder;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeConfigManager;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.dispatcher.driver.QueryDriver;
import com.kayhut.fuse.executor.BaseModuleInjectionTest;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.PlanTraceOptions;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.jooby.internal.RequestScope;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;

import static com.kayhut.fuse.assembly.knowledge.Setup.client;
import static com.kayhut.fuse.assembly.knowledge.Setup.manager;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.FileBuilder._f;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;

public class QueryDriverTest extends BaseModuleInjectionTest {
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static KnowledgeWriterContext ctx;
    static FileBuilder f1;

    public void setupData(Client client,RawSchema schema) throws ParseException, JsonProcessingException {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, schema);
        // Efile entities for tests
        f1 = _f(ctx.nextFileId()).logicalId(ctx.nextLogicalId()).name("mazda").path("https://www.google.co.il").mime("string").cat("cars").ctx("family cars")
                .desc("search mazda at google").creationUser("Haim Hania").creationTime(sdf.parse("2012-01-17 03:03:04.827"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2011-04-16 00:00:00.000"))
                .deleteTime(sdf.parse("2018-02-02 02:02:02.222"));
        Assert.assertEquals(1, commit(ctx, INDEX, f1));
    }

    @Test
    public void testGetInfo() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();
        final Optional<StoreResourceInfo> info = driver.getInfo();

        Assert.assertTrue(info.isPresent());
        Assert.assertTrue(info.get().getResourceUrl().endsWith("/fuse/query"));

    }

    @Test
    public void testCreateAndFetch() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(),schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new EProp(2, "logicalId", Constraint.of(ConstraintOp.eq, f1.logicalId))
                )).build();

        final CreateQueryRequest createQueryRequest = new CreateQueryRequest("q1", "MyQuery", query,new PlanTraceOptions(),
                new CreateGraphHierarchyCursorRequest(Collections.emptyList(),new CreatePageRequest()));

        final Optional<QueryResourceInfo> info = driver.createAndFetch(createQueryRequest);
        Assert.assertTrue(info.isPresent());
        Assert.assertTrue(info.get().getResourceUrl().endsWith("/fuse/query"));

    }
}
