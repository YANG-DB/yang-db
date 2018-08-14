package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanAssert;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.ScoreEProp;
import com.kayhut.fuse.model.query.properties.ScoreEPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import javaslang.collection.Stream;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.*;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.junit.Assert.assertEquals;

/**
 * Created by Roman on 23/04/2017.
 */
public class InitialPlanBoostExtenderStrategyTest {


    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type,"A"))
                .next(new AsgEBase<>(new ScoreEPropGroup(2,100, EProp.of(3, "pTyple", Constraint.of(eq, "1234")))))
                .next(rel(4,OWN.getrType(),R))
                .next(typed(5, DRAGON.type,"B"))
                .next(new AsgEBase<>(new ScoreEPropGroup(6,100, EProp.of(7, "pTyple", Constraint.of(eq, "1234"))))).build();
    }


    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type,"A"))
                .next(ePropGroup(2,new ScoreEProp(3, "pTyple", Constraint.of(eq, "1234"), 100)))
                .next(rel(4,OWN.getrType(),R))
                .next(typed(5, DRAGON.type,"B")).build();

    }

    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 2)));

        List<Plan> extendedPlans = Stream.ofAll(new InitialPlanBoostExtensionStrategy().extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(),1);
        Plan actualPlan1 = extendedPlans.get(0);
        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 2)));

        List<Plan> extendedPlans = Stream.ofAll(new InitialPlanBoostExtensionStrategy().extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(),1);
        Plan actualPlan1 = extendedPlans.get(0);
        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
    }

}
