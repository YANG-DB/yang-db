package com.yangdb.fuse.epb.plan.estimation.pattern;

import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.epb.plan.estimation.CostEstimationConfig;
import com.yangdb.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.yangdb.fuse.epb.plan.estimation.pattern.estimators.M1PatternCostEstimator;
import com.yangdb.fuse.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import com.yangdb.fuse.epb.plan.statistics.StatisticsProvider;
import com.yangdb.fuse.epb.utils.PlanMockUtils;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanOp;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static com.yangdb.fuse.epb.utils.PlanMockUtils.Type.TYPED;
import static com.yangdb.fuse.epb.utils.StatisticsMockUtils.build;
import static com.yangdb.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.concrete;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.ePropGroup;
import static com.yangdb.fuse.model.execution.plan.Direction.out;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 5/16/2017.
 */
public class BasicPatternCostEstimatorTest {
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology.Accessor ont;

    @Before
    public void setup(){
        graphElementSchemaProvider = mock(GraphElementSchemaProvider.class);
        GraphEdgeSchema graphEdgeSchema = mock(GraphEdgeSchema.class);
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
        StatisticsProvider provider = build(Collections.emptyMap(), Integer.MAX_VALUE);
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


        HashMap<RegexPatternCostEstimator.PatternPart, PlanOp> map = new HashMap<>();
        AsgQuery query = AsgQuery.Builder.start("name", "ont").
                next(concrete(1, "id", "4", "name", "A").next(AsgQuery.Builder.ePropGroup(101, EProp.of(9, "12", Constraint.of(gt, MALE))))).build();
        Plan plan = new Plan().withOp(new EntityOp(AsgQueryUtil.element$(query, 1))).withOp(new EntityFilterOp(AsgQueryUtil.element$(query, 101)));


        map.put(RegexPatternCostEstimator.PatternPart.ENTITY_ONLY, plan.getOps().get(0));
        PatternCostEstimator.Result<Plan, CountEstimatesCost> result = estimator.estimate(Pattern.buildEntityPattern(map), new IncrementalEstimationContext<>(Optional.empty(), query));
        List<PlanWithCost<Plan, CountEstimatesCost>> costs = result.getPlanStepCosts();

        Assert.assertNotNull(costs);
        Assert.assertEquals(costs.size(), 1);
        Assert.assertEquals(costs.get(0).getPlan().getOps().size(), 2);
        Assert.assertTrue(costs.get(0).getPlan().getOps().get(0) instanceof EntityOp);
        Assert.assertTrue(costs.get(0).getPlan().getOps().get(1) instanceof EntityFilterOp);
        Assert.assertEquals(costs.get(0).getCost().getCost(), 1, 0);
    }

    @Test
    public void calculateFullStep() throws Exception {
        PlanMockUtils.PlanMockBuilder builder = PlanMockUtils.PlanMockBuilder.mock().entity(TYPED, 100, "4")
                .entityFilter(0.2,7,"6", Constraint.of(ConstraintOp.eq, "equals")).startNewPlan()
                .rel(out, "1", 1000).relFilter(0.4,11,"11",Constraint.of(ConstraintOp.ge, "gt"))
                .entity(TYPED, 50, "5").entityFilter(0.1,12,"9", Constraint.of(ConstraintOp.inSet, "inSet"));
        PlanWithCost<Plan, PlanDetailedCost> oldPlan = builder.oldPlanWithCost(50, 250);
        Plan plan = builder.plan();
        StatisticsProvider provider = build(builder.statistics(), 1000);

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
        PatternCostEstimator.Result<Plan, CountEstimatesCost> result = estimator.estimate(Pattern.buildEntityRelationEntityPattern(map), new IncrementalEstimationContext<>(Optional.of(oldPlan), query));

        Assert.assertEquals(result.getPlanStepCosts().get(0).getCost().getCost(), 20, 0.1);
        Assert.assertEquals(0.4, result.getPlanStepCosts().get(1).getCost().getCost(), 0.1);
        Assert.assertEquals(50, result.getPlanStepCosts().get(2).getCost().getCost(), 0.1);

        Assert.assertEquals(1, result.countsUpdateFactors().length);
        Assert.assertEquals(1.0, result.countsUpdateFactors()[0], 0.1);
    }

}