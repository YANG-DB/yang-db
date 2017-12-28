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
import com.kayhut.fuse.epb.plan.pruners.M2GlobalPruner;
import com.kayhut.fuse.epb.plan.pruners.M2LocalPruner;
import com.kayhut.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.kayhut.fuse.epb.plan.selectors.CheapestPlanSelector;
import com.kayhut.fuse.epb.plan.statistics.EBaseStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.validation.M2PlanValidator;
import com.kayhut.fuse.epb.utils.PlanMockUtils;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanAssert;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
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
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EpbJoinSelectionTests {
    @Before
    public void setup() throws ParseException {
        startTime = DATE_FORMAT.parse("2017-01-01-10").getTime();
        Map<String, Integer> typeCard = new HashMap<>();
        typeCard.put(OWN.getName(), 1000);
        typeCard.put(REGISTERED.getName(), 5000);
        typeCard.put(SUBJECT.getName(), 10000);
        typeCard.put(MEMBER_OF.getName(), 100);
        typeCard.put(FIRE.getName(), 10000);
        typeCard.put(FREEZE.getName(), 50000);
        typeCard.put(DRAGON.name, 10000);
        typeCard.put(PERSON.name, 2000);
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
                    new TraversalValuesByKeyProvider().getValueByKey(edgeSchema.getConstraint().getTraversalConstraint(), T.label.getAccessor()))
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
                    new TraversalValuesByKeyProvider().getValueByKey(vertexSchema.getConstraint().getTraversalConstraint(), T.label.getAccessor()))
                    .get(0);

            return new Statistics.SummaryStatistics(typeCard.get(constraintLabel)*indices.size(), typeCard.get(constraintLabel)*indices.size());
        });

        when(graphStatisticsProvider.getGlobalSelectivity(any(), any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema graphEdgeSchema = invocationOnMock.getArgumentAt(0, GraphEdgeSchema.class);
            Rel.Direction direction = invocationOnMock.getArgumentAt(1, Rel.Direction.class);
            if(graphEdgeSchema.getLabel().equals(SUBJECT.getName()) && direction.name().equals(Rel.Direction.L.name())){
                return 10000;
            }
            if(graphEdgeSchema.getLabel().equals(SUBJECT.getName()) && direction.name().equals(Rel.Direction.R)){
                return 1;
            }
            return 5L;
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(String.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgumentAt(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            return createStringHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Long.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgumentAt(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            return createLongHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Date.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgumentAt(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgumentAt(2, GraphElementPropertySchema.class);
            return createDateHistogram(card,elementSchema,propertySchema, indices);
        });

        ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
        graphElementSchemaProvider = buildSchemaProvider(ont);

        eBaseStatisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, ont, graphStatisticsProvider);
        RegexPatternCostEstimator regexPatternCostEstimator = new RegexPatternCostEstimator(new M2PatternCostEstimator(
                new CostEstimationConfig(0.8, 1),
                (ont) -> eBaseStatisticsProvider,
                (id) -> Optional.of(ont.get())));
        ((EntityJoinPatternCostEstimator)((M2PatternCostEstimator)regexPatternCostEstimator.getEstimator()).getEstimators().get(EntityJoinPattern.class)).setCostEstimator(regexPatternCostEstimator);
        estimator = regexPatternCostEstimator;

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> globalPruner = new M2GlobalPruner();
        PlanValidator<Plan, AsgQuery> validator = new M2PlanValidator();

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> localPruner = new M2LocalPruner();


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
    public void testJoinPlanSelection(){
            AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                    next(typed(1, PERSON.type)).
                    next(eProp(2, EProp.of(NAME.name, 2, of(eq, "abc")))).
                    next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                    next(typed(5, DRAGON.type)).
                    next(eProp(6)).
                    next(rel(7, SUBJECT.getrType(), Rel.Direction.R).below(relProp(8))).
                    next(typed(9, KINGDOM.type)).
                    next(eProp(10)).
                    next(rel(11, SUBJECT.getrType(), Rel.Direction.L).below(relProp(12))).
                    next(typed(13, DRAGON.type)).
                    next(eProp(14)).
                    next(rel(15, OWN.getrType(), Rel.Direction.L).below(relProp(16))).
                    next(typed(17, PERSON.type)).
                    next(eProp(18,EProp.of(NAME.name, 18, of(eq, "abc")))).
                    build();

            PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
            Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).join(PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(2).rel(3).relFilter(4).entity(5).entityFilter(6).rel(7).relFilter(8).entity(9).entityFilter(10).plan(),
                    PlanMockUtils.PlanMockBuilder.mock(query).entity(17).entityFilter(18).rel(15, Rel.Direction.R).relFilter(16).entity(13).entityFilter(14).rel(11, Rel.Direction.R).relFilter(12).entity(9).entityFilter(10).plan()).plan();
            PlanAssert.assertEquals(expected, plan.getPlan());
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