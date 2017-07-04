package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.epb.plan.validation.opValidator.CompositePlanOpValidator;
import com.kayhut.fuse.epb.plan.validation.opValidator.NoRedundantRelationOpValidator;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.JoinOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.rel;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.kayhut.fuse.model.query.Rel.Direction.*;
import static org.junit.Assert.*;

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

    @Test
    public void joinOpValidationTest1() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        List<AsgEBase<? extends EBase>> leftBranch = AsgQueryUtil.<EEntityBase, EEntityBase>path(asgQuery, 1, 5);
        List<AsgEBase<? extends EBase>> rightBranch = AsgQueryUtil.<EEntityBase, EEntityBase>path(asgQuery, 5, 9);

        Plan leftPlan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 4).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 5).get())
                );

        Plan rightPlan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 8).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 7).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 4).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 5).get())
                );

        Plan plan = new Plan(
                new JoinOp(leftPlan, rightPlan)
        );

        Assert.assertTrue(new JoinCompletePlanOpValidator().isPlanValid(plan, asgQuery).valid());
    }

}