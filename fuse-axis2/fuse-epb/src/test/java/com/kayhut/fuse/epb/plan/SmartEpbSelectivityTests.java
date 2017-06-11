package com.kayhut.fuse.epb.plan;

import com.google.common.collect.Iterables;
import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.cost.calculation.BasicStepEstimator;
import com.kayhut.fuse.epb.plan.cost.calculation.M1StepEstimator;
import com.kayhut.fuse.epb.plan.extenders.M1NonRedundantPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.M1PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.statistics.EBaseStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.epb.tests.PlanMockUtils;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanAssert;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartition;
import javaslang.collection.Stream;
import org.elasticsearch.common.collect.Tuple;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.eProp;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 24/05/2017.
 */
public class SmartEpbSelectivityTests {

    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology ontology;
    private Ontology.Accessor ont;
    private PhysicalIndexProvider physicalIndexProvider;
    private GraphStatisticsProvider graphStatisticsProvider;
    private GraphLayoutProvider layoutProvider;

    private EBaseStatisticsProvider eBaseStatisticsProvider;
    private StatisticsCostEstimator statisticsCostEstimator;

    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;
    private long startTime;

    private static String INDEX_PREFIX = "idx-";
    private static String INDEX_FORMAT = "idx-%s";
    private static String DATE_FORMAT_STRING = "yyyy-MM-dd-HH";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
    private Map<Tuple<String, Rel.Direction>, Long> globalSelectivityMap;

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
            GraphEdgeSchema edgeSchema = invocationOnMock.getArgumentAt(0, GraphEdgeSchema.class);
            List<String> indices = Stream.ofAll(edgeSchema.getIndexPartition().getIndices()).toJavaList();
            return graphStatisticsProvider.getEdgeCardinality(edgeSchema, indices);
        });

        when(graphStatisticsProvider.getEdgeCardinality(any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edgeSchema = invocationOnMock.getArgumentAt(0, GraphEdgeSchema.class);
            List indices = invocationOnMock.getArgumentAt(1, List.class);
            return new Statistics.Cardinality(typeCard.get(edgeSchema.getType())* indices.size(), typeCard.get(edgeSchema.getType())* indices.size());
        });

        when(graphStatisticsProvider.getVertexCardinality(any())).thenAnswer(invocationOnMock -> {
            GraphVertexSchema vertexSchema = invocationOnMock.getArgumentAt(0, GraphVertexSchema.class);
            List<String> indices = Stream.ofAll(vertexSchema.getIndexPartition().getIndices()).toJavaList();
            return graphStatisticsProvider.getVertexCardinality(vertexSchema, indices);
        });

        when(graphStatisticsProvider.getVertexCardinality(any(), any())).thenAnswer(invocationOnMock -> {
            GraphVertexSchema vertexSchema = invocationOnMock.getArgumentAt(0, GraphVertexSchema.class);
            List indices = invocationOnMock.getArgumentAt(1, List.class);
            return new Statistics.Cardinality(typeCard.get(vertexSchema.getType())*indices.size(), typeCard.get(vertexSchema.getType())*indices.size());
        });

        when(graphStatisticsProvider.getGlobalSelectivity(any(), any(), any())).thenAnswer(invocationOnMock -> {
            GraphEdgeSchema edgeSchema = invocationOnMock.getArgumentAt(0, GraphEdgeSchema.class);
            Rel.Direction direction = invocationOnMock.getArgumentAt(1, Rel.Direction.class);
            Tuple<String, Rel.Direction> edge = new Tuple<>(edgeSchema.getType(), direction);
            return globalSelectivityMap.getOrDefault(edge, 10L);
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(String.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgumentAt(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);
            int card = typeCard.get(elementSchema.getType());
            return createStringHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Long.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgumentAt(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);
            int card = typeCard.get(elementSchema.getType());
            return createLongHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), eq(Date.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgumentAt(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);
            int card = typeCard.get(elementSchema.getType());
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgumentAt(2, GraphElementPropertySchema.class);
            return createDateHistogram(card,elementSchema,propertySchema, indices);
        });

        IndexPartition defaultIndexPartition = mock(IndexPartition.class);
        when(defaultIndexPartition.getIndices()).thenReturn(Collections.singleton("idx1"));
        physicalIndexProvider = mock(PhysicalIndexProvider.class);
        when(physicalIndexProvider.getIndexPartitionByLabel(any(), any())).thenAnswer(invocationOnMock -> {
            String type = invocationOnMock.getArgumentAt(0, String.class);
            if(type.equals(PERSON.name)){
                return (IndexPartition) () -> Arrays.asList("Persons1","Persons2");
            }
            if(type.equals(DRAGON.name)){
                return (IndexPartition) () -> Arrays.asList("Dragons1","Dragons2");
            }
            if(type.equals(OWN.getName())){
                return new TimeSeriesIndexPartition() {
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

                    @Override
                    public Iterable<String> getIndices() {
                        return IntStream.range(0, 3).mapToObj(i -> new Date(startTime - 60*60*1000 * i)).
                                map(this::getIndexName).collect(Collectors.toList());
                    }
                };
            }
            return defaultIndexPartition;
        });

        layoutProvider = mock(GraphLayoutProvider.class);
        when(layoutProvider.getRedundantProperty(any(), any())).thenReturn(Optional.empty());

        ontology = OntologyTestUtils.createDragonsOntologyShort();
        ont = new Ontology.Accessor(ontology);
        graphElementSchemaProvider = new OntologySchemaProvider(ontology, physicalIndexProvider, layoutProvider);

        eBaseStatisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, ont, graphStatisticsProvider);
        statisticsCostEstimator = new StatisticsCostEstimator(
                (ont) -> eBaseStatisticsProvider,
                M1StepEstimator.getStepEstimator(1.0,0.001),
                (id) -> Optional.of(ont.get()));

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();


        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> globalPlanSelector = new CheapestPlanSelector();
        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> localPlanSelector = new AllCompletePlanSelector<>();

        planSearcher = new BottomUpPlanSearcher<>(
                new M1PlanExtensionStrategy(id -> Optional.of(ontology), (ont) -> physicalIndexProvider, (ont) -> layoutProvider),
                //new M1NonRedundantPlanExtensionStrategy(),
                pruneStrategy,
                pruneStrategy,
                globalPlanSelector,
                localPlanSelector,
                validator,
                statisticsCostEstimator);
    }

    private Statistics.HistogramStatistics<Date> createDateHistogram(long card, GraphElementSchema elementSchema, GraphElementPropertySchema graphElementPropertySchema,List<String> indices) {
        List<Statistics.BucketInfo<Date>> buckets = new ArrayList<>();
        if(elementSchema.getIndexPartition() instanceof TimeSeriesIndexPartition){
            TimeSeriesIndexPartition timeSeriesIndexPartition = (TimeSeriesIndexPartition) elementSchema.getIndexPartition();
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
                in(eProp(3, EProp.of(Integer.toString(FIRST_NAME.type), 3, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(eProp(7))),
                        rel(8, MEMBER_OF.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, GUILD.type).next(eProp(11)))).
                build();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(3).rel(8).relFilter(9).entity(10).entityFilter(11).goTo(1).rel(4).relFilter(5).entity(6).entityFilter(7).plan();
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        PlanAssert.assertEquals(expected, first.getPlan());
        Assert.assertEquals(43.36, first.getCost().getGlobalCost().cost, 0.1);
    }

    @Test
    public void testThreeEntityPathSourceConditionDestFilter(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(quant1(2, QuantType.all)).
                in(eProp(3, EProp.of(Integer.toString(FIRST_NAME.type), 3, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(eProp(7, EProp.of(Integer.toString(NAME.type),7, Constraint.of(ConstraintOp.eq,"abc"))))),
                        rel(8, MEMBER_OF.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, GUILD.type).next(eProp(11)))).
                build();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(3).rel(4).relFilter(5).entity(6).entityFilter(7).goTo(1).rel(8).relFilter(9).entity(10).entityFilter(11).plan();
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        PlanAssert.assertEquals(expected, first.getPlan());
        Assert.assertEquals(21.47, first.getCost().getGlobalCost().cost, 0.1);
    }


}
