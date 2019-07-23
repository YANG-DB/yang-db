package com.yangdb.fuse.epb.plan.validation.opValidator;

import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityJoinOp;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.validation.ValidationResult;
import org.junit.Assert;
import org.junit.Test;

import static com.yangdb.fuse.dispatcher.utils.PlanUtil.*;
import static com.yangdb.fuse.model.OntologyTestUtils.OWN;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;

public class JoinBranchSameStartAndEndValidatorTests {

    public AsgQuery simpleQuery(){
        return AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, OntologyTestUtils.PERSON.type)).
                next(ePropGroup(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, OntologyTestUtils.DRAGON.type)).
                next(ePropGroup(6)).
                next(rel(7, OWN.getrType(), Rel.Direction.R).below(relProp(8))).
                next(typed(9, OntologyTestUtils.DRAGON.type)).
                next(ePropGroup(10)).
                build();
    }

    @Test
    public void validJoinTest(){
        AsgQuery query = simpleQuery();
        Plan plan = new Plan(new EntityJoinOp(
                new Plan(entityOp(query, 1), filterOp(query, 2), relOp(query,3),relFilterOp(query,4), entityOp(query,5),filterOp(query,6)),
                new Plan(entityOp(query, 9), filterOp(query, 10), relOp(query,7, Rel.Direction.L),relFilterOp(query,8), entityOp(query,5),filterOp(query,6))));
        JoinBranchSameStartAndEndValidator validator = new JoinBranchSameStartAndEndValidator();
        ValidationResult result = validator.isPlanOpValid(query, plan, 0);
        Assert.assertTrue(result.valid());
    }

    @Test
    public void invalidJoinSingleEntityLeftTest(){
        AsgQuery query = simpleQuery();
        Plan plan = new Plan(new EntityJoinOp(
                new Plan(entityOp(query, 1), filterOp(query, 2)),
                new Plan(entityOp(query, 9), filterOp(query, 10), relOp(query,7, Rel.Direction.L),relFilterOp(query,8), entityOp(query,5),filterOp(query,6))));
        JoinBranchSameStartAndEndValidator validator = new JoinBranchSameStartAndEndValidator();
        ValidationResult result = validator.isPlanOpValid(query, plan, 0);
        Assert.assertFalse(result.valid());
    }

    @Test
    public void invalidJoinSingleEntityRightTest(){
        AsgQuery query = simpleQuery();
        Plan plan = new Plan(new EntityJoinOp(
                new Plan(entityOp(query, 5), filterOp(query, 6), relOp(query,3, Rel.Direction.L),relFilterOp(query,4), entityOp(query,1),filterOp(query,2)),
                new Plan(entityOp(query, 1), filterOp(query, 2))));
        JoinBranchSameStartAndEndValidator validator = new JoinBranchSameStartAndEndValidator();
        ValidationResult result = validator.isPlanOpValid(query, plan, 0);
        Assert.assertFalse(result.valid());
    }

    @Test
    public void invalidJoinGotoTest(){
        AsgQuery query = simpleQuery();
        Plan plan = new Plan(new EntityJoinOp(
                new Plan(entityOp(query, 5), filterOp(query, 6), relOp(query,3, Rel.Direction.L),relFilterOp(query,4), entityOp(query,1),filterOp(query,2), gotoOp(query, 5)),
                new Plan(entityOp(query, 9), filterOp(query, 10), relOp(query,7, Rel.Direction.L),relFilterOp(query,8), entityOp(query,5),filterOp(query,6))));
        JoinBranchSameStartAndEndValidator validator = new JoinBranchSameStartAndEndValidator();
        ValidationResult result = validator.isPlanOpValid(query, plan, 0);
        Assert.assertFalse(result.valid());
    }
}
