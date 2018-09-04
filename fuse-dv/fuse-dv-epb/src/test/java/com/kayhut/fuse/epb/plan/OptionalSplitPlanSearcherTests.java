package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.execution.plan.PlanAssert;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.OptionalOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityNoOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.Value;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Date;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.concrete;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.ePropGroup;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.*;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.mockito.Matchers.any;

public class OptionalSplitPlanSearcherTests {

    private AsgQuery queryMultiOptional(){
        return AsgQuery.Builder.start("q", "O")
                .next(typed(1, OntologyTestUtils.PERSON.type)
                        .next(AsgQuery.Builder.ePropGroup(2,EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189)))))
                .next(rel(4, OWN.getrType(), R)
                        .below(relProp(5, of(6, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(7, OntologyTestUtils.DRAGON.type))
                .next(quant1(8, all))
                .in(AsgQuery.Builder.ePropGroup(9, EProp.of(10, NAME.type, Constraint.of(eq, "smith")), EProp.of(11, GENDER.type, Constraint.of(gt, new Value(MALE.ordinal(),MALE.name()))))
                        , optional(50).next(rel(12, FREEZE.getrType(), R)
                                .next(unTyped(13)
                                        .next(AsgQuery.Builder.ePropGroup(14,EProp.of(15, NAME.type, Constraint.of(ConstraintOp.notContains, "bob"))))
                                ))
                        , optional(60).next(rel(16, FIRE.getrType(), R)
                        .next(concrete(20, "smoge", DRAGON.type, "Display:smoge", "D")
                            .next(AsgQuery.Builder.ePropGroup(21,EProp.of(22, NAME.type, Constraint.of(ConstraintOp.eq, "smoge"))))
                        ))
                )
                .build();
    }

    private AsgQuery querySingleOptional(){
        return AsgQuery.Builder.start("q", "O")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(AsgQuery.Builder.ePropGroup(2,EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                .next(optional(50).next(rel(12, FREEZE.getrType(), R)
                                .next(unTyped(13)
                                        .next(AsgQuery.Builder.ePropGroup(14,EProp.of(15, NAME.type, Constraint.of(ConstraintOp.notContains, "bob"))))
                ))).build();
    }

    private AsgQuery queryNoOptional(){
        return AsgQuery.Builder.start("q", "O")
                .next(typed(1, OntologyTestUtils.PERSON.type)
                        .next(AsgQuery.Builder.ePropGroup(2,EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189)))))
                .build();
    }

    @Test
    public void testNoOptional(){
        PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcherMock = Mockito.mock(PlanSearcher.class);
        PlanWithCost<Plan, PlanDetailedCost> expectedPlan = new PlanWithCost<>(
                new Plan(new EntityOp()),
                new PlanDetailedCost(new DoubleCost(1), Collections.emptyList()));
        Mockito.when(planSearcherMock.search(any())).thenReturn(expectedPlan);

        OptionalSplitPlanSearcher planSearcher = new OptionalSplitPlanSearcher(planSearcherMock, null);
        AsgQuery query = queryNoOptional();
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = planSearcher.search(query);

        Assert.assertNotNull(planWithCost);
        Assert.assertEquals(expectedPlan, planWithCost);
    }

    @Test
    public void testSingleOptional(){
        AsgQuery query = querySingleOptional();
        PlanSearcher<Plan, PlanDetailedCost, AsgQuery> mainPlanSearcherMock = Mockito.mock(PlanSearcher.class);
        Plan expectedPlan = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)), new EntityFilterOp(AsgQueryUtil.element$(query, 2)));
        PlanDetailedCost planDetailedCost = new PlanDetailedCost(new DoubleCost(10), Stream.of(new PlanWithCost(expectedPlan, new CountEstimatesCost(10,10))));
        Mockito.when(mainPlanSearcherMock.search(any())).thenReturn(new PlanWithCost<>(expectedPlan, planDetailedCost));

        PlanSearcher<Plan, PlanDetailedCost, AsgQuery> optionalPlanSearcherMock = Mockito.mock(PlanSearcher.class);
        Plan expectedOptionalPlan = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                new RelationOp(AsgQueryUtil.element$(query, 12)),
                new EntityOp(AsgQueryUtil.element$(query, 13)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 14)));
        PlanDetailedCost optionalPlanDetailedCost = new PlanDetailedCost(new DoubleCost(10), Stream.of(new PlanWithCost(expectedOptionalPlan, new CountEstimatesCost(10,10))));
        Mockito.when(optionalPlanSearcherMock.search(any())).thenReturn(new PlanWithCost<>(expectedOptionalPlan, optionalPlanDetailedCost));

        Plan expectedCompletePlan = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 2)),
                new GoToEntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 50),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 12)),
                        new EntityOp(AsgQueryUtil.element$(query, 13)),
                        new EntityFilterOp(AsgQueryUtil.element$(query, 14)
                        ))
                );



        OptionalSplitPlanSearcher planSearcher = new OptionalSplitPlanSearcher(mainPlanSearcherMock, optionalPlanSearcherMock);
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = planSearcher.search(query);
        PlanAssert.assertEquals(expectedCompletePlan, planWithCost.getPlan());
    }

    @Test
    public void testMultiOptional(){
        AsgQuery query = queryMultiOptional();
        PlanSearcher<Plan, PlanDetailedCost, AsgQuery> mainPlanSearcherMock = Mockito.mock(PlanSearcher.class);
        Plan expectedPlan = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 2)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 7)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));

        PlanDetailedCost planDetailedCost = new PlanDetailedCost(new DoubleCost(10), Stream.of(new PlanWithCost(expectedPlan, new CountEstimatesCost(10,10))));
        Mockito.when(mainPlanSearcherMock.search(any())).thenReturn(new PlanWithCost<>(expectedPlan, planDetailedCost));

        PlanSearcher<Plan, PlanDetailedCost, AsgQuery> optionalPlanSearcherMock = Mockito.mock(PlanSearcher.class);
        Plan expectedOptionalPlan1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 7)),
                new RelationOp(AsgQueryUtil.element$(query, 12)),
                new EntityOp(AsgQueryUtil.element$(query, 13)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 14)));
        PlanDetailedCost optionalPlanDetailedCost1 = new PlanDetailedCost(new DoubleCost(10), Stream.of(new PlanWithCost(expectedOptionalPlan1, new CountEstimatesCost(10,10))));


        Plan expectedOptionalPlan2 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 7)),
                new RelationOp(AsgQueryUtil.element$(query, 16)),
                new EntityOp(AsgQueryUtil.element$(query, 20)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 21)));
        PlanDetailedCost optionalPlanDetailedCost2 = new PlanDetailedCost(new DoubleCost(10), Stream.of(new PlanWithCost(expectedOptionalPlan2, new CountEstimatesCost(10,10))));

        Mockito.when(optionalPlanSearcherMock.search(any())).thenAnswer(invocationOnMock -> {
            AsgQuery asgQuery = invocationOnMock.getArgumentAt(0, AsgQuery.class);
            if(Stream.ofAll(asgQuery.getElements()).last().geteNum() == 14){
                return new PlanWithCost<>(expectedOptionalPlan1, optionalPlanDetailedCost1);
            }
            return new PlanWithCost<>(expectedOptionalPlan2, optionalPlanDetailedCost2);
        });

        Plan expectedCompletePlan = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 2)),
                new RelationOp(AsgQueryUtil.element$(query, 4)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 7)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)),
                new GoToEntityOp(AsgQueryUtil.element$(query, 7)),
                new OptionalOp(AsgQueryUtil.element$(query, 50),
                        new EntityNoOp(AsgQueryUtil.element$(query, 7)),
                        new RelationOp(AsgQueryUtil.element$(query, 12)),
                        new EntityOp(AsgQueryUtil.element$(query, 13)),
                        new EntityFilterOp(AsgQueryUtil.element$(query, 14)
                        )),
                new GoToEntityOp(AsgQueryUtil.element$(query, 7)),
                new OptionalOp(AsgQueryUtil.element$(query, 60),
                        new EntityNoOp(AsgQueryUtil.element$(query, 7)),
                        new RelationOp(AsgQueryUtil.element$(query, 16)),
                        new EntityOp(AsgQueryUtil.element$(query, 20)),
                        new EntityFilterOp(AsgQueryUtil.element$(query, 21)
                        ))
        );



        OptionalSplitPlanSearcher planSearcher = new OptionalSplitPlanSearcher(mainPlanSearcherMock, optionalPlanSearcherMock);
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = planSearcher.search(query);
        PlanAssert.assertEquals(expectedCompletePlan, planWithCost.getPlan());
    }

}
