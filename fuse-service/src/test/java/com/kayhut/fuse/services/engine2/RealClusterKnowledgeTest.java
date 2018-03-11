package com.kayhut.fuse.services.engine2;

import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.services.engine2.data.util.FuseClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

public class RealClusterKnowledgeTest {

    @Test
    @Ignore
    public void testAdvancedSearch() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 9, 14, 28), 0),
                new EProp(3, "context", Constraint.of(ConstraintOp.eq, "globAL")),
                new Rel(4, $ont.rType$("hasEvalue"), R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$("Evalue"), 6, 0),
                new Quant1(6, QuantType.all, Arrays.asList(7, 8, 29), 0),
                new EProp(7, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "title")),
                new EProp(8, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "*")),
                new Rel(9, $ont.rType$("hasEvalue"), R, null, 10, 0),
                new ETyped(10, "B", $ont.eType$("Evalue"), 11, 0),
                new Quant1(11, QuantType.all, Arrays.asList(12, 13, 30), 0),
                new EProp(12, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "nicknames")),
                new EProp(13, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "***")),
                new Rel(14, $ont.rType$("hasEntity"), L, null, 15, 0),
                new ETyped(15, "B", $ont.eType$("LogicalEntity"), 16, 0),
                new Rel(16, $ont.rType$("hasEntity"), R, null, 17, 0),
                new ETyped(17, "B", $ont.eType$("Entity"), 18, 0),
                new Quant1(18, QuantType.all, Arrays.asList(19, 20, 21, 22, 31), 0),
                new EProp(19, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "global")),
                new EProp(20, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new EProp(21, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "balla")),
                new OptionalComp(22, 23),
                new Rel(23, $ont.rType$("hasEvalue"), R, null, 24, 0),
                new ETyped(24, "B", $ont.eType$("Evalue"), 25, 0),
                new Quant1(25, QuantType.all, Arrays.asList(26, 27, 32), 0),
                new EProp(26, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "description")),
                new EProp(27, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "*")),

                new EProp(28, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(29, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(30, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(31, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(32, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty))
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), CreateCursorRequest.CursorType.graph);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        String plan = fuseClient.getPlan(queryResourceInfo.getExplainPlanUrl() + "/print");
        Assert.assertEquals(plan, "{\"@type\":\"com.kayhut.fuse.model.transport.ContentResponse\",\"status\":{\"value\":200,\"reason\":\"Success\",\"name\":\"OK\"},\"id\":\"a6437dcf-2ff4-4e18-88ef-0203d22bb0cf\",\"data\":\"" +
                "[cost:DoubleCost{estimation=20.0}" +
                "\\n, ──Typ[Entity:17]──?[19]:[context<eq,global>, context<eq,context1>, category<eq,balla>, deleteTime<empty,null>, logicalId<IdentityProjection>, context<IdentityProjection>, category<IdentityProjection>, authorization<IdentityProjection>, lastUpdateUser<IdentityProjection>, lastUpdateTime<IdentityProjection>, creationTime<IdentityProjection>, creationUser<IdentityProjection>, deleteTime<IdentityProjection>, deleteUser<IdentityProjection>], [, " +
                "\\n          └─Opt[17]--> Rel(hasEvalue:23)──?[2301]:[fieldId<eq,description>, deleteTime<empty,null>, logicalId<IdentityProjection>, context<IdentityProjection>, authorization<IdentityProjection>, lastUpdateUser<IdentityProjection>, lastUpdateTime<IdentityProjection>, creationTime<IdentityProjection>, creationUser<IdentityProjection>, deleteTime<IdentityProjection>, deleteUser<IdentityProjection>, fieldId<IdentityProjection>, bdt<IdentityProjection>, stringValue<IdentityProjection>, dateValue<IdentityProjection>, intValue<IdentityProjection>]──Typ[Evalue:24]──?[26]:[]]-<--Rel(hasEntity:16)──?[1601]:[]──Typ[LogicalEntity:15]──?[1501]:[]--> Rel(hasEntity:14)──?[1401]:[context<eq,globAL>, deleteTime<empty,null>, logicalId<IdentityProjection>, context<IdentityProjection>, category<IdentityProjection>, authorization<IdentityProjection>, lastUpdateUser<IdentityProjection>, lastUpdateTime<IdentityProjection>, creationTime<IdentityProjection>, creationUser<IdentityProjection>, deleteTime<IdentityProjection>, deleteUser<IdentityProjection>]──Typ[Entity:1]──?[3]:[]--> Rel(hasEvalue:4)──?[401]:[fieldId<eq,title>, deleteTime<empty,null>, logicalId<IdentityProjection>, context<IdentityProjection>, authorization<IdentityProjection>, lastUpdateUser<IdentityProjection>, lastUpdateTime<IdentityProjection>, creationTime<IdentityProjection>, creationUser<IdentityProjection>, deleteTime<IdentityProjection>, deleteUser<IdentityProjection>, fieldId<IdentityProjection>, bdt<IdentityProjection>, stringValue<IdentityProjection>, dateValue<IdentityProjection>, intValue<IdentityProjection>]──Typ[Evalue:5]──?[7]:[], " +
                "\\n          └─goTo[17]--> Rel(hasEvalue:9)──?[901]:[fieldId<eq,nicknames>, deleteTime<empty,null>, logicalId<IdentityProjection>, context<IdentityProjection>, authorization<IdentityProjection>, lastUpdateUser<IdentityProjection>, lastUpdateTime<IdentityProjection>, creationTime<IdentityProjection>, creationUser<IdentityProjection>, deleteTime<IdentityProjection>, deleteUser<IdentityProjection>, fieldId<IdentityProjection>, bdt<IdentityProjection>, stringValue<IdentityProjection>, dateValue<IdentityProjection>, intValue<IdentityProjection>]──Typ[Evalue:10]──?[12]:[], " +
                "\\n                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  └─goTo[1]]\"}");
    }

    @Test
    @Ignore
    public void testAdvancedSearchFieldId() throws IOException, InterruptedException {
        FuseClient fuseClient = new FuseClient("http://localhost:8888/fuse");
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        Query query = Query.Builder.instance().withName("q2").withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 2, 0),
                new Quant1(2, QuantType.all, Arrays.asList(3, 4, 9, 14, 28), 0),
                new EProp(3, "context", Constraint.of(ConstraintOp.eq, "globAL")),
                new Rel(4, $ont.rType$("hasEvalue"), R, null, 5, 0),
                new ETyped(5, "B", $ont.eType$("Evalue"), 6, 0),
                new Quant1(6, QuantType.all, Arrays.asList(7, 8, 29), 0),
                new EProp(7, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "title")),
                new EProp(8, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "a*")),
                new Rel(9, $ont.rType$("hasEvalue"), R, null, 10, 0),
                new ETyped(10, "B", $ont.eType$("Evalue"), 11, 0),
                new Quant1(11, QuantType.all, Arrays.asList(12, 13, 30), 0),
                new EProp(12, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "nicknames")),
                new EProp(13, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "***")),
                new Rel(14, $ont.rType$("hasEntity"), L, null, 15, 0),
                new ETyped(15, "B", $ont.eType$("LogicalEntity"), 16, 0),
                new Rel(16, $ont.rType$("hasEntity"), R, null, 17, 0),
                new ETyped(17, "B", $ont.eType$("Entity"), 18, 0),
                new Quant1(18, QuantType.all, Arrays.asList(19, 20, 21, 22, 31), 0),
                new EProp(19, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "global")),
                new EProp(20, $ont.pType$("context"), Constraint.of(ConstraintOp.eq, "context1")),
                new EProp(21, $ont.pType$("category"), Constraint.of(ConstraintOp.eq, "balla")),
                new OptionalComp(22, 23),
                new Rel(23, $ont.rType$("hasEvalue"), R, null, 24, 0),
                new ETyped(24, "B", $ont.eType$("Evalue"), 25, 0),
                new Quant1(25, QuantType.all, Arrays.asList(26, 27, 32), 0),
                new EProp(26, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "description")),
                new EProp(27, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, "*")),

                new EProp(28, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(29, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(30, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(31, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty)),
                new EProp(32, $ont.pType$("deleteTime"), Constraint.of(ConstraintOp.empty))
        )).build();


        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), CreateCursorRequest.CursorType.graph);
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        String plan = fuseClient.getPlan(queryResourceInfo.getExplainPlanUrl() + "/print");
        Assert.assertEquals(plan, "{\"@type\":\"com.kayhut.fuse.model.transport.ContentResponse\",\"status\":{\"value\":200,\"reason\":\"Success\",\"name\":\"OK\"},\"id\":\"9baf8a0b-c08c-409b-b621-6ba9f033a787\",\"data\":\"" +
                "[cost:DoubleCost{estimation=19.0}" +
                "\\n, ──Typ[Evalue:5]──?[7]:[fieldId<eq,title>, deleteTime<empty,null>, stringValue<like,a*>, logicalId<IdentityProjection>, context<IdentityProjection>, authorization<IdentityProjection>, lastUpdateUser<IdentityProjection>, lastUpdateTime<IdentityProjection>, creationTime<IdentityProjection>, creationUser<IdentityProjection>, deleteTime<IdentityProjection>, deleteUser<IdentityProjection>, fieldId<IdentityProjection>, bdt<IdentityProjection>, stringValue<IdentityProjection>, dateValue<IdentityProjection>, intValue<IdentityProjection>]-<--Rel(hasEvalue:4)──?[401]:[]──Typ[Entity:1]──?[3]:[context<eq,globAL>, deleteTime<empty,null>, logicalId<IdentityProjection>, context<IdentityProjection>, category<IdentityProjection>, authorization<IdentityProjection>, lastUpdateUser<IdentityProjection>, lastUpdateTime<IdentityProjection>, creationTime<IdentityProjection>, creationUser<IdentityProjection>, deleteTime<IdentityProjection>, deleteUser<IdentityProjection>]--> Rel(hasEvalue:9)──?[901]:[fieldId<eq,nicknames>, deleteTime<empty,null>, logicalId<IdentityProjection>, context<IdentityProjection>, authorization<IdentityProjection>, lastUpdateUser<IdentityProjection>, lastUpdateTime<IdentityProjection>, creationTime<IdentityProjection>, creationUser<IdentityProjection>, deleteTime<IdentityProjection>, deleteUser<IdentityProjection>, fieldId<IdentityProjection>, bdt<IdentityProjection>, stringValue<IdentityProjection>, dateValue<IdentityProjection>, intValue<IdentityProjection>]──Typ[Evalue:10]──?[12]:[], " +
                "\\n                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              └─goTo[1]-<--Rel(hasEntity:14)──?[1401]:[]──Typ[LogicalEntity:15]──?[1501]:[]--> Rel(hasEntity:16)──?[1601]:[context<eq,global>, context<eq,context1>, category<eq,balla>, deleteTime<empty,null>, logicalId<IdentityProjection>, context<IdentityProjection>, category<IdentityProjection>, authorization<IdentityProjection>, lastUpdateUser<IdentityProjection>, lastUpdateTime<IdentityProjection>, creationTime<IdentityProjection>, creationUser<IdentityProjection>, deleteTime<IdentityProjection>, deleteUser<IdentityProjection>]──Typ[Entity:17]──?[19]:[], [, " +
                "\\n                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    └─Opt[17]--> Rel(hasEvalue:23)──?[2301]:[fieldId<eq,description>, deleteTime<empty,null>, logicalId<IdentityProjection>, context<IdentityProjection>, authorization<IdentityProjection>, lastUpdateUser<IdentityProjection>, lastUpdateTime<IdentityProjection>, creationTime<IdentityProjection>, creationUser<IdentityProjection>, deleteTime<IdentityProjection>, deleteUser<IdentityProjection>, fieldId<IdentityProjection>, bdt<IdentityProjection>, stringValue<IdentityProjection>, dateValue<IdentityProjection>, intValue<IdentityProjection>]──Typ[Evalue:24]──?[26]:[]]]\"}");
    }
}
