package com.yangdb.fuse.epb.plan.estimation.pattern;

import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.epb.plan.estimation.CostEstimationConfig;
import com.yangdb.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.yangdb.fuse.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import com.yangdb.fuse.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import com.yangdb.fuse.epb.plan.statistics.EBaseStatisticsProvider;
import com.yangdb.fuse.epb.plan.statistics.GraphStatisticsProvider;
import com.yangdb.fuse.epb.plan.statistics.Statistics;
import com.yangdb.fuse.epb.plan.statistics.StatisticsProvider;
import com.yangdb.fuse.epb.utils.GraphStatisticsProviderMock;
import com.yangdb.fuse.epb.utils.PlanMockUtils;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.OntologyTestUtils.DRAGON;
import com.yangdb.fuse.model.OntologyTestUtils.PERSON;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.*;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static com.yangdb.fuse.epb.utils.PlanMockUtils.Type.CONCRETE;
import static com.yangdb.fuse.epb.utils.PlanMockUtils.Type.TYPED;
import static com.yangdb.fuse.model.OntologyTestUtils.*;
import static com.yangdb.fuse.model.execution.plan.Direction.out;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 5/16/2017.
 */
public class BasicPatternCostEstimatorWithStatisticsProviderTest {
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology.Accessor ont;

    @Before
    public void setup(){
        graphElementSchemaProvider = mock(GraphElementSchemaProvider.class);
        GraphEdgeSchema graphEdgeSchema = mock(GraphEdgeSchema.class);
        when(graphEdgeSchema.getProperty(any()))
                .thenAnswer(invocationOnMock -> Optional.of(new GraphElementPropertySchema.Impl(
                        invocationOnMock.getArguments()[0].toString(),
                        invocationOnMock.getArguments()[0].toString())));
        when(graphEdgeSchema.getIndexPartitions())
                .thenReturn(Optional.of(new StaticIndexPartitions(Collections.singleton("index"))));
        GraphEdgeSchema.End edgeEnd = mock(GraphEdgeSchema.End.class);
        when(edgeEnd.getRedundantProperty(any())).thenAnswer(invocationOnMock -> {
            String property = (String)invocationOnMock.getArguments()[0];
            if(property.equals("lastName")){
                return Optional.of(new GraphRedundantPropertySchema.Impl("lastName", "entityB.lastName", "string"));
            }


            return Optional.empty();


        });
        when(graphEdgeSchema.getEndB()).thenReturn(Optional.of(edgeEnd));
        when(graphElementSchemaProvider.getEdgeSchemas(any())).thenReturn(Collections.singletonList(graphEdgeSchema));
        ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
    }


    @Test
    @Ignore("No more single op pattern - all patterns are now with ***OpFilter along the entity / Relation")
    public void calculateEntityOnlyPattern() throws Exception {
        StatisticsProvider provider = new EBaseStatisticsProvider(graphElementSchemaProvider, ont, getStatisticsProvider(PlanMockUtils.PlanMockBuilder.mock()));
        PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> estimator =
                new M1PatternCostEstimator(new CostEstimationConfig(1, 0.001), (ont) -> provider, new OntologyProvider() {
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
                });

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withOnt(ont.get().getOnt()).build();


        HashMap<RegexPatternCostEstimator.PatternPart, PlanOp> map = new HashMap<>();
        EntityOp entityOp = new EntityOp();
        entityOp.setAsgEbase(new AsgEBase<>(new EConcrete()));
        map.put(RegexPatternCostEstimator.PatternPart.ENTITY_ONLY, entityOp);
        PatternCostEstimator.Result<Plan, CountEstimatesCost> estimate = estimator.estimate(Pattern.buildEntityPattern(map), new IncrementalEstimationContext<>(Optional.empty(), query));
        List<PlanWithCost<Plan, CountEstimatesCost>> costs = estimate.getPlanStepCosts();

        Assert.assertNotNull(costs);
        Assert.assertEquals(costs.size(),1);
        Assert.assertEquals(costs.get(0).getPlan().getOps().size(),2);
        Assert.assertTrue(costs.get(0).getPlan().getOps().get(0) instanceof EntityOp);
        Assert.assertTrue(costs.get(0).getPlan().getOps().get(1) instanceof EntityFilterOp);
        Assert.assertEquals(costs.get(0).getCost().getCost(), 1, 0);
    }

    @Test
    public void calculateFullStepNotNull() throws Exception {
        PlanMockUtils.PlanMockBuilder builder = PlanMockUtils.PlanMockBuilder.mock().entity(TYPED, 100, PERSON.type)
                .entityFilter(0.2,7,FIRST_NAME.type, Constraint.of(ConstraintOp.eq, "equals")).startNewPlan()
                .rel(out, OWN.getrType(), 100)
                .relFilter(0.6,11,START_DATE.type,Constraint.of(ConstraintOp.ge, "gt"))
                .entity(CONCRETE, 1, DRAGON.type)
                .entityFilter(1,12, NAME.type, Constraint.of(ConstraintOp.inSet, "inSet"));
        PlanWithCost<Plan, PlanDetailedCost> oldPlan = builder.oldPlanWithCost(50, 250);
        Plan plan = builder.plan();
        StatisticsProvider provider = new EBaseStatisticsProvider(graphElementSchemaProvider, ont, getStatisticsProvider(builder));

        PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> estimator =
                new M1PatternCostEstimator(new CostEstimationConfig(1, 0.001), (ont) -> provider, new OntologyProvider() {
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
                });

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withOnt(ont.get().getOnt()).build();

        HashMap<RegexPatternCostEstimator.PatternPart, PlanOp> map = new HashMap<>();
        int numOps = plan.getOps().size();
        map.put(RegexPatternCostEstimator.PatternPart.ENTITY_ONE, plan.getOps().get(numOps-6));
        map.put(RegexPatternCostEstimator.PatternPart.OPTIONAL_ENTITY_ONE_FILTER, plan.getOps().get(numOps-5));
        map.put(RegexPatternCostEstimator.PatternPart.RELATION, plan.getOps().get(numOps-4));
        map.put(RegexPatternCostEstimator.PatternPart.OPTIONAL_REL_FILTER, plan.getOps().get(numOps-3));
        map.put(RegexPatternCostEstimator.PatternPart.ENTITY_TWO, plan.getOps().get(numOps-2));
        map.put(RegexPatternCostEstimator.PatternPart.OPTIONAL_ENTITY_TWO_FILTER, plan.getOps().get(numOps-1));
        PatternCostEstimator.Result estimate = estimator.estimate(Pattern.buildEntityRelationEntityPattern(map), new IncrementalEstimationContext<>(Optional.of(oldPlan), query));
        Assert.assertNotNull(estimate);
    }

    private GraphStatisticsProvider getStatisticsProvider(PlanMockUtils.PlanMockBuilder builder) {
        GraphStatisticsProvider mock = GraphStatisticsProviderMock.mock(builder);
        when(mock.getEdgeCardinality(any())).thenReturn(new Statistics.SummaryStatistics(100,10));
        when(mock.getEdgeCardinality(any(),any())).thenReturn(new Statistics.SummaryStatistics(100,10));
        when(mock.getVertexCardinality(any())).thenReturn(new Statistics.SummaryStatistics(100,10));
        when(mock.getVertexCardinality(any(),any())).thenReturn(new Statistics.SummaryStatistics(100,10));
        return mock;
    }

}