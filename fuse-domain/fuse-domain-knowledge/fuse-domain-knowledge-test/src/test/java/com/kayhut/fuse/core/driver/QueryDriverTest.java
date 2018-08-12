package com.kayhut.fuse.core.driver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder;
import com.kayhut.fuse.assembly.knowledge.domain.FileBuilder;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder;
import com.kayhut.fuse.dispatcher.driver.QueryDriver;
import com.kayhut.fuse.executor.BaseModuleInjectionTest;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.properties.constraint.NamedParameter;
import com.kayhut.fuse.model.query.properties.constraint.ParameterizedConstraint;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.Property;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.ExecuteStoredQueryRequest;
import com.kayhut.fuse.model.transport.PlanTraceOptions;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import com.sun.xml.internal.bind.api.AccessorException;
import javaslang.collection.Stream;
import javaslang.control.Option;
import org.elasticsearch.client.Client;
import org.jooby.internal.RequestScope;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;

import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.kayhut.fuse.assembly.knowledge.domain.FileBuilder._f;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder._rel;

public class QueryDriverTest extends BaseModuleInjectionTest {
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static KnowledgeWriterContext ctx;
    static FileBuilder f1,f2;

    public void setupData(Client client, RawSchema schema) throws ParseException, JsonProcessingException {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, schema);
        // Efile entities for tests
        String logicalId = ctx.nextLogicalId();
        f1 = _f(ctx.nextFileId()).logicalId(logicalId).name("mazda").path("https://www.google.co.il").mime("string").cat("cars").ctx("family cars")
                .desc("search mazda at google").creationUser("Haim Hania").creationTime(sdf.parse("2012-01-17 03:03:04.827"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2011-04-16 00:00:00.000"))
                .deleteTime(sdf.parse("2018-02-02 02:02:02.222"));
        f2 = _f(ctx.nextFileId()).logicalId(logicalId).name("subaru").path("https://www.google.co.il").mime("string").cat("cars").ctx("family cars")
                .desc("search mazda at google").creationUser("Haim Hania").creationTime(sdf.parse("2012-01-17 03:03:04.827"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2011-04-16 00:00:00.000"))
                .deleteTime(sdf.parse("2018-02-02 02:02:02.222"));
        Assert.assertEquals(2, commit(ctx, INDEX, f1,f2));
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
    public void testCreateAndFetchSingleValue() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new EProp(2, "logicalId", Constraint.of(ConstraintOp.eq, f1.logicalId))
                )).build();

        final CreateQueryRequest createQueryRequest = new CreateQueryRequest("q1", "MyQuery", query, new PlanTraceOptions(),
                new CreateGraphHierarchyCursorRequest(Collections.emptyList(), new CreatePageRequest()));

        final Optional<QueryResourceInfo> info = driver.createAndFetch(createQueryRequest);
        Assert.assertTrue(info.isPresent());
        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().isEmpty());
        Assert.assertEquals(2, ((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0).getEntities().size(), 1);

    }

    @Test
    @Ignore
    //todo fix plan builder for this given query
    public void testCreateAndFetchMultiValueOnRel() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        String creationTime = "2018-07-17 13:19:20.667";
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat("cat").ctx("context1");
        String e1Id = e1.id();
        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat("person").ctx("context1");
        final RelationBuilder rel1 = _rel(ctx.nextRelId()).sideA(e1).sideB(e2).context("context1").cat("rel").creationTime(sdf.parse(creationTime));
        e1.rel(rel1, "out");
        e2.rel(rel1, "in");

        final RelationBuilder rel2 = _rel(ctx.nextRelId()).sideA(e1).sideB(e2).context("context1").cat("bell").creationTime(sdf.parse(creationTime));
        e1.rel(rel2, "out");
        e2.rel(rel2, "in");


        Assert.assertEquals(6, commit(ctx, INDEX, e1, e2));
        Assert.assertEquals(2, commit(ctx, REL_INDEX, rel1,rel2));

        // Based on the knowledge ontology build the V1 query
        Query query = Query.Builder.instance().withName("q1").withOnt("Knowledge").withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", "Entity", e1Id, "A", 2, 0),
                new Rel(2, "relatedEntity", Rel.Direction.L, "", 6, 3),
                new Quant1(3, QuantType.all, Arrays.asList(4, 5 ), 0),
                new RelProp(4, "creationTime", ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("creationTime")), 0),
                new RelProp(5, "category", ParameterizedConstraint.of(ConstraintOp.inSet, new NamedParameter("category")), 0),
                new ETyped(6, "B", "Entity", 0, 0)
                )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(new QueryMetadata(CreateQueryRequest.Type._stored, "q1", "myStoredQuery", 180000), query);
        Assert.assertTrue(resourceInfo.isPresent());

        Optional<QueryResourceInfo> info = driver.call(new ExecuteStoredQueryRequest("callQ1", "q1",
                Arrays.asList(new NamedParameter("creationTime",  creationTime),
                        new NamedParameter("category", Arrays.asList("bell","dell")))));

        // Read Entity (with V1 query)
        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().isEmpty());
        Assignment assignment = ((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0);
        com.kayhut.fuse.model.results.Entity entityA = Stream.ofAll(assignment.getEntities()).find(e -> e.geteTag().contains("A")).get();

        Entity entityB = Stream.ofAll(assignment.getEntities()).find(e -> e.geteTag().contains("B")).get();
        Assert.assertEquals(e1Id, entityA.geteID());
        Assert.assertEquals(e2.id(), entityB.geteID());

        Assert.assertEquals(1, assignment.getRelationships().size());
        Option<Property> category = Stream.ofAll(assignment.getRelationships().get(0).getProperties()).find(p -> p.getpType().equals("category"));
        Assert.assertFalse(category.isEmpty());
        Assert.assertEquals("rel", category.get().getValue());

    }

    @Test
    public void testCallAndFetchSingleParamInSetConstraint() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new EProp(2, "name", ParameterizedConstraint.of(ConstraintOp.inSet, new NamedParameter("name")))
                )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(new QueryMetadata(CreateQueryRequest.Type._stored, "q1", "myStoredQuery", 180000), query);
        Assert.assertTrue(resourceInfo.isPresent());

        Optional<QueryResourceInfo> info = driver.call(new ExecuteStoredQueryRequest("callQ1", "q1",
                Collections.singleton(new NamedParameter("name", Arrays.asList("mazda","subaru")))));

        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().isEmpty());
        Assert.assertEquals(2, ((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0).getEntities().size(), 1);
    }

    @Test
    public void testCallAndFetchMultiParams() throws ParseException, JsonProcessingException {
        init("config/application.test.engine3.m1.dfs.knowledge.public.conf");
        RequestScope requestScope = setup();
        Provider<Client> clientProvider = requestScope.scope(Key.get(Client.class), injector.getProvider(Client.class));
        Provider<RawSchema> schemaProvider = requestScope.scope(Key.get(RawSchema.class), injector.getProvider(RawSchema.class));
        setupData(clientProvider.get(), schemaProvider.get());

        final Provider<QueryDriver> driverScope = requestScope.scope(Key.get(QueryDriver.class), injector.getProvider(QueryDriver.class));
        final QueryDriver driver = driverScope.get();

        Query query = Query.Builder.instance().withName("query").withOnt(KNOWLEDGE)
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "A", "Efile", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 4 ), 0),
                        new EProp(3, "logicalId", ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("logicalId"))),
                        new EProp(4, "name", ParameterizedConstraint.of(ConstraintOp.eq, new NamedParameter("name")))
                )).build();

        final Optional<QueryResourceInfo> resourceInfo = driver.create(new QueryMetadata(CreateQueryRequest.Type._stored, "q1", "myStoredQuery", 180000), query);
        Assert.assertTrue(resourceInfo.isPresent());

        Optional<QueryResourceInfo> info = driver.call(new ExecuteStoredQueryRequest("callQ1", "q1",
                Arrays.asList(new NamedParameter("logicalId", f1.logicalId),new NamedParameter("name", "mazda"))));

        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().isEmpty());
        Assert.assertEquals(1, ((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0).getEntities().size(), 1);
        Assert.assertTrue(((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0).getEntities().get(0).getProperties().contains(new Property("name","raw","mazda")));

    }
}
