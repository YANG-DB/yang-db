package com.kayhut.fuse.epb.plan;

import com.google.common.collect.Iterables;
import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.cost.calculation.BasicStepEstimator;
import com.kayhut.fuse.epb.plan.extenders.M1NonRedundantPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.statistics.EBaseStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.epb.tests.PlanMockUtils;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartition;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartition;
import javaslang.collection.Stream;
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
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 20/05/2017.
 */
public class SmartEpbShortPathTests {

    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology.Accessor ont;
    private PhysicalIndexProvider physicalIndexProvider;
    private GraphStatisticsProvider graphStatisticsProvider;
    private GraphLayoutProvider layoutProvider;

    private EBaseStatisticsProvider eBaseStatisticsProvider;
    private StatisticsCostEstimator statisticsCostEstimator;

    protected BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;
    protected long startTime;

    private static String INDEX_PREFIX = "idx-";
    private static String INDEX_FORMAT = "idx-%s";
    private static String DATE_FORMAT_STRING = "yyyy-MM-dd-HH";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

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

        when(graphStatisticsProvider.getGlobalSelectivity(any(), any())).thenReturn(10l);
        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), isA(List.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgumentAt(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);
            GraphElementPropertySchema propertySchema = invocationOnMock.getArgumentAt(2, GraphElementPropertySchema.class);
            int card = typeCard.get(elementSchema.getType());
            if(propertySchema.getType().equals(STRING)){
                return createStringHistogram(card, indices.size());
            }
            if(propertySchema.getType().equals(INT)){
                return createLongHistogram(card, indices.size());
            }
            if(propertySchema.getType().equals(DATE)){
                return createDateHistogram(card, elementSchema,propertySchema, indices);
            }
            return null;
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), isA(String.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgumentAt(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);
            int card = typeCard.get(elementSchema.getType());
            return createStringHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), isA(Long.class))).thenAnswer(invocationOnMock -> {
            GraphElementSchema elementSchema = invocationOnMock.getArgumentAt(0, GraphElementSchema.class);
            List<String> indices = invocationOnMock.getArgumentAt(1, List.class);
            int card = typeCard.get(elementSchema.getType());
            return createLongHistogram(card, indices.size());
        });

        when(graphStatisticsProvider.getConditionHistogram(any(), any(), any(), any(), isA(Date.class))).thenAnswer(invocationOnMock -> {
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

        ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
        graphElementSchemaProvider = new OntologySchemaProvider(ont.get(), physicalIndexProvider, layoutProvider);

        eBaseStatisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, ont, graphStatisticsProvider);
        statisticsCostEstimator = new StatisticsCostEstimator(eBaseStatisticsProvider, graphElementSchemaProvider, ont, new BasicStepEstimator(1.0,0.001));

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();


        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> planSelector = new CheapestPlanSelector();

        planSearcher = new BottomUpPlanSearcher<>(
                new M1NonRedundantPlanExtensionStrategy(),
                pruneStrategy,
                pruneStrategy,
                planSelector,
                planSelector,
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

    @Test
    public void testSingleElementNoCondition(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2)).
                build();
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(2).plan();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        PlanAssert.assertEquals(expected, first.getPlan());
        Assert.assertEquals(first.getCost().getGlobalCost().cost,400, 0.1);
        Assert.assertEquals(first.getCost().getOpCosts().iterator().next().getCost().cost,400, 0.1);
    }

    @Test
    public void testSingleElementWithCondition() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2, EProp.of(Integer.toString(FIRST_NAME.type), 2, Constraint.of(ConstraintOp.eq, "abc")))).
                build();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(2).plan();
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        PlanAssert.assertEquals(expected, first.getPlan());
        Assert.assertEquals(first.getCost().getGlobalCost().cost, 133d / 13d, 0.1);
        Assert.assertEquals(first.getCost().getOpCosts().iterator().next().getCost().cost, 133d / 13d, 0.1);
    }

    @Test
    public void testPathSelectionNoConditions(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, DRAGON.type)).
                next(eProp(6)).
                build();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(5).entityFilter(6).rel(3, Rel.Direction.L).relFilter(4).entity(1).entityFilter(2).plan();
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        PlanAssert.assertEquals(expected, first.getPlan());
        Assert.assertEquals(2403, first.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(2000, op.getCost().cost, 0.1);
        Assert.assertEquals(3, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(400, iterator.next().getCost().cost, 0.1);
    }

    @Test
    public void testPathSelectionFilterToSide(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, DRAGON.type)).
                next(eProp(6, EProp.of(Integer.toString(NAME.type),6, Constraint.of(ConstraintOp.eq,"abc")))).
                build();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(5).entityFilter(6).rel(3, Rel.Direction.L).relFilter(4).entity(1).entityFilter(2).plan();
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        PlanAssert.assertEquals(expected, first.getPlan());
        Assert.assertEquals(111.1, first.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(10.09,op.getCost().cost, 0.1);
        Assert.assertEquals(0.1, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(100.9, iterator.next().getCost().cost, 0.1);
    }

    @Test
    public void testPathSelectionFilterFromSide(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2,EProp.of(Integer.toString(FIRST_NAME.type),2, Constraint.of(ConstraintOp.eq,"abc")))).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, DRAGON.type)).
                next(eProp(6)).
                build();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(2).rel(3).relFilter(4).entity(5).entityFilter(6).plan();
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        PlanAssert.assertEquals(expected, first.getPlan());
        Assert.assertEquals(112.6, first.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(133d/13d,op.getCost().cost, 0.1);
        Assert.assertEquals(0.1, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(102.3, iterator.next().getCost().cost, 0.1);
    }

    @Test
    public void testPathSelectionFilterOnRel(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4, RelProp.of(START_DATE.type, 2, Constraint.of(ConstraintOp.ge, new Date(startTime)))))).
                next(typed(5, DRAGON.type)).
                next(eProp(6)).
                build();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(2).rel(3).relFilter(4).entity(5).entityFilter(6).plan();
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        PlanAssert.assertEquals(expected, first.getPlan());
        Assert.assertEquals(1401, first.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(400,op.getCost().cost, 0.1);
        Assert.assertEquals(1, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(1000, iterator.next().getCost().cost, 0.1);
    }


    @Test
    public void testFilterOnAllItems(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2, EProp.of(Integer.toString(FIRST_NAME.type), 2, Constraint.of(ConstraintOp.ge, "g")))).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4, RelProp.of(START_DATE.type, 2, Constraint.of(ConstraintOp.ge, new Date(startTime)))))).
                next(typed(5, DRAGON.type)).
                next(eProp(6, EProp.of(Integer.toString(NAME.type),6, Constraint.of(ConstraintOp.ge,"g")))).
                build();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(2).rel(3).relFilter(4).entity(5).entityFilter(6).plan();
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        PlanAssert.assertEquals(expected, first.getPlan());
        Assert.assertEquals(1267, first.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(266,op.getCost().cost, 0.1);
        Assert.assertEquals(1, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(1000, iterator.next().getCost().cost, 0.1);
    }

    @Test
    public void testFilterOnAllItemsReverse(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2, EProp.of(Integer.toString(FIRST_NAME.type), 2, Constraint.of(ConstraintOp.ge, "g")))).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4, RelProp.of(START_DATE.type, 2, Constraint.of(ConstraintOp.ge, new Date(startTime)))))).
                next(typed(5, DRAGON.type)).
                next(eProp(6, EProp.of(Integer.toString(NAME.type),6, Constraint.of(ConstraintOp.eq,"abc")))).
                build();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(5).entityFilter(6).rel(3, Rel.Direction.L).relFilter(4).entity(1).entityFilter(2).plan();
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        PlanAssert.assertEquals(expected, first.getPlan());
        Assert.assertEquals(111.1, first.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(10.09,op.getCost().cost, 0.1);
        Assert.assertEquals(0.1, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(100.9, iterator.next().getCost().cost, 0.1);
    }


    @Test
    public void testThreeEntityPathWithQuant(){
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
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(3).rel(8, Rel.Direction.R).relFilter(9).entity(10).entityFilter(11).goTo(1).rel(4).relFilter(5).entity(6).entityFilter(7).plan();
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        PlanAssert.assertEquals(expected, first.getPlan());
        Assert.assertEquals(10.5, first.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(10.23,op.getCost().cost, 0.1);
        Assert.assertEquals(0.1, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(0.1, iterator.next().getCost().cost, 0.1);
    }


    @Test
    @Ignore
    public void testThreeEntityPathWithQuantAnFilter(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(quant1(2, QuantType.all)).
                in(eProp(3, EProp.of(Integer.toString(FIRST_NAME.type), 3, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(eProp(7, EProp.of(Integer.toString(NAME.type),6, Constraint.of(ConstraintOp.eq,"abc"))))),
                        rel(8, MEMBER_OF.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, GUILD.type).next(eProp(11)))).
                build();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(3).rel(4).relFilter(5).entity(6).entityFilter(7).goTo(1).rel(8, Rel.Direction.R).relFilter(9).entity(10).entityFilter(11).plan();
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        PlanAssert.assertEquals(expected, first.getPlan());
        Assert.assertEquals(10.5, first.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(10.23,op.getCost().cost, 0.1);
        Assert.assertEquals(0.1, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(0.1, iterator.next().getCost().cost, 0.1);
    }

}
