package com.kayhut.fuse.assembly.knowledge.service;

import com.kayhut.fuse.assembly.knowledge.KnowledgeGraphHierarchyCursorRequest;
import com.kayhut.fuse.assembly.knowledge.RankingKnowledgeDataInfraManager;
import com.kayhut.fuse.assembly.knowledge.Setup;
import com.kayhut.fuse.model.execution.plan.PlanAssert;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.QueryResultAssert;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import com.kayhut.fuse.test.framework.index.ElasticEmbeddedNode;
import org.jooby.Jooby;
import org.junit.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kayhut.fuse.assembly.knowledge.Setup.fuseClient;
import static com.kayhut.fuse.model.OntologyTestUtils.NAME;

public class RankingScoreBasedE2ETests {
    public static final String KNOWLEDGE = "knowledge";
    private static Jooby app;
    public static String CONFIG_PATH = Paths.get("resources","assembly","Knowledge","config", "application.test.engine3.m1.dfs.knowledge-test.public.conf").toString();

    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("RankingScoreBasedE2ETests - setup");

        Setup.setup();

        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        $ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Knowledge"));

        manager = new RankingKnowledgeDataInfraManager(CONFIG_PATH, ElasticEmbeddedNode.getClient(KNOWLEDGE,9300));
        manager.init();
        manager.load();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("RankingScoreBasedE2ETests - teardown");

