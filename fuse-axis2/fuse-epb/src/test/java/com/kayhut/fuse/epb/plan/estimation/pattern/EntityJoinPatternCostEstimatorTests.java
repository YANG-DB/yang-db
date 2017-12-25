package com.kayhut.fuse.epb.plan.estimation.pattern;

import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.EntityJoinPatternCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.JoinCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.EProp;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.END_DATE;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.ConstraintOp.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntityJoinPatternCostEstimatorTests {
    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        long time = System.currentTimeMillis();
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(rel(2, OWN.getrType(), R).below(relProp(10, of(START_DATE.type, 10, Constraint.of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of(NAME.type, 9, Constraint.of(eq, "smith")), EProp.of(GENDER.type, 9, Constraint.of(gt, MALE)))
                        , rel(5, FREEZE.getrType(), R)
                                .next(unTyped(6))
                        , rel(7, FIRE.getrType(), R)
                                .below(relProp(11, of(START_DATE.type, 11,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(END_DATE.type, 11, Constraint.of(le, new Date(time + 1000 * 60)))))
                                .next(concrete(8, "smoge", DRAGON.type, "Display:smoge", "D"))
                )
                .build();
    }

    public static AsgQuery simpleQuery1(){
        return AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, PERSON.type)).
                next(eProp(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, DRAGON.type)).
                next(eProp(6)).
                next(rel(7, OWN.getrType(), Rel.Direction.R).below(relProp(8))).
                next(typed(9, DRAGON.type)).
                next(eProp(10)).
                build();
    }


    @Test
    public void testNewJoinPattern(){
        AsgQuery asgQuery = simpleQuery2("q","o");
        CostEstimator< Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery >> estimatorMock = mock(CostEstimator.class);
        EntityJoinPattern pattern = new EntityJoinPattern(new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)))
                , new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery,3)))));
        List<PlanWithCost<Plan, CountEstimatesCost>> rightCosts = pattern.getEntityJoinOp().getRightBranch().getOps().stream().map(op -> new PlanWithCost<>(new Plan(op), new CountEstimatesCost(100, 10))).collect(Collectors.toList());
        when(estimatorMock.estimate(any(), any())).thenReturn(new PlanWithCost<>(pattern.getEntityJoinOp().getRightBranch(), new PlanDetailedCost(new DoubleCost(50), rightCosts)));

        EntityJoinPatternCostEstimator estimator = new EntityJoinPatternCostEstimator();
        estimator.setCostEstimator(estimatorMock);

        List<PlanWithCost<Plan, CountEstimatesCost>> leftCosts = pattern.getEntityJoinOp().getLeftBranch().getOps().stream().map(op -> new PlanWithCost<>(new Plan(op), new CountEstimatesCost(100, 10))).collect(Collectors.toList());
        PatternCostEstimator.Result<Plan, CountEstimatesCost> estimate = estimator.estimate(pattern, new IncrementalEstimationContext<>(Optional.of(new PlanWithCost<Plan, PlanDetailedCost>(pattern.getEntityJoinOp().getLeftBranch(), new PlanDetailedCost(new DoubleCost(100), leftCosts))), asgQuery));

        Assert.assertEquals(1,estimate.getPlanStepCosts().size());
        Assert.assertEquals(0,estimate.getPlanStepCosts().get(0).getCost().getCost(),0.001);
        Assert.assertEquals(0,estimate.getPlanStepCosts().get(0).getCost().peek(),0.001);
    }

    @Test
    public void testJoinCostCalculation(){
        AsgQuery query = simpleQuery1();

        EntityJoinPattern pattern = new EntityJoinPattern(
                new EntityJoinOp(
                        new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                                new EntityFilterOp(AsgQueryUtil.element$(query,2)),
                                new RelationOp(AsgQueryUtil.element$(query, 3)),
                                new RelationFilterOp(AsgQueryUtil.element$(query,4)),
                                new EntityOp(AsgQueryUtil.element$(query, 5)),
                                new EntityFilterOp(AsgQueryUtil.element$(query,6)))
                , new Plan(new EntityOp(AsgQueryUtil.element$(query, 9)),
                        new EntityFilterOp(AsgQueryUtil.element$(query,10)),
                        new RelationOp(AsgQueryUtil.element$(query, 7)),
                        new RelationFilterOp(AsgQueryUtil.element$(query,8)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new EntityFilterOp(AsgQueryUtil.element$(query,6)))
                ));

        CostEstimator< Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery >> estimatorMock = mock(CostEstimator.class);

        EntityJoinPatternCostEstimator estimator = new EntityJoinPatternCostEstimator();
        estimator.setCostEstimator(estimatorMock);
        List<PlanWithCost<Plan, CountEstimatesCost>> rightCosts = pattern.getEntityJoinOp().getRightBranch().getOps().stream().map(op -> new PlanWithCost<>(new Plan(op), new CountEstimatesCost(100 , 10))).collect(Collectors.toList());
        for (int i = 0; i < rightCosts.size(); i++) {
            PlanWithCost<Plan, CountEstimatesCost> cost = rightCosts.get(i);
            cost.setCost(new CountEstimatesCost(cost.getCost().getCost() + i, cost.getCost().peek() + i));
        }

        when(estimatorMock.estimate(any(), any())).thenReturn(new PlanWithCost<>(pattern.getEntityJoinOp().getRightBranch(), new PlanDetailedCost(new DoubleCost(50), rightCosts)));

        List<PlanWithCost<Plan, CountEstimatesCost>> leftCosts = pattern.getEntityJoinOp().getLeftBranch().getOps().stream().map(op -> new PlanWithCost<>(new Plan(op), new CountEstimatesCost(1000, 100))).collect(Collectors.toList());
        for (int i = 0; i < leftCosts.size(); i++) {
            PlanWithCost<Plan, CountEstimatesCost> cost = leftCosts.get(i);
            cost.setCost(new CountEstimatesCost(cost.getCost().getCost() + i, cost.getCost().peek() + i));
        }
        PatternCostEstimator.Result<Plan, CountEstimatesCost> estimate = estimator.estimate(pattern, new IncrementalEstimationContext<>(Optional.of(new PlanWithCost<Plan, PlanDetailedCost>(pattern.getEntityJoinOp().getLeftBranch(), new PlanDetailedCost(new DoubleCost(100), leftCosts))), query));

        Assert.assertEquals(14, estimate.getPlanStepCosts().get(0).getCost().peek(), 0.0001);
        Assert.assertEquals(118, estimate.getPlanStepCosts().get(0).getCost().getCost(), 0.0001);
    }

    @Test
    public void testJoinCostCalculationExpansion(){
        AsgQuery query = simpleQuery1();

        EntityJoinPattern pattern = new EntityJoinPattern(
                new EntityJoinOp(
                        new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                                new EntityFilterOp(AsgQueryUtil.element$(query,2)),
                                new RelationOp(AsgQueryUtil.element$(query, 3)),
                                new RelationFilterOp(AsgQueryUtil.element$(query,4)),
                                new EntityOp(AsgQueryUtil.element$(query, 5)),
                                new EntityFilterOp(AsgQueryUtil.element$(query,6)))
                        , new Plan(new EntityOp(AsgQueryUtil.element$(query, 9)),
                        new EntityFilterOp(AsgQueryUtil.element$(query,10)),
                        new RelationOp(AsgQueryUtil.element$(query, 7)),
                        new RelationFilterOp(AsgQueryUtil.element$(query,8)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new EntityFilterOp(AsgQueryUtil.element$(query,6)))
                ));

        EntityJoinOp prevJoin = new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                        new EntityFilterOp(AsgQueryUtil.element$(query, 2)),
                        new RelationOp(AsgQueryUtil.element$(query, 3)),
                        new RelationFilterOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new EntityFilterOp(AsgQueryUtil.element$(query, 6)))
                , new Plan(new EntityOp(AsgQueryUtil.element$(query, 9)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 10)))
        );

        CostEstimator< Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery >> estimatorMock = mock(CostEstimator.class);

        EntityJoinPatternCostEstimator estimator = new EntityJoinPatternCostEstimator();
        estimator.setCostEstimator(estimatorMock);
        List<PlanWithCost<Plan, CountEstimatesCost>> rightCosts = pattern.getEntityJoinOp().getRightBranch().getOps().stream().map(op -> new PlanWithCost<>(new Plan(op), new CountEstimatesCost(100 , 10))).collect(Collectors.toList());
        for (int i = 0; i < rightCosts.size(); i++) {
            PlanWithCost<Plan, CountEstimatesCost> cost = rightCosts.get(i);
            cost.setCost(new CountEstimatesCost(cost.getCost().getCost() + i, cost.getCost().peek() + i));
        }

        when(estimatorMock.estimate(any(), any())).thenReturn(new PlanWithCost<>(pattern.getEntityJoinOp().getRightBranch(), new PlanDetailedCost(new DoubleCost(50), rightCosts)));

        List<PlanWithCost<Plan, CountEstimatesCost>> leftCosts = pattern.getEntityJoinOp().getLeftBranch().getOps().stream().map(op -> new PlanWithCost<>(new Plan(op), new CountEstimatesCost(1000, 100))).collect(Collectors.toList());
        for (int i = 0; i < leftCosts.size(); i++) {
            PlanWithCost<Plan, CountEstimatesCost> cost = leftCosts.get(i);
            cost.setCost(new CountEstimatesCost(cost.getCost().getCost() + i, cost.getCost().peek() + i));
        }
        IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> context = new IncrementalEstimationContext<>(
                Optional.of(new PlanWithCost<>(new Plan(prevJoin), new PlanDetailedCost(new DoubleCost(1000),
                        Collections.singleton(new PlanWithCost<>(new Plan(prevJoin), new JoinCost(0,0,new PlanDetailedCost(new DoubleCost(100), leftCosts),
                                new PlanDetailedCost(new DoubleCost(100), rightCosts))))))),
                query);

        PatternCostEstimator.Result<Plan, CountEstimatesCost> estimate = estimator.estimate(pattern, context);

        Assert.assertEquals(14, estimate.getPlanStepCosts().get(0).getCost().peek(), 0.0001);
        Assert.assertEquals(118, estimate.getPlanStepCosts().get(0).getCost().getCost(), 0.0001);
    }
}
