package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import org.junit.Assert;
import org.junit.Test;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.rel;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

/**
 * Created by benishue on 7/5/2017.
 */
public class JoinIntersectionPlanOpValidatorTest {



    /**
     * In this case we have only JoinOp inside the plan.
     * This should be a valid one since we have one intersection
     * of EntityOp = Kingdom (id: 5)
     */
    @Test
    public void joinOpIntersectionValidationTest1() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");

        Plan plan = new Plan(
                createJoinOp1(asgQuery)
        );

        Assert.assertTrue(new JoinIntersectionPlanOpValidator().isPlanValid(plan, asgQuery).valid());
    }


    /**
     * 0 Intersection, the branches of the JoinOp don't have common EntityOp
     */
    @Test
    public void joinOpIntersectionValidationTest2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");

        JoinOp joinOp = createJoinOp1(asgQuery);
        //Replacing the last EntityOp (kingdom) with some other EntityOp
        Plan leftBranchPlan = joinOp.getLeftBranch();
        EntityOp kingdomEop = PlanUtil.<EntityOp>first$(leftBranchPlan, op -> op.geteNum() == 5);
        EntityOp horseEop = new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 9).get());

        JoinOp inCompleteJoinOp = new JoinOp(PlanUtil.replace(leftBranchPlan, kingdomEop, horseEop),
                joinOp.getRightBranch());

        Plan plan = new Plan(
                inCompleteJoinOp
        );

        Assert.assertTrue(new JoinIntersectionPlanOpValidator().isPlanValid(plan, asgQuery).valid());
    }


    /**
     * This is valid since we have more than one Op in the plan beside the JoinOp
     */
    @Test
    public void joinOpIntersectionValidationTest3() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");

        Plan plan = new Plan(
                createJoinOp1(asgQuery),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 7).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 8).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 9).get())
        );

        Assert.assertTrue(new JoinIntersectionPlanOpValidator().isPlanValid(plan, asgQuery).valid());
    }


    /**
     * This should be false since we have Intersection of 2 Entity Ops - this is inefficient
     */
    @Test
    public void joinOpIntersectionValidationTest4() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");

        JoinOp joinOp = createJoinOp1(asgQuery);
        EntityOp personEop = PlanUtil.<EntityOp>first$(joinOp.getRightBranch(), op -> op.geteNum() == 7);
        EntityOp dragonEop = new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get());

        JoinOp duplicatedJoinOp = new JoinOp(joinOp.getLeftBranch(),
                PlanUtil.replace(joinOp.getRightBranch(), personEop, dragonEop));

        Plan plan = new Plan(
                duplicatedJoinOp
        );

        Assert.assertFalse(new JoinIntersectionPlanOpValidator().isPlanValid(plan, asgQuery).valid());
    }


    /**
     * In this case we have in the left branch of the JoinOp a nested JoinOp.
     * When we drill down in both of the JoinOp direction to get the EntityOps,
     * the intersection of them should be the same enum, in this test, the right side of the
     *  higher level joinOp contains an enum which doesn't intersect with the EntityOp enum
     *  used for the Join operation in the left side.
     */
    @Test
    public void joinOpIntersectionValidationTest5() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");

        Plan plan = new Plan(new JoinOp(new Plan(createJoinOp1(asgQuery)),
                new Plan(new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 7).get()))));

        Assert.assertFalse(new JoinIntersectionPlanOpValidator().isPlanValid(plan, asgQuery).valid());
    }

    /**
     * The plan is not valid since the intersection is of size 2
     */
    @Test
    public void joinOpIntersectionValidationTest6() {
        AsgQuery asgQuery1 = simpleQuery1("name", "ont");
        AsgQuery asgQuery2 = simpleQuery2("name", "ont");

        Plan plan = new Plan(new JoinOp(new Plan(createJoinOp1(asgQuery1)),
                                        new Plan(createJoinOp2(asgQuery1, asgQuery2))));

        Assert.assertFalse(new JoinIntersectionPlanOpValidator().isPlanValid(plan, null).valid());
    }


    private static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.DRAGON.type, "D1"))
                .next(rel(2, FIRE.getrType(), R))
                .next(typed(3, OntologyTestUtils.DRAGON.type, "D2"))
                .next(rel(4, ORIGIN.getrType(), R))
                .next(typed(5, OntologyTestUtils.KINGDOM.type, "K"))
                .next(rel(6, SUBJECT.getrType(), L))
                .next(typed(7, OntologyTestUtils.PERSON.type, "P"))
                .next(rel(8, OWN.getrType(), R))
                .next(typed(9, HORSE.type, "H"))
                .build();
    }

    private static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(10, OntologyTestUtils.DRAGON.type, "P1"))
                .next(rel(11, SUBJECT.getrType(), R))
                .next(typed(5, OntologyTestUtils.KINGDOM.type, "K"))
                .build();
    }

    private static JoinOp createJoinOp1(AsgQuery asgQuery) {
        Plan leftPlan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 4).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 5).get())
        );

        Plan rightPlan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 7).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 6).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 5).get())
        );

        return new JoinOp(leftPlan, rightPlan);
    }


    private static JoinOp createJoinOp2(AsgQuery asgQuery1, AsgQuery asgQuery2) {
        Plan leftPlan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery2, 10).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery2, 11).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery2, 5).get())
        );

        Plan rightPlan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery1, 7).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery1, 6).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery1, 5).get())
        );

        return new JoinOp(leftPlan, rightPlan);
    }

}