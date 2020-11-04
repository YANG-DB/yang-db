package com.yangdb.fuse.core.driver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.yangdb.fuse.assembly.knowledge.domain.*;
import com.yangdb.fuse.dispatcher.driver.QueryDriver;
import com.yangdb.fuse.executor.BaseModuleInjectionTest;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.results.Entity;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.cursor.CreatePathsCursorRequest;
import org.elasticsearch.client.Client;
import org.jooby.internal.RequestScope;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.yangdb.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.yangdb.fuse.assembly.knowledge.domain.FileBuilder._f;
import static com.yangdb.fuse.assembly.KNOWLEDGE.KNOWLEDGE;
import static com.yangdb.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.yangdb.fuse.assembly.knowledge.domain.ValueBuilder._v;
import static com.yangdb.fuse.model.query.Rel.Direction.L;
import static com.yangdb.fuse.model.query.Rel.Direction.R;

public class UnionQueryDriverTest extends BaseModuleInjectionTest {
    static private SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
    static KnowledgeWriterContext ctx;
    static FileBuilder f1, f2;
    static EntityBuilder e1;
    static ValueBuilder v1,v2;

    public void setupData(Client client, RawSchema schema) throws ParseException, JsonProcessingException {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, schema);

        String logicalId = ctx.nextLogicalId();
        f1 = _f(ctx.nextFileId()).logicalId(logicalId).name("mazda").path("https://www.google.co.il").mime("string").cat("cars").ctx("family cars")
                .desc("search mazda at google").creationUser("Haim Hania").creationTime(sdf.parse("2012-01-17 03:03:04.827"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2011-04-16 00:00:00.000"))
                .deleteTime(sdf.parse("2018-02-02 02:02:02.222"));
        f2 = _f(ctx.nextFileId()).logicalId(logicalId).name("subaru").path("https://www.google.co.il").mime("string").cat("cars").ctx("family cars")
                .desc("search mazda at google").creationUser("Haim Hania").creationTime(sdf.parse("2012-01-17 03:03:04.827"))
                .lastUpdateUser("Dudi Fargon").lastUpdateTime(sdf.parse("2011-04-16 00:00:00.000"))
                .deleteTime(sdf.parse("2018-02-02 02:02:02.222"));

        e1 = _e(ctx.nextLogicalId()).cat("opel").ctx("context1").creationTime(sdf.parse("2018-01-28 14:33:53.567"))
                .deleteTime(sdf.parse("2018-07-09 02:02:02.222"));

        v1 = _v(ctx.nextValueId()).field("Car sale").value("Chevrolet").bdt("identifier").ctx("sale")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu peretz").creationTime(sdf.parse("2018-07-12 09:01:03.763"))
                .deleteTime(sdf.parse("2017-10-10 09:09:09.090"));
        v2 = _v(ctx.nextValueId()).field("garage").value("Zion and his sons").bdt("identifier").ctx("fixing cars")
                .creationUser("kobi Shaul").lastUpdateUser("Dudu Peretz").creationTime(new Date(System.currentTimeMillis()));

        // Add Evalue to Entity
        e1.value(v1);
        e1.value(v2);

        // Insert Entity and Evalue entities to ES
        Assert.assertEquals(1, commit(ctx, INDEX, e1));
        Assert.assertEquals(2, commit(ctx, INDEX, v1, v2));
    }

    @Test
    public void testCallAndFetchMultiValueOnRel() throws ParseException, JsonProcessingException {
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
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.some, Arrays.asList(3, 4,7), 0),
                            new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                            new Rel(4, "hasEvalue", R, null, 5, 0),
                                    new ETyped(5, "V1", "Evalue", 6, 0),
                                    new EProp(6, "fieldId", Constraint.of(ConstraintOp.eq, v1.fieldId)),
                            new Rel(7, "hasEvalue", R, null, 8, 0),
                                    new ETyped(8, "V2", "Evalue", 9, 0),
                                    new EProp(9, "fieldId", Constraint.of(ConstraintOp.eq, v2.fieldId)))
                ).build();



        final Optional<QueryResourceInfo> info = driver.create(
                new CreateQueryRequest("q1", "myStoredQuery", query,
                        new CreatePathsCursorRequest(new CreatePageRequest())));
        Assert.assertTrue(info.isPresent());
        Assert.assertTrue(info.get().getError()==null);

        // Read Entity (with V1 query)
        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().isEmpty());
        Assert.assertEquals(3,((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().size());
        Assignment<Entity,Relationship> assignment1 = ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0);
        Assignment<Entity,Relationship> assignment2 = ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(1);
        Assignment<Entity,Relationship> assignment3 = ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(2);
        Assert.assertEquals(1,assignment1.getEntities().size());
        Assert.assertEquals(2,assignment2.getEntities().size());
        Assert.assertEquals(1,assignment2.getRelationships().size());
        Assert.assertEquals(2,assignment3.getEntities().size());
        Assert.assertEquals(1,assignment3.getRelationships().size());


    }
    @Test
    public void testCallAndFetchMultiValueOnRelInnerUnion() throws ParseException, JsonProcessingException {
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
                        new ETyped(1, "A", "Entity", 2, 0),
                        new Quant1(2, QuantType.some, Arrays.asList(3, 4,7), 0),
                            new EProp(3, "category", Constraint.of(ConstraintOp.eq, e1.category)),
                            new Rel(4, "hasEvalue", R, null, 10, 0),
                                    new ETyped(10, "V1", "Evalue", 11, 0),
                                    new Quant1(11, QuantType.some, Arrays.asList(12, 13), 0),
                                        new EProp(12, "fieldId", Constraint.of(ConstraintOp.eq, v1.fieldId)),
                                        new Rel(13, "hasEvalue", L, null, 14, 0),
                                            new ETyped(14, "A2", "Entity", 0, 0),
                            new Rel(7, "hasEvalue", R, null, 8, 0),
                                    new ETyped(8, "V2", "Evalue", 9, 0),
                                    new EProp(9, "fieldId", Constraint.of(ConstraintOp.eq, v2.fieldId)))
                ).build();



        final Optional<QueryResourceInfo> info = driver.create(
                new CreateQueryRequest("q1", "myStoredQuery", query,
                        new CreatePathsCursorRequest(new CreatePageRequest())));
        Assert.assertTrue(info.isPresent());
        Assert.assertTrue(info.get().getError()==null);

        // Read Entity (with V1 query)
        Assert.assertFalse(info.get().getCursorResourceInfos().isEmpty());
        Assert.assertFalse(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty());
        Assert.assertTrue(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).isAvailable());
        Assert.assertNotNull(info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData());
        Assert.assertFalse(((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().isEmpty());
        Assert.assertEquals(5,((AssignmentsQueryResult) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().size());

        Assignment<Entity,Relationship> assignment1 = ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(0);
        Assignment<Entity,Relationship> assignment2 = ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(1);
        Assignment<Entity,Relationship> assignment3 = ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(2);
        Assignment<Entity,Relationship> assignment4 = ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(3);
        Assignment<Entity,Relationship> assignment5 = ((AssignmentsQueryResult<Entity,Relationship>) info.get().getCursorResourceInfos().get(0).getPageResourceInfos().get(0).getData()).getAssignments().get(4);

        Assert.assertEquals(1,assignment1.getEntities().size());

        Assert.assertEquals(2,assignment2.getEntities().size());
        Assert.assertEquals(1,assignment2.getRelationships().size());

        Assert.assertEquals(2,assignment3.getEntities().size());
        Assert.assertEquals(2,assignment3.getRelationships().size());

        Assert.assertEquals(2,assignment4.getEntities().size());
        Assert.assertEquals(2,assignment4.getRelationships().size());


        Assert.assertEquals(2,assignment5.getEntities().size());
        Assert.assertEquals(1,assignment5.getRelationships().size());

    }

}
