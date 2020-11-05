package com.yangdb.fuse.epb.plan;

import com.yangdb.fuse.dispatcher.epb.PlanPruneStrategy;
import com.yangdb.fuse.dispatcher.epb.PlanSelector;
import com.yangdb.fuse.dispatcher.epb.PlanValidator;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.epb.plan.estimation.CostEstimationConfig;
import com.yangdb.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.yangdb.fuse.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import com.yangdb.fuse.epb.plan.extenders.M1.M1PlanExtensionStrategy;
import com.yangdb.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.yangdb.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.yangdb.fuse.epb.plan.selectors.CheapestPlanSelector;
import com.yangdb.fuse.epb.plan.statistics.EBaseStatisticsProvider;
import com.yangdb.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.yangdb.fuse.epb.plan.statistics.Statistics;
import com.yangdb.fuse.epb.plan.validation.M1PlanValidator;
import com.yangdb.fuse.epb.utils.PlanMockUtils;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.PlanAssert;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.yangdb.fuse.unipop.schemaProviders.*;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.elasticsearch.common.collect.Tuple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.yangdb.fuse.model.OntologyTestUtils.*;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.ePropGroup;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 24/05/2017.
 */
public class SmartEpbSelectivityTests {
    @Before
    public void setup() throws ParseException {
        startTime = DATE_FORMAT.parse("2017-01-01-10").getTime();
        Map<String, Integer> typeCard = new HashMap<>();
        typeCard.put(OWN.getName(), 1000);
        typeCard.put(MEMBER_OF.getName(), 400);
        typeCard.put(OntologyTestUtils.DRAGON.name, 1000);
        typeCard.put(OntologyTestUtils.PERSON.name, 200);
        typeCard.put(OntologyTestUtils.GUILD.name, 100);

        globalSelectivityMap = new HashMap<>();
        globalSelectivityMap.put(new Tuple<>(OWN.getName(), Rel.Direction.R), 10L);
        globalSelectivityMap.put(new Tuple<>(OWN.getName(), Rel.Direction.L), 1L);
        globalSelectivityMap.put(new Tuple<>(MEMBER_OF.getName(), Rel.Direction.R), 3L);
        globalSelectivityMap.put(new Tuple<>(MEMBER_OF.getName(), Rel.Direction.L), 50L);

        graphStatisticsProvider = mock(GraphStatisticsProvider.class);
        when(graphStatisticsProvider.getEdgeCardinality(any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edgeSchema = invocationOnMock.getArgument(0, GraphEdgeSchema.class);
            List<String> indices = Stream.ofAll(edgeSchema.getIndexPartitions().get().getPartitions())
                    .flatMap(IndexPartitions.Partition::getIndices).toJavaList();
            return graphStatisticsProvider.getEdgeCardinality(edgeSchema, indices);
        });

        when(graphStatisticsProvider.getEdgeCardinality(any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edgeSchema = invocationOnMock.getArgument(0, GraphEdgeSchema.class);
            List indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(edgeSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            return new Statistics.SummaryStatistics(typeCard.get(constraintLabel)* indices.size(), typeCard.get(constraintLabel)* indices.size());
        });

        when(graphStatisticsProvider.getVertexCardinality(any())).thenAnswer(invocationOnMock -> {
            GraphVertexSchema vertexSchema = invocationOnMock.getArgument(0, GraphVertexSchema.class);
            List<String> indices = Stream.ofAll(vertexSchema.getIndexPartitions().get().getPartitions())
                    .flatMap(IndexPartitions.Partition::getIndices).toJavaList();
            return graphStatisticsProvider.getVertexCardinality(vertexSchema, indices);
        });

        when(graphStatisticsProvider.getVertexCardinality(any(), any())).thenAnswer(invocationOnMock -> {
            GraphVertexSchema vertexSchema = invocationOnMock.getArgument(0, GraphVertexSchema.class);
            List indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(vertexSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            return new Statistics.SummaryStatistics(typeCard.get(constraintLabel)*indices.size(), typeCard.get(constraintLabel)*indices.size());
        });

        when(graphStatisticsProvider.getGlobalSelectivity(any(), any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edgeSchema = invocationOnMock.getArgument(0, GraphEdgeSchema.class);
            Rel.Direction direction = invocationOnMock.getArgument(1, Rel.Direction.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(edgeSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            Tuple<String, Rel.Direction> edge = new Tuple<>(constraintLabel, direction);
            return globalSelectivityMap.getOrDefault(edge, 10L);
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(String.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgument(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            return createStringHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Long.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgument(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            return createLongHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Date.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgument(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgument(1, List.class);

            String constraintLabel = Stream.ofAll(
                    new TraversalValuesByKeyProvider().getValueByKey(elementSchema.getConstraint().getTraversalConstraint(), org.apache.tinkerpop.gremlin.structure.T.label.getAccessor()))
                    .get(0);

            int card = typeCard.get(constraintLabel);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgument(2, GraphElementPropertySchema.class);
            return createDateHistogram(card,elementSchema,propertySchema, indices);
        });

        ontology = OntologyTestUtils.createDragonsOntologyShort();
        ont = new Ontology.Accessor(ontology);
        graphElementSchemaProvider = buildSchemaProvider(ont);

        eBaseStatisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, ont, graphStatisticsProvider);
        estimator = new RegexPatternCostEstimator(new M1PatternCostEstimator(
                new CostEstimationConfig(1.0, 0.001),
                (ont) -> eBaseStatisticsProvider,
                new OntologyProvider() {
                    @Override
                    public Optional<Ontology> get(String id) {
                        return Optional.of(ont.get());
                    }

                    @Override
                    public Collection<Ontology> getAll() {
                        return Collections.singleton(ont.get());
                    }

                    @Override
                    public Ontology add(Ontology ontology) {
                return ontology;
             }
                }));

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();


        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> globalPlanSelector = new CheapestPlanSelector();
        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> localPlanSelector = new AllCompletePlanSelector<>();

        planSearcher = new BottomUpPlanSearcher<>(
                new M1PlanExtensionStrategy(new OntologyProvider() {
                    @Override
                    public Ontology add(Ontology ontology) {
                        return ontology;
                    }
                    @Override
                    public Optional<Ontology> get(String id) {
                        return Optional.of(ontology);
                    }

                    @Override
                    public Collection<Ontology> getAll() {
                        return Collections.singleton(ontology);
                    }

                }, ont -> graphElementSchemaProvider),
                pruneStrategy,
                pruneStrategy,
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
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, 0L, 1000L));
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, 1000L, 2000L));
        bucketInfos.add(new Statistics.BucketInfo<>(bucketSize, bucketSize/10, 2000L, 3000L));
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

    @Test
    public void testThreeEntityPathSourceCondition(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(quant1(2, QuantType.all)).
                in(AsgQuery.Builder.ePropGroup(3, EProp.of(3, FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(AsgQuery.Builder.ePropGroup(7))),
                        rel(8, MEMBER_OF.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, GUILD.type).next(AsgQuery.Builder.ePropGroup(11)))).
                build();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(3).rel(8).relFilter(9).entity(10).entityFilter(11).goTo(1).rel(4).relFilter(5).entity(6).entityFilter(7).plan();

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(54.043, plan.getCost().getGlobalCost().cost, 0.1);
    }

    @Test
    public void testThreeEntityPathSourceConditionDestFilter(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(quant1(2, QuantType.all)).
                in(AsgQuery.Builder.ePropGroup(3, EProp.of(3, FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(AsgQuery.Builder.ePropGroup(7, EProp.of(7, NAME.type, Constraint.of(ConstraintOp.eq,"abc"))))),
                        rel(8, MEMBER_OF.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, GUILD.type).next(AsgQuery.Builder.ePropGroup(11)))).
                build();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(3).rel(4).relFilter(5).entity(6).entityFilter(7).goTo(1).rel(8).relFilter(9).entity(10).entityFilter(11).plan();

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(25.113, plan.getCost().getGlobalCost().cost, 0.1);
    }

    //region Private Methods
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
                                        Collections.singletonList(relation.getePairs().get(0).geteTypeA() + "IdA"),
                                        Optional.of(relation.getePairs().get(0).geteTypeA()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(relation.getePairs().get(0).geteTypeB() + "IdB"),
                                        Optional.of(relation.getePairs().get(0).geteTypeB()))),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
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

        return new OntologySchemaProvider(ont.get(), new GraphElementSchemaProvider.Impl(vertexSchemas, edgeSchemas));
    }
    //endregion

    //region Fields
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology ontology;
    private Ontology.Accessor ont;
    private GraphStatisticsProvider graphStatisticsProvider;

    private EBaseStatisticsProvider eBaseStatisticsProvider;
    private RegexPatternCostEstimator estimator;

    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;
    private long startTime;

    private static String INDEX_PREFIX = "idx-";
    private static String INDEX_FORMAT = "idx-%s";
    private static String DATE_FORMAT_STRING = "yyyy-MM-dd-HH";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
    private Map<Tuple<String, Rel.Direction>, Long> globalSelectivityMap;
    //endregion
}
