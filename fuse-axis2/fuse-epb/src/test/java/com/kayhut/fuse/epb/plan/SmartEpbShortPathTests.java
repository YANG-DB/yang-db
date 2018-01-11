package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.dispatcher.epb.PlanSelector;
import com.kayhut.fuse.dispatcher.epb.PlanValidator;
import com.kayhut.fuse.epb.plan.estimation.CostEstimationConfig;
import com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.M1.M1PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.kayhut.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.kayhut.fuse.epb.plan.selectors.CheapestPlanSelector;
import com.kayhut.fuse.epb.plan.statistics.EBaseStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.kayhut.fuse.epb.plan.statistics.Statistics;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.epb.utils.PlanMockUtils;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.QuantType;
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
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 20/05/2017.
 */
public class SmartEpbShortPathTests {
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
        estimator = new RegexPatternCostEstimator(new M1PatternCostEstimator(
                new CostEstimationConfig(1.0, 0.001),
                (ont) -> eBaseStatisticsProvider,
                (id) -> Optional.of(ont.get())));

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();


        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> globalPlanSelector = new CheapestPlanSelector();
        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> localPlanSelector = new AllCompletePlanSelector<>();
        planSearcher = new BottomUpPlanSearcher<>(
                new M1PlanExtensionStrategy(id -> Optional.of(ont.get()), ont -> graphElementSchemaProvider),
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
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(plan.getCost().getGlobalCost().cost,400, 0.1);
        Assert.assertEquals(plan.getCost().getPlanStepCosts().iterator().next().getCost().getCost(), 400, 0.1);
    }

