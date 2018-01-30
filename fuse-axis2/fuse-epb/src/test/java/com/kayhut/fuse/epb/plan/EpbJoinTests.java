package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.dispatcher.epb.PlanSelector;
import com.kayhut.fuse.dispatcher.epb.PlanValidator;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.pattern.EntityJoinPattern;
import com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.EntityJoinPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.M2PatternCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.M2.M2PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.kayhut.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.kayhut.fuse.epb.plan.selectors.CheapestPlanSelector;
import com.kayhut.fuse.epb.plan.statistics.EBaseStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.validation.M2PlanValidator;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.FREEZE;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.eProp;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.kayhut.fuse.model.query.ConstraintOp.ge;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.ConstraintOp.le;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EpbJoinTests {
    @Before
    public void setup() throws ParseException {
        startTime = DATE_FORMAT.parse("2017-01-01-10").getTime();
        Map<String, Integer> typeCard = new HashMap<>();
        typeCard.put(OWN.getName(), 1000);
        typeCard.put(REGISTERED.getName(), 5000);
        typeCard.put(MEMBER_OF.getName(), 100);
        typeCard.put(FIRE.getName(), 10000);
        typeCard.put(FREEZE.getName(), 50000);
        typeCard.put(DRAGON.name, 1000);
        typeCard.put(PERSON.name, 200);
        typeCard.put(HORSE.name, 4600);
        typeCard.put(GUILD.name, 50);
        typeCard.put(KINGDOM.name, 15);

        graphStatisticsProvider = mock(GraphStatisticsProvider.class);
        when(graphStatisticsProvider.getEdgeCardinality(any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edgeSchema = invocationOnMock.getArgumentAt(0, GraphEdgeSchema.class);
            List<String> indices = Stream.ofAll(edgeSchema.getIndexPartitions().get().getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList();
            return graphStatisticsProvider.getEdgeCardinality(edgeSchema, indices);
        });

        when(graphStatisticsProvider.getEdgeCardinality(any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edgeSchema = invocationOnMock.getArgumentAt(0, GraphEdgeSchema.class);
            List indices = invocationOnMock.getArgumentAt(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(edgeSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            return new Statistics.SummaryStatistics(typeCard.get(constraintLabel)* indices.size(), typeCard.get(constraintLabel)* indices.size());
        });

        when(graphStatisticsProvider.getVertexCardinality(any())).thenAnswer(invocationOnMock -> {
            GraphVertexSchema vertexSchema = invocationOnMock.getArgumentAt(0, GraphVertexSchema.class);
            List<String> indices = Stream.ofAll(vertexSchema.getIndexPartitions().get().getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaList();
            return graphStatisticsProvider.getVertexCardinality(vertexSchema, indices);
        });

        when(graphStatisticsProvider.getVertexCardinality(any(), any())).thenAnswer(invocationOnMock -> {
            GraphVertexSchema vertexSchema = invocationOnMock.getArgumentAt(0, GraphVertexSchema.class);
            List indices = invocationOnMock.getArgumentAt(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(vertexSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            return new Statistics.SummaryStatistics(typeCard.get(constraintLabel)*indices.size(), typeCard.get(constraintLabel)*indices.size());
        });

        when(graphStatisticsProvider.getGlobalSelectivity(any(), any(), any())).thenReturn(10l);

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(String.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgumentAt(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            return createStringHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Long.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgumentAt(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            return createLongHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Date.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgumentAt(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgumentAt(2, GraphElementPropertySchema.class);
            return createDateHistogram(card,elementSchema,propertySchema, indices);
        });

        ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
        graphElementSchemaProvider = buildSchemaProvider(ont);

        eBaseStatisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, ont, graphStatisticsProvider);
        M2PatternCostEstimator m2PatternCostEstimator = new M2PatternCostEstimator(
                new CostEstimationConfig(1.0, 0.001),
                (ont) -> eBaseStatisticsProvider,
                (id) -> Optional.of(ont.get()),
                null);
        EntityJoinPatternCostEstimator entityJoinPatternCostEstimator = (EntityJoinPatternCostEstimator)m2PatternCostEstimator.getEstimators().get(EntityJoinPattern.class);

        RegexPatternCostEstimator regexPatternCostEstimator = new RegexPatternCostEstimator(m2PatternCostEstimator);
        entityJoinPatternCostEstimator.setCostEstimator(regexPatternCostEstimator);

        estimator = regexPatternCostEstimator;

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> globalPruner = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M2PlanValidator();

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> localPruner = new NoPruningPruneStrategy<>();


        globalPlanSelector = new KeepAllPlansSelectorDecorator<>(new CheapestPlanSelector());
        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> localPlanSelector = new AllCompletePlanSelector<>();
        planSearcher = new BottomUpPlanSearcher<>(
                new M2PlanExtensionStrategy(id -> Optional.of(ont.get()), ont -> graphElementSchemaProvider),
                globalPruner,
                localPruner,
                globalPlanSelector,
                localPlanSelector,
                validator,
                estimator);
    }


    private Statistics.HistogramStatistics<Date> createDateHistogram(long card, GraphElementSchema elementSchema, GraphElementPropertySchema graphElementPropertySchema,List<String> indices) {
        List<Statistics.BucketInfo<Date>> buckets = new ArrayList<>();
        if(elementSchema.getIndexPartitions().get() instanceof TimeSeriesIndexPartitions){
            TimeSeriesIndexPartitions timeSeriesIndexPartition = (TimeSeriesIndexPartitions) elementSchema.getIndexPartitions().get();
            if(timeSeriesIndexPartition.getTimeField().equals(graphElementPropertySchema.getName())){
                for(int i = 0;i<3;i++){
                    Date dt = new Date(startTime - i*60*60*1000);
                    String indexName = timeSeriesIndexPartition.getIndexName(dt);
                    if(indices.contains(indexName)){
                        buckets.add(new Statistics.BucketInfo<>(card, card/10, dt, new Date(startTime - (i-1) * 60*60*1000)));
                    }
                }
                return new Statistics.HistogramStatistics<>(buckets);
            }
        }
        long bucketSize = card * indices.size() / 3;
        for(int i = 0;i < 3;i++){
            Date dt = new Date(startTime - i*60*60*1000);
            buckets.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, dt, new Date(startTime - (i-1) * 60*60*1000)));
        }
        return new Statistics.HistogramStatistics<>(buckets);
    }

    private Statistics.HistogramStatistics<Long> createLongHistogram(int card, int numIndices) {
        long bucketSize = card * numIndices / 3;
        List<Statistics.BucketInfo<Long>> bucketInfos = new ArrayList<>();
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, 0l,1000l));
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, 1000l,2000l));
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, 2000l,3000l));
        return new Statistics.HistogramStatistics<>(bucketInfos);
    }

    private Statistics.HistogramStatistics<String> createStringHistogram(int card, int numIndices) {
        long bucketSize = card * numIndices / 3;
        List<Statistics.BucketInfo<String>> bucketInfos = new ArrayList<>();
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, "a","g"));
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, "g","o"));
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, "o","z"));
        return new Statistics.HistogramStatistics<>(bucketInfos);
    }

    private GraphElementSchemaProvider buildSchemaProvider(Ontology.Accessor ont) {
        Iterable<GraphVertexSchema> vertexSchemas =
                Stream.ofAll(ont.entities())
                        .map(entity -> (GraphVertexSchema) new GraphVertexSchema.Impl(
                                entity.geteType(),
                                entity.geteType().equals(PERSON.name) ? new StaticIndexPartitions(Arrays.asList("Persons1","Persons2")) :
                                        entity.geteType().equals(DRAGON.name) ? new StaticIndexPartitions(Arrays.asList("Dragons1","Dragons2")) :
                                                new StaticIndexPartitions(Collections.singletonList("idx1"))))
                        .toJavaList();

        Iterable<GraphEdgeSchema> edgeSchemas =
                Stream.ofAll(ont.relations())
                        .map(relation -> (GraphEdgeSchema) new GraphEdgeSchema.Impl(
                                relation.getrType(),
                                new GraphElementConstraint.Impl(__.has(T.label, relation.getrType())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        relation.getePairs().get(0).geteTypeA() + "IdA",
                                        Optional.of(relation.getePairs().get(0).geteTypeA()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        relation.getePairs().get(0).geteTypeB() + "IdB",
                                        Optional.of(relation.getePairs().get(0).geteTypeB()))),
                                Optional.of(new GraphEdgeSchema.Direction.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.of(relation.getrType().equals(OWN.getName()) ?
                                        new TimeSeriesIndexPartitions() {
                                            @Override
                                            public Optional<String> getPartitionField() {
                                                return Optional.of(START_DATE.name);
                                            }

                                            @Override
                                            public Iterable<Partition> getPartitions() {
                                                return Collections.singletonList(() ->
                                                        IntStream.range(0, 3).mapToObj(i -> new Date(startTime - 60*60*1000 * i)).
                                                                map(this::getIndexName).collect(Collectors.toList()));
                                            }

                                            @Override
                                            public String getDateFormat() {
                                                return DATE_FORMAT_STRING;
                                            }

                                            @Override
                                            public String getIndexPrefix() {
                                                return INDEX_PREFIX;
                                            }

                                            @Override
                                            public String getIndexFormat() {
                                                return INDEX_FORMAT;
                                            }

                                            @Override
                                            public String getTimeField() {
                                                return START_DATE.name;
                                            }

                                            @Override
                                            public String getIndexName(Date date) {
                                                return String.format(getIndexFormat(), DATE_FORMAT.format(date));
                                            }
                                        } : new StaticIndexPartitions(Collections.singletonList("idx1"))),
                                Collections.emptyList()))
                        .toJavaList();

        return new OntologySchemaProvider(ont.get(), new OntologySchemaProvider.Adapter(vertexSchemas, edgeSchemas));
    }

    @Test
    public void testSimplePatternNoJoin(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, DRAGON.type)).
                next(eProp(6)).
                build();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Assert.assertEquals(2,Stream.ofAll(globalPlanSelector.getPlans()).length());

        assertNoJoinPlans(globalPlanSelector.getPlans());

    }

    @Test
    public void test3HopsJoinCreation(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, DRAGON.type)).
                next(eProp(6)).
                next(rel(7, OWN.getrType(), Rel.Direction.R).below(relProp(8))).
                next(typed(9, DRAGON.type)).
                next(eProp(10)).
                build();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        List<PlanWithCost<Plan, PlanDetailedCost>> joinPlans = Stream.ofAll(this.globalPlanSelector.getPlans()).filter(p -> p.getPlan().getOps().stream().anyMatch(op -> op instanceof EntityJoinOp)).toJavaList();
        Assert.assertEquals(2, joinPlans.size());
        Assert.assertTrue(joinPlans.get(0).getCost().getGlobalCost().getCost() == joinPlans.get(1).getCost().getGlobalCost().getCost());
    }

    @Test
    public void testStarJoinCreation(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(quant1(2, all))
                .in(eProp(3)
                        , rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type).
                                next(eProp(7)))
                        , rel(8, OWN.getrType(), Rel.Direction.R).below(relProp(9)).
                            next(typed(10, DRAGON.type).
                            next(eProp(11)))
                        , rel(12, OWN.getrType(), Rel.Direction.R).below(relProp(13)).
                                next(typed(14, DRAGON.type).
                                next(eProp(15)))
                ).
                build();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        List<PlanWithCost<Plan, PlanDetailedCost>> joinPlans = Stream.ofAll(this.globalPlanSelector.getPlans()).filter(p -> p.getPlan().getOps().stream().anyMatch(op -> op instanceof EntityJoinOp)).toJavaList();
        for (PlanWithCost<Plan, PlanDetailedCost> joinPlan : joinPlans) {
            Iterable<Plan> permutations = permute(joinPlan.getPlan());
            for (Plan newPlan : permutations) {
                Assert.assertTrue(joinPlans.stream().anyMatch(p -> p.getPlan().toString().equals(newPlan.toString())));
                Assert.assertEquals(joinPlan.getCost().getGlobalCost().cost, Stream.ofAll(joinPlans).find(p -> p.getPlan().toString().equals(newPlan.toString())).get().getCost().getGlobalCost().cost,0.0001);
            }
        }
    }

    private Iterable<Plan> permute(Plan plan){
        List<Plan> plans = new ArrayList<>();

        if(plan.getOps().get(0) instanceof EntityJoinOp){
            EntityJoinOp entityJoinOp = (EntityJoinOp) plan.getOps().get(0);

            Iterable<Plan> leftPlans = permute(entityJoinOp.getLeftBranch());
            Iterable<Plan> rightPlans = permute(entityJoinOp.getRightBranch());
            for(Plan leftPlan : leftPlans){
                for(Plan rightPlan : rightPlans){
                    EntityJoinOp newJoinOp = new EntityJoinOp(leftPlan, rightPlan,true);
                    Plan newPlan = new Plan(Stream.of((PlanOp)newJoinOp).appendAll(plan.getOps().stream().skip(1).collect(Collectors.toList())));
                    plans.add(newPlan);

                    newJoinOp = new EntityJoinOp(rightPlan, leftPlan,true);
                    newPlan = new Plan(Stream.of((PlanOp)newJoinOp).appendAll(plan.getOps().stream().skip(1).collect(Collectors.toList())));
                    plans.add(newPlan);
                }
            }
        }else{
            plans.add(plan);
        }

        return plans;
    }


    private void assertNoJoinPlans(Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans) {
        for (PlanWithCost<Plan, PlanDetailedCost> plan : plans) {
            Assert.assertFalse(plan.getPlan().getOps().stream().anyMatch(op -> op instanceof EntityJoinOp));
        }
    }



    //region Fields
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology.Accessor ont;
    private GraphStatisticsProvider graphStatisticsProvider;

    private EBaseStatisticsProvider eBaseStatisticsProvider;
    private RegexPatternCostEstimator estimator;

    protected BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;
    private KeepAllPlansSelectorDecorator<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> globalPlanSelector;
    protected long startTime;

    private static String INDEX_PREFIX = "idx-";
    private static String INDEX_FORMAT = "idx-%s";
    private static String DATE_FORMAT_STRING = "yyyy-MM-dd-HH";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
    //endregion


    private class KeepAllPlansSelectorDecorator<P, Q> implements PlanSelector<P, Q>{
        private Iterable<P> plans;
        private PlanSelector<P,Q> innerSelector;

        public KeepAllPlansSelectorDecorator(PlanSelector<P, Q> innerSelector) {
            this.innerSelector = innerSelector;
        }

        public Iterable<P> getPlans() {
            return plans;
        }


        @Override
        public Iterable<P> select(Q query, Iterable<P> plans) {
            this.plans = plans;
            return this.innerSelector.select(query, plans);
        }
    }
}