        if(manager != null){
            manager.drop();
        }
    }

    //endregion


    private void runQueryAndValidate(Query query, AssignmentsQueryResult expectedAssignmentsQueryResult, Plan expectedPlan) throws IOException, InterruptedException {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        Plan actualPlan = fuseClient.getPlanObject(queryResourceInfo.getExplainPlanUrl());
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) fuseClient.getPageData(pageResourceInfo.getDataUrl());
        PlanAssert.assertEquals(expectedPlan, actualPlan);


        QueryResultAssert.assertEquals(expectedAssignmentsQueryResult, actualAssignmentsQueryResult, shouldIgnoreRelId());
    }

    private AssignmentsQueryResult runQuery(Query query, Iterable<String> tags) throws IOException, InterruptedException {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query);
        Plan actualPlan = fuseClient.getPlanObject(queryResourceInfo.getExplainPlanUrl());
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new KnowledgeGraphHierarchyCursorRequest(tags));
        PageResourceInfo pageResourceInfo = fuseClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = fuseClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) fuseClient.getPageData(pageResourceInfo.getDataUrl());



        return actualAssignmentsQueryResult;
    }


    @Test
    public void testMotiEqNick() throws IOException, InterruptedException {
        Query query = getByNicknamesEq("moti");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(2, globalEntitiesSorted.size());
        Assert.assertEquals("e00000002.global", globalEntitiesSorted.get(0).geteID());
        Assert.assertEquals("e00000001.global", globalEntitiesSorted.get(1).geteID());
    }

    @Test
    public void testMotiEqTitle() throws IOException, InterruptedException {
        Query query = getByTitleEq("moti");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(1, globalEntitiesSorted.size());
        Assert.assertEquals("e00000002.global", globalEntitiesSorted.get(0).geteID());
    }


    @Test
    public void testMotiLikeNoWildcard() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("moti");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(2, globalEntitiesSorted.size());
        Assert.assertEquals("e00000002.global", globalEntitiesSorted.get(0).geteID());
        Assert.assertEquals("e00000001.global", globalEntitiesSorted.get(1).geteID());
    }


    @Test
    public void testMotiNickLike() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("*moti*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(4, globalEntitiesSorted.size());
        Assert.assertEquals("e00000002.global", globalEntitiesSorted.get(0).geteID());
        Assert.assertEquals("e00000001.global", globalEntitiesSorted.get(1).geteID());
        Assert.assertEquals("e00000003.global", globalEntitiesSorted.get(2).geteID());
        Assert.assertEquals("e00000004.global", globalEntitiesSorted.get(3).geteID());
    }

    @Test
    public void testMotiNickLikeSingleWildcard() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("moti*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(4, globalEntitiesSorted.size());
        Assert.assertEquals("e00000002.global", globalEntitiesSorted.get(0).geteID());
        Assert.assertEquals("e00000001.global", globalEntitiesSorted.get(1).geteID());
        Assert.assertEquals("e00000003.global", globalEntitiesSorted.get(2).geteID());
        Assert.assertEquals("e00000004.global", globalEntitiesSorted.get(3).geteID());
    }

    @Test
    public void testMotiNickLikeSingleWildcard2() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("*moti");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(4, globalEntitiesSorted.size());
        Assert.assertEquals("e00000002.global", globalEntitiesSorted.get(0).geteID());
        Assert.assertEquals("e00000001.global", globalEntitiesSorted.get(1).geteID());
    }

    @Test
    public void testMotiTitleLike() throws IOException, InterruptedException {
        Query query = getByTitleLike("*moti*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(3, globalEntitiesSorted.size());
        Assert.assertEquals("e00000002.global", globalEntitiesSorted.get(0).geteID());
        Assert.assertEquals("e00000001.global", globalEntitiesSorted.get(1).geteID());
        Assert.assertEquals("e00000004.global", globalEntitiesSorted.get(2).geteID());
    }

    @Test
    public void testMotiTitleLikeSingleWildcard() throws IOException, InterruptedException {
        Query query = getByTitleLike("moti*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(3, globalEntitiesSorted.size());
        Assert.assertEquals("e00000002.global", globalEntitiesSorted.get(0).geteID());
        Assert.assertEquals("e00000001.global", globalEntitiesSorted.get(1).geteID());
        Assert.assertEquals("e00000004.global", globalEntitiesSorted.get(2).geteID());
    }

    @Test
    public void testMotiTitleLikeSingleWildcardUpperCase() throws IOException, InterruptedException {
        Query query = getByTitleLike("Moti*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(3, globalEntitiesSorted.size());
        Assert.assertEquals("e00000002.global", globalEntitiesSorted.get(0).geteID());
        Assert.assertEquals("e00000001.global", globalEntitiesSorted.get(1).geteID());
        Assert.assertEquals("e00000004.global", globalEntitiesSorted.get(2).geteID());
    }


    @Test
    public void testMotiCoNickLike() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("*moti co*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(2, globalEntitiesSorted.size());
        Assert.assertEquals("e00000001.global", globalEntitiesSorted.get(0).geteID());
        Assert.assertEquals("e00000003.global", globalEntitiesSorted.get(1).geteID());
    }

    @Test
    public void testMotiCoNickLikeUpperCase() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("*Moti Co*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(2, globalEntitiesSorted.size());
        Assert.assertEquals("e00000001.global", globalEntitiesSorted.get(0).geteID());
        Assert.assertEquals("e00000003.global", globalEntitiesSorted.get(1).geteID());
    }

    @Test
    public void testMotiCoTitleLike() throws IOException, InterruptedException {
        Query query = getByTitleLike("*moti co*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(1, globalEntitiesSorted.size());
        Assert.assertEquals("e00000001.global", globalEntitiesSorted.get(0).geteID());
    }

    @Test
    public void testVeorgianaSuzetteNickLike() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("*veorgiana*suzette*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(1, globalEntitiesSorted.size());
        Assert.assertEquals("e00000008.global", globalEntitiesSorted.get(0).geteID());
    }

    @Test
    public void testVeorgianaSuzetteNickLikeUpperCase() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("*veorgiana*SUZETTE*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(1, globalEntitiesSorted.size());
        Assert.assertEquals("e00000008.global", globalEntitiesSorted.get(0).geteID());
    }

    @Test
    public void testVeorgianaNickLike() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("*veorgiana*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(3, globalEntitiesSorted.size());
        Set<String> namesSet = globalEntitiesSorted.stream().map(e -> e.geteID()).collect(Collectors.toSet());
        Assert.assertTrue(namesSet.contains("e00000007.global"));
        Assert.assertTrue(namesSet.contains("e00000008.global"));
        Assert.assertTrue(namesSet.contains("e00000009.global"));
    }

    @Test
    public void testAAA() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("*aaa*bbb*ccc*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(3, globalEntitiesSorted.size());
        Assert.assertEquals("e00000010.global", globalEntitiesSorted.get(0).geteID());
        Set<String> ids = new HashSet<>();
        ids.add(globalEntitiesSorted.get(1).geteID());
        ids.add(globalEntitiesSorted.get(2).geteID());

        Assert.assertTrue(ids.contains("e00000011.global"));
        Assert.assertTrue(ids.contains("e00000012.global"));

    }

    @Test
    public void testAAACCC() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("*aaa*ccc*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntitiesSorted = getGlobalEntitesSorted(assignmentsQueryResult);
        Assert.assertEquals(7, globalEntitiesSorted.size());
        Assert.assertEquals("e00000013.global", globalEntitiesSorted.get(0).geteID());
        Assert.assertEquals("e00000014.global", globalEntitiesSorted.get(1).geteID());
        Assert.assertEquals("e00000010.global", globalEntitiesSorted.get(2).geteID());
        Assert.assertEquals("e00000012.global", globalEntitiesSorted.get(3).geteID());
        Assert.assertEquals("e00000015.global", globalEntitiesSorted.get(4).geteID());
        Assert.assertEquals("e00000011.global", globalEntitiesSorted.get(5).geteID());
        Assert.assertEquals("e00000019.global", globalEntitiesSorted.get(6).geteID());

    }

    @Test
    public void testLongPrefix() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("abcdefghijkl*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntities
                = assignmentsQueryResult.getAssignments().get(0).getEntities().stream().filter(e -> e.geteTag().contains("A")).collect(Collectors.toList());
        Assert.assertEquals(2, globalEntities.size());
        Set<String> namesSet = globalEntities.stream().map(e -> e.geteID()).collect(Collectors.toSet());
        Assert.assertTrue(namesSet.contains("e00000016.global"));
        Assert.assertTrue(namesSet.contains("e00000017.global"));

    }

    @Test
    public void testQuestionMark() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("*?*");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntities
                = assignmentsQueryResult.getAssignments().get(0).getEntities().stream().filter(e -> e.geteTag().contains("A")).collect(Collectors.toList());
        Assert.assertEquals(3, globalEntities.size());
        Set<String> namesSet = globalEntities.stream().map(e -> e.geteID()).collect(Collectors.toSet());
        Assert.assertTrue(namesSet.contains("e00000018.global"));
        Assert.assertTrue(namesSet.contains("e00000019.global"));
        Assert.assertTrue(namesSet.contains("e00000020.global"));
    }

    @Test
    public void testWhitespace() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("* *");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntities
                = assignmentsQueryResult.getAssignments().get(0).getEntities().stream().filter(e -> e.geteTag().contains("A")).collect(Collectors.toList());
        Assert.assertEquals(15, globalEntities.size());
        Set<String> namesSet = globalEntities.stream().map(e -> e.geteID()).collect(Collectors.toSet());

    }

    @Test
    public void testTitleLikeAny() throws IOException, InterruptedException {
        Query query = getByTitleLikeAny(Arrays.asList("*moti*", "*cohen*"));
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());

        List<Entity> globalEntities
                = assignmentsQueryResult.getAssignments().get(0).getEntities().stream().filter(e -> e.geteTag().contains("A")).collect(Collectors.toList());
        Assert.assertEquals(3, globalEntities.size());
        Set<String> namesSet = globalEntities.stream().map(e -> e.geteID()).collect(Collectors.toSet());
        Assert.assertTrue(namesSet.contains("e00000001.global"));
        Assert.assertTrue(namesSet.contains("e00000002.global"));
        Assert.assertTrue(namesSet.contains("e00000004.global"));
    }

    @Test
    public void testTitleLikeAnyUpperCase() throws IOException, InterruptedException {
        Query query = getByTitleLikeAny(Arrays.asList("*MOTI*", "*COHEN*"));
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());

        List<Entity> globalEntities
                = assignmentsQueryResult.getAssignments().get(0).getEntities().stream().filter(e -> e.geteTag().contains("A")).collect(Collectors.toList());
        Assert.assertEquals(3, globalEntities.size());
        Set<String> namesSet = globalEntities.stream().map(e -> e.geteID()).collect(Collectors.toSet());
        Assert.assertTrue(namesSet.contains("e00000001.global"));
        Assert.assertTrue(namesSet.contains("e00000002.global"));
        Assert.assertTrue(namesSet.contains("e00000004.global"));
    }

    @Test
    public void testWhitespaceStartNickEq() throws IOException, InterruptedException {
        Query query = getByNicknamesEq("  OMG  ");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntities
                = assignmentsQueryResult.getAssignments().get(0).getEntities().stream().filter(e -> e.geteTag().contains("A")).collect(Collectors.toList());
        Assert.assertEquals(1, globalEntities.size());
        Set<String> namesSet = globalEntities.stream().map(e -> e.geteID()).collect(Collectors.toSet());
        Assert.assertTrue(namesSet.contains("e00000021.global"));
    }

    @Test
    public void testWhitespaceStartNickLikeEq() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("  OMG  ");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntities
                = assignmentsQueryResult.getAssignments().get(0).getEntities().stream().filter(e -> e.geteTag().contains("A")).collect(Collectors.toList());
        Assert.assertEquals(1, globalEntities.size());
        Set<String> namesSet = globalEntities.stream().map(e -> e.geteID()).collect(Collectors.toSet());
        Assert.assertTrue(namesSet.contains("e00000021.global"));
    }

    @Test
    public void testWhitespaceStartNick() throws IOException, InterruptedException {
        Query query = getByNicknamesLike("*  OMG  *");
        AssignmentsQueryResult assignmentsQueryResult = runQuery(query, Arrays.asList("A"));
        Assert.assertEquals(1, assignmentsQueryResult.getAssignments().size());
        List<Entity> globalEntities
                = assignmentsQueryResult.getAssignments().get(0).getEntities().stream().filter(e -> e.geteTag().contains("A")).collect(Collectors.toList());
        Assert.assertEquals(1, globalEntities.size());
        Set<String> namesSet = globalEntities.stream().map(e -> e.geteID()).collect(Collectors.toSet());
        Assert.assertTrue(namesSet.contains("e00000021.global"));
    }


    private List<Entity> getGlobalEntitesSorted(AssignmentsQueryResult assignmentsQueryResult) {
        return assignmentsQueryResult.getAssignments().get(0).getEntities().stream().filter(e -> e.geteTag().contains("A")).sorted((o1, o2) -> -1*Double.compare((double) o1.getProperties().stream().filter(p -> p.getpType().equals("score")).findFirst().get().getValue(),
                (double) o2.getProperties().stream().filter(p -> p.getpType().equals("score")).findFirst().get().getValue())).collect(Collectors.toList());
    }

    private Query getByNicknamesLike(String nick) {
        return Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 6,0),
                new Rel(6, $ont.rType$("hasEvalue"), Rel.Direction.R, "", 7, 0),
                new ETyped(7, "D.globalEntityValue", $ont.eType$("Evalue"), 8,0),
                new Quant1(8, QuantType.all, Arrays.asList(9,10), 0),
                new EProp(9, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "nicknames")),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, nick))
        )).build();
    }

    private Query getByTitleLikeAny(List<String> names) {
        return Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 6,0),
                new Rel(6, $ont.rType$("hasEvalue"), Rel.Direction.R, "", 7, 0),
                new ETyped(7, "D.globalEntityValue", $ont.eType$("Evalue"), 8,0),
                new Quant1(8, QuantType.all, Arrays.asList(9,10), 0),
                new EProp(9, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "title")),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.likeAny, names))
        )).build();
    }

    private Query getByTitleLike(String nick) {
        return Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 6,0),
                new Rel(6, $ont.rType$("hasEvalue"), Rel.Direction.R, "", 7, 0),
                new ETyped(7, "D.globalEntityValue", $ont.eType$("Evalue"), 8,0),
                new Quant1(8, QuantType.all, Arrays.asList(9,10), 0),
                new EProp(9, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "title")),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.like, nick))
        )).build();
    }


    private Query getByNicknamesEq(String nick) {
        return Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 6,0),
                new Rel(6, $ont.rType$("hasEvalue"), Rel.Direction.R, "", 7, 0),
                new ETyped(7, "D.globalEntityValue", $ont.eType$("Evalue"), 8,0),
                new Quant1(8, QuantType.all, Arrays.asList(9,10), 0),
                new EProp(9, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "nicknames")),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.eq, nick))
        )).build();
    }

    private Query getByTitleEq(String nick) {
        return Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Entity"), 6,0),
                new Rel(6, $ont.rType$("hasEvalue"), Rel.Direction.R, "", 7, 0),
                new ETyped(7, "D.globalEntityValue", $ont.eType$("Evalue"), 8,0),
                new Quant1(8, QuantType.all, Arrays.asList(9,10), 0),
                new EProp(9, $ont.pType$("fieldId"), Constraint.of(ConstraintOp.eq, "title")),
                new EProp(10, $ont.pType$("stringValue"), Constraint.of(ConstraintOp.eq, nick))
        )).build();
    }


    private Query getEValues() {
        return Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", $ont.eType$("Evalue"), 0,0)
        )).build();
    }



    protected boolean shouldIgnoreRelId() {
        return true;
    }
    private static Ontology.Accessor $ont;
    private static RankingKnowledgeDataInfraManager manager;

}