    @Test
    public void testSingleElementWithCondition() {
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2, EProp.of(FIRST_NAME.type, 2, Constraint.of(ConstraintOp.eq, "abc")))).
                build();

        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(2).plan();

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(plan.getCost().getGlobalCost().cost, 133d / 13d, 0.1);
        Assert.assertEquals(plan.getCost().getPlanStepCosts().iterator().next().getCost().getCost(), 133d / 13d, 0.1);
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

        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(5).entityFilter(6).rel(3, Rel.Direction.L).relFilter(4).entity(1).entityFilter(2).plan();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(2403, plan.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanWithCost<Plan, CountEstimatesCost>> iterator = plan.getCost().getPlanStepCosts().iterator();
        PlanWithCost<Plan, CountEstimatesCost> op = iterator.next();
        Assert.assertEquals(2000, op.getCost().getCost(), 0.1);
        Assert.assertEquals(3, iterator.next().getCost().getCost(), 0.1);
        Assert.assertEquals(400, iterator.next().getCost().getCost(), 0.1);
    }

    @Test
    public void testPathSelectionFilterToSide(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, DRAGON.type)).
                next(eProp(6, EProp.of(NAME.type,6, Constraint.of(ConstraintOp.eq,"abc")))).
                build();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(5).entityFilter(6).rel(3, L).relFilter(4).entity(1).entityFilter(2).plan();

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(111.1, plan.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanWithCost<Plan, CountEstimatesCost>> iterator = plan.getCost().getPlanStepCosts().iterator();
        PlanWithCost<Plan, CountEstimatesCost> op = iterator.next();
        Assert.assertEquals(10.09,op.getCost().getCost(), 0.1);
        Assert.assertEquals(0.1, iterator.next().getCost().getCost(), 0.1);
        Assert.assertEquals(100.9, iterator.next().getCost().getCost(), 0.1);
    }

    @Test
    public void testPathSelectionFilterFromSide(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2,EProp.of(FIRST_NAME.type,2, Constraint.of(ConstraintOp.eq,"abc")))).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, DRAGON.type)).
                next(eProp(6)).
                build();

        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(2).rel(3).relFilter(4).entity(5).entityFilter(6).plan();

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(112.6, plan.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanWithCost<Plan, CountEstimatesCost>> iterator = plan.getCost().getPlanStepCosts().iterator();
        PlanWithCost<Plan, CountEstimatesCost> op = iterator.next();
        Assert.assertEquals(133d/13d,op.getCost().getCost(), 0.1);
        Assert.assertEquals(0.1, iterator.next().getCost().getCost(), 0.1);
        Assert.assertEquals(102.3, iterator.next().getCost().getCost(), 0.1);
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

        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(2).rel(3).relFilter(4).entity(5).entityFilter(6).plan();

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(1401, plan.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanWithCost<Plan, CountEstimatesCost>> iterator = plan.getCost().getPlanStepCosts().iterator();
        PlanWithCost<Plan, CountEstimatesCost> op = iterator.next();
        Assert.assertEquals(400,op.getCost().getCost(), 0.1);
        Assert.assertEquals(1, iterator.next().getCost().getCost(), 0.1);
        Assert.assertEquals(1000, iterator.next().getCost().getCost(), 0.1);
    }


    @Test
    public void testFilterOnAllItems(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2, EProp.of(FIRST_NAME.type, 2, Constraint.of(ConstraintOp.ge, "g")))).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4, RelProp.of(START_DATE.type, 2, Constraint.of(ConstraintOp.ge, new Date(startTime)))))).
                next(typed(5, DRAGON.type)).
                next(eProp(6, EProp.of(NAME.type,6, Constraint.of(ConstraintOp.ge,"g")))).
                build();

        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(2).rel(3).relFilter(4).entity(5).entityFilter(6).plan();

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(1267, plan.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanWithCost<Plan, CountEstimatesCost>> iterator = plan.getCost().getPlanStepCosts().iterator();
        PlanWithCost<Plan, CountEstimatesCost> op = iterator.next();
        Assert.assertEquals(266,op.getCost().getCost(), 0.1);
        Assert.assertEquals(1, iterator.next().getCost().getCost(), 0.1);
        Assert.assertEquals(1000, iterator.next().getCost().getCost(), 0.1);
    }

    @Test
    public void testFilterOnAllItemsReverse(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2, EProp.of(FIRST_NAME.type, 2, Constraint.of(ConstraintOp.ge, "g")))).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4, RelProp.of(START_DATE.type, 2, Constraint.of(ConstraintOp.ge, new Date(startTime)))))).
                next(typed(5, DRAGON.type)).
                next(eProp(6, EProp.of(NAME.type,6, Constraint.of(ConstraintOp.eq,"abc")))).
                build();

        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(5).entityFilter(6).rel(3, L).relFilter(4).entity(1).entityFilter(2).plan();

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(111.1, plan.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanWithCost<Plan, CountEstimatesCost>> iterator = plan.getCost().getPlanStepCosts().iterator();
        PlanWithCost<Plan, CountEstimatesCost> op = iterator.next();
        Assert.assertEquals(10.09,op.getCost().getCost(), 0.1);
        Assert.assertEquals(0.1, iterator.next().getCost().getCost(), 0.1);
        Assert.assertEquals(100.9, iterator.next().getCost().getCost(), 0.1);
    }


    @Test
    public void testThreeEntityPathWithQuant(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(quant1(2, QuantType.all)).
                in(eProp(3, EProp.of(FIRST_NAME.type, 3, Constraint.of(ConstraintOp.eq, "abc"))),
                    rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                    next(typed(6, DRAGON.type)
                            .next(eProp(7))),
                    rel(8, MEMBER_OF.getrType(), Rel.Direction.R).below(relProp(9)).
                    next(typed(10, GUILD.type).next(eProp(11)))).
                build();

        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(3).rel(4).relFilter(5).entity(6).entityFilter(7).goTo(1).rel(8).relFilter(9).entity(10).entityFilter(11).plan();

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(112.8, plan.getCost().getGlobalCost().cost, 0.1);
    }


    @Test
    public void testThreeEntityPathWithQuantAndFilter(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(quant1(2, QuantType.all)).
                in(eProp(3, EProp.of(FIRST_NAME.type, 3, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(eProp(7, EProp.of(NAME.type,6, Constraint.of(ConstraintOp.eq,"abc"))))),
                        rel(8, MEMBER_OF.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, GUILD.type).next(eProp(11)))).
                build();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(3).rel(4).relFilter(5).entity(6).entityFilter(7).goTo(1).rel(8, Rel.Direction.R).relFilter(9).entity(10).entityFilter(11).plan();

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(23.91, plan.getCost().getGlobalCost().cost, 0.1);
    }

    @Test
    public void testBackUpdateOfEstimates(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(quant1(2, QuantType.all)).
                in(eProp(3, EProp.of(FIRST_NAME.type, 3, Constraint.of(ConstraintOp.eq, "abc"))),
                        rel(4, OWN.getrType(), Rel.Direction.R).below(relProp(5)).
                                next(typed(6, DRAGON.type)
                                        .next(eProp(7, EProp.of(NAME.type,6, Constraint.of(ConstraintOp.eq,"abc"))))),
                        rel(8, MEMBER_OF.getrType(), Rel.Direction.R).below(relProp(9)).
                                next(typed(10, GUILD.type).next(eProp(11)))).
                build();

        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(1).entityFilter(3).rel(4).relFilter(5).entity(6).entityFilter(7).goTo(1).rel(8, Rel.Direction.R).relFilter(9).entity(10).entityFilter(11).plan();

        PlanAssert.assertEquals(expected, plan.getPlan());
        PlanWithCost<Plan, CountEstimatesCost> opWithCost = plan.getCost().getPlanStepCosts().iterator().next();
        Assert.assertEquals(3, opWithCost.getCost().getCountEstimates().size());
        Iterator<Double> iterator = opWithCost.getCost().getCountEstimates().iterator();
        Assert.assertEquals(10.23, iterator.next(), 0.01);
        Assert.assertEquals(0.34, iterator.next(), 0.01);
        Assert.assertEquals(0.0008, iterator.next(), 0.01);
    }

    @Test
    public void testUnTyped(){
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragons").
                next(unTyped(1, PERSON.type, DRAGON.type)).
                next(eProp(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4, RelProp.of(START_DATE.type, 2, Constraint.of(ConstraintOp.ge, new Date(startTime)))))).
                next(typed(5, DRAGON.type)).
                next(eProp(6)).
                build();

        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);
        Plan expected = PlanMockUtils.PlanMockBuilder.mock(query).entity(5).entityFilter(6).rel(3, L).relFilter(4).entity(1).entityFilter(2).plan();

        Assert.assertNotNull(plan);
        PlanAssert.assertEquals(expected, plan.getPlan());
        Assert.assertEquals(3001, plan.getCost().getGlobalCost().cost, 0.1);
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
    //endregion

    //region Fields
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology.Accessor ont;
    private GraphStatisticsProvider graphStatisticsProvider;

    private EBaseStatisticsProvider eBaseStatisticsProvider;
    private RegexPatternCostEstimator estimator;

    protected BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;
    protected long startTime;

    private static String INDEX_PREFIX = "idx-";
    private static String INDEX_FORMAT = "idx-%s";
    private static String DATE_FORMAT_STRING = "yyyy-MM-dd-HH";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
    //endregion
}
