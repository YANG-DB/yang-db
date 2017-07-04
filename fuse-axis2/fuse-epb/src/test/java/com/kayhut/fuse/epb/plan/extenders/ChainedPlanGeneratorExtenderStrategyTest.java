package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.extenders.ChainPlanExtensionStrategy;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.properties.EProp;
import javaslang.collection.Stream;
import org.junit.Test;

import java.util.*;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.ConstraintOp.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.junit.Assert.assertEquals;

public class ChainedPlanGeneratorExtenderStrategyTest {
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

    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        Plan startPlan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 0)));


        ChainPlanExtensionStrategy chain = new ChainPlanExtensionStrategy<Plan, AsgQuery>(
                (plan, query) -> Arrays.asList(Plan.compose(plan, new EntityOp(AsgQueryUtil.element$(asgQuery, 1)))
                                             , Plan.compose(plan, new EntityOp(AsgQueryUtil.element$(asgQuery, 3)))),
                (plan, query) -> Collections.singletonList(Plan.compose(plan, new RelationOp(AsgQueryUtil.element$(asgQuery, 5)))),
                (plan, query) -> Arrays.asList(Plan.compose(plan, new EntityOp(AsgQueryUtil.element$(asgQuery, 1)))
                                             , Plan.compose(plan, new EntityOp(AsgQueryUtil.element$(asgQuery, 3)))
                                             ,Plan.compose(plan, new RelationOp(AsgQueryUtil.element$(asgQuery, 5)))));


        List<Plan> extendedPlans = Stream.ofAll(chain.extendPlan(startPlan, asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 6);
        extendedPlans.forEach(p->assertEquals(p.getOps().size(),4));
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        Plan startPlan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 0)));


        ChainPlanExtensionStrategy chain =  new ChainPlanExtensionStrategy<Plan, AsgQuery>(
                (plan, query) -> Arrays.asList(Plan.compose(plan, new EntityOp(AsgQueryUtil.element$(asgQuery, 1)))
                                             , Plan.compose(plan, new RelationOp(AsgQueryUtil.element$(asgQuery, 3)))),
                (plan, query) -> Arrays.asList(Plan.compose(plan, new EntityOp(AsgQueryUtil.element$(asgQuery, 7)))
                                            , Plan.compose(plan, new RelationOp(AsgQueryUtil.element$(asgQuery, 9)))),
                (plan, query) -> Arrays.asList(Plan.compose(plan, new EntityOp(AsgQueryUtil.element$(asgQuery, 1)))
                                             , Plan.compose(plan, new EntityOp(AsgQueryUtil.element$(asgQuery, 3)))
                                             ,Plan.compose(plan, new RelationOp(AsgQueryUtil.element$(asgQuery, 5)))));


        List<Plan> extendedPlans = Stream.ofAll(chain.extendPlan(startPlan, asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 12);
        extendedPlans.forEach(p->assertEquals(p.getOps().size(),4));
    }
}
