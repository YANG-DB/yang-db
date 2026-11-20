package com.yangdb.fuse.epb.plan.validation.opValidator;

import com.yangdb.fuse.dispatcher.epb.PlanValidator;
import com.yangdb.fuse.epb.plan.validation.M1PlanValidator;
import com.yangdb.fuse.epb.plan.validation.M2PlanValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.properties.FunctionRelProp;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.CountConstraintOp;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.yangdb.fuse.model.OntologyTestUtils.KNOW;
import static com.yangdb.fuse.model.OntologyTestUtils.PERSON;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.query.Rel.Direction.R;

/**
 * Q7: {@code (p1)-[k]->(p2) WHERE count(k) > 15} groups the count by the edge document's source entity, i.e. by the
 * entity the traversal comes from. When the planner reversed the relation (starting from p2) the filter counted p2's
 * incoming edges instead, and the tutorial query returned nothing. A count-filtered relation must therefore be
 * traversed in its query direction.
 */
public class AggregatedRelationDirectionOpValidatorTests {

    private static AsgQuery countQuery() {
        return AsgQuery.Builder.start("q", "Dragons")
                .next(typed(1, PERSON.type, "p1"))
                .next(rel(2, KNOW.getrType(), R)
                        .below(relPropGroup(200,
                                new FunctionRelProp(201, "count", Constraint.of(CountConstraintOp.gt, "15")))))
                .next(typed(3, PERSON.type, "p2"))
                .build();
    }

    private static AsgQuery plainQuery() {
        return AsgQuery.Builder.start("q", "Dragons")
                .next(typed(1, PERSON.type, "p1"))
                .next(rel(2, KNOW.getrType(), R).below(relPropGroup(200)))
                .next(typed(3, PERSON.type, "p2"))
                .build();
    }

    private static Plan forward(AsgQuery query) {
        return new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(query, 200).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get()));
    }

    private static Plan reversed(AsgQuery query) {
        return new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get()),
                new RelationOp(AsgQueryUtil.reverse(AsgQueryUtil.<Rel>element(query, 2).get())),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(query, 200).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get()));
    }

    @Test
    public void countFilteredRelationIsOnlyValidInQueryDirection() {
        for (PlanValidator<Plan, AsgQuery> validator : Arrays.<PlanValidator<Plan, AsgQuery>>asList(new M1PlanValidator(), new M2PlanValidator())) {
            String name = validator.getClass().getSimpleName();
            AsgQuery query = countQuery();
            Assert.assertTrue(name + " forward", validator.isPlanValid(forward(query), query).valid());
            Assert.assertFalse(name + " reversed", validator.isPlanValid(reversed(query), query).valid());
        }
    }

    @Test
    public void relationWithoutAggregationMayStillBeReversed() {
        for (PlanValidator<Plan, AsgQuery> validator : Arrays.<PlanValidator<Plan, AsgQuery>>asList(new M1PlanValidator(), new M2PlanValidator())) {
            String name = validator.getClass().getSimpleName();
            AsgQuery query = plainQuery();
            Assert.assertTrue(name + " forward", validator.isPlanValid(forward(query), query).valid());
            Assert.assertTrue(name + " reversed", validator.isPlanValid(reversed(query), query).valid());
        }
    }
}
