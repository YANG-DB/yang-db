package com.kayhut.fuse.epb.plan.cost.calculation;

import com.kayhut.fuse.epb.plan.cost.StatisticsCostEstimator;
import com.kayhut.fuse.epb.plan.statistics.StatisticsProvider;
import com.kayhut.fuse.epb.tests.PlanMockUtils;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import javaslang.Tuple2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.epb.tests.PlanMockUtils.Type.TYPED;
import static com.kayhut.fuse.epb.tests.StatisticsMockUtils.build;
import static com.kayhut.fuse.model.execution.plan.Direction.out;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 5/16/2017.
 */
public class BasicStepEstimatorTest {
    private GraphElementSchemaProvider graphElementSchemaProvider;
    private Ontology ontology;

    @Before
    public void setup(){
        graphElementSchemaProvider = mock(GraphElementSchemaProvider.class);
        GraphEdgeSchema graphEdgeSchema = mock(GraphEdgeSchema.class);
        GraphEdgeSchema.End edgeEnd = mock(GraphEdgeSchema.End.class);
        when(edgeEnd.getRedundantVertexProperty(any())).thenAnswer(invocationOnMock -> {
            String property = (String)invocationOnMock.getArguments()[0];
            if(property.equals("lastName")){
                return Optional.of(new GraphRedundantPropertySchema() {
                    @Override
                    public String getName() {
                        return "lastName";
                    }

                    @Override
                    public String getType() {
                        return "string";
                    }

                    @Override
                    public String getPropertyRedundantName() {
                        return "entityB.lastName";
                    }
                });
            }


            return Optional.empty();


        });
        when(graphEdgeSchema.getDestination()).thenReturn(Optional.of(edgeEnd));
        when(graphElementSchemaProvider.getEdgeSchema(any())).thenReturn(Optional.of(graphEdgeSchema));
        ontology = OntologyTestUtils.createDragonsOntologyShort();
    }

    @Test
    public void calculateEntityOnlyPattern() throws Exception {
        BasicStepEstimator estimator = new BasicStepEstimator(1);
        StatisticsProvider provider = build(Collections.emptyMap(), Integer.MAX_VALUE);

        HashMap<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map = new HashMap<>();
        EntityOp entityOp = new EntityOp();
        entityOp.setAsgEBase(new AsgEBase<>(new EConcrete()));
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.ENTITY_ONLY, entityOp);
        StepEstimator.StepEstimatorResult result = estimator.calculate(provider, map, StatisticsCostEstimator.StatisticsCostEstimatorPatterns.SINGLE_MODE, Optional.empty());
        List<PlanOpWithCost<Cost>> costs = result.planOpWithCosts();

        Assert.assertNotNull(costs);
        Assert.assertEquals(costs.size(),1);
        Assert.assertEquals(costs.get(0).getOpBase().size(),2);
        Assert.assertTrue(costs.get(0).getOpBase().get(0) instanceof EntityOp);
        Assert.assertTrue(costs.get(0).getOpBase().get(1) instanceof EntityFilterOp);
        Assert.assertEquals(costs.get(0).getCost().cost,1,0);
    }

    @Test
    public void calculateFullStep() throws Exception {
        BasicStepEstimator estimator = new BasicStepEstimator(1);
        PlanMockUtils.PlanMockBuilder builder = PlanMockUtils.PlanMockBuilder.mock().entity(TYPED, 100, 4)
                .entityFilter(0.2,7,"6", Constraint.of(ConstraintOp.eq, "equals")).startNewPlan()
                .rel(out, 1, 1000).relFilter(0.4,11,"11",Constraint.of(ConstraintOp.ge, "gt"))
                .entity(TYPED, 50, 5).entityFilter(0.1,12,"9", Constraint.of(ConstraintOp.inSet, "inSet"));
        PlanWithCost<Plan, PlanDetailedCost> oldPlan = builder.planWithCost(50, 250);
        Plan plan = builder.plan();
        StatisticsProvider provider = build(builder.statistics(), 1000);

        HashMap<StatisticsCostEstimator.StatisticsCostEstimatorNames, PlanOpBase> map = new HashMap<>();
        int numOps = plan.getOps().size();
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.ENTITY_ONE, plan.getOps().get(numOps-6));
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.OPTIONAL_ENTITY_ONE_FILTER, plan.getOps().get(numOps-5));
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.RELATION, plan.getOps().get(numOps-4));
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.OPTIONAL_REL_FILTER, plan.getOps().get(numOps-3));
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.ENTITY_TWO, plan.getOps().get(numOps-2));
        map.put(StatisticsCostEstimator.StatisticsCostEstimatorNames.OPTIONAL_ENTITY_TWO_FILTER, plan.getOps().get(numOps-1));
        StepEstimator.StepEstimatorResult result = estimator.calculate(provider, map, StatisticsCostEstimator.StatisticsCostEstimatorPatterns.FULL_STEP, Optional.of(oldPlan));
        Assert.assertEquals(result.planOpWithCosts().get(0).getCost().cost,20,0.1);
        Assert.assertEquals(result.planOpWithCosts().get(1).getCost().cost,200,0.1);
        Assert.assertEquals(result.planOpWithCosts().get(2).getCost().cost,20,0.1);
        Assert.assertEquals(result.lambda(),0.2,0.1);//lambda
    }

}