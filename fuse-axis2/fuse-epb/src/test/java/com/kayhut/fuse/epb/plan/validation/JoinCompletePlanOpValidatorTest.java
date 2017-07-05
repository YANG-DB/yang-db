package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.rel;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

/**
 * Created by benishue on 7/4/2017.
 */
public class JoinCompletePlanOpValidatorTest {


    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, DRAGON.type, "D1"))
                .next(rel(2, FIRE.getrType(), R))
                .next(typed(3, DRAGON.type, "D2"))
                .next(rel(4, ORIGIN.getrType(), R))
                .next(typed(5, KINGDOM.type, "K"))
                .next(rel(6, SUBJECT.getrType(), L))
                .next(typed(7, PERSON.type, "P"))
                .next(rel(8, OWN.getrType(), R))
                .next(typed(9, HORSE.type, "H"))
                .build();
    }

    /**
     * In this case we have only JoinOp inside the plan.
     * This should be a valid one since we are looking for a plan
     * where we have at least 2 Ops and the first one of them is a Join Op
     */
    @Test
    public void joinOpValidationTest1() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");

        Plan plan = new Plan(
                createJoinOp(asgQuery)
        );

        Assert.assertTrue(new JoinCompletePlanOpValidator().isPlanValid(plan, asgQuery).valid());
    }

    /**
     * This case is valid since we have a plan with more than 2 Ops.
     * The first Op is a JoinOp and it's "Complete" - on the left branch
     * of the JoinOp we are looking for the last EntityOp(EOP) or EOP + attached EntityFilterOp (EFO)
     * We should check that we have this EOP (or EOP + EFO) at the right branch of the JoinOp
     * i.e., The enums should be the same
     */
    @Test
    public void joinOpValidationTest2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");

        Plan plan = new Plan(
                createJoinOp(asgQuery),
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 7).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 8).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 9).get())
        );

        Assert.assertTrue(new JoinCompletePlanOpValidator().isPlanValid(plan, asgQuery).valid());
    }


    @Test
    public void joinOpValidationTest3() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");

        JoinOp joinOp = createJoinOp(asgQuery);
        //Replacing the last EntityOp (kingdom) with some other EntityOp
        Plan leftBranchPlan = joinOp.getLeftBranch();
        EntityOp kingdomEop = PlanUtil.<EntityOp>first$(leftBranchPlan, op -> op.geteNum() == 5);
        EntityOp horseEop = new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 9).get());

        JoinOp inValidJoinOp = new JoinOp(PlanUtil.replace(leftBranchPlan, kingdomEop, horseEop),
                joinOp.getRightBranch());

        Plan plan = new Plan(
                inValidJoinOp,
                new GoToEntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 7).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 8).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 9).get())
        );

        Assert.assertFalse(new JoinCompletePlanOpValidator().isPlanValid(plan, asgQuery).valid());
    }

    private static JoinOp createJoinOp(AsgQuery asgQuery) {
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

}