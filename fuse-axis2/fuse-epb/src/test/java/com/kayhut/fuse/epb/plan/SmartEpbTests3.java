package com.kayhut.fuse.epb.plan;

import com.google.common.collect.Iterables;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Value;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.Iterator;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.ConstraintOp.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by moti on 20/05/2017.
 */
@Ignore
public class SmartEpbTests3 extends SmartEpbShortPathTests {

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        long time = System.currentTimeMillis();
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type)
                        .next(eProp(2,EProp.of(Integer.toString(HEIGHT.type), 3, Constraint.of(ConstraintOp.gt, 189)))))
                .next(rel(4, OWN.getrType(), R)
                        .below(relProp(5, of(START_DATE.type, 6, Constraint.of(eq, new Date())))))
                .next(typed(7, DRAGON.type))
                .next(quant1(8, all))
                .in(eProp(9, EProp.of(NAME.type, 10, Constraint.of(eq, "smith")), EProp.of(GENDER.type, 11, Constraint.of(gt, new Value(MALE.ordinal(),MALE.name()))))
                        , rel(12, FREEZE.getrType(), R)
                                .below(relProp(122))
                                .next(unTyped(13)
                                    .next(eProp(14,EProp.of(Integer.toString(NAME.type), 15, Constraint.of(ConstraintOp.notContains, "bob"))))
                                )
                        , rel(16, FIRE.getrType(), R)
                                .below(relProp(18, of(START_DATE.type, 19,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(END_DATE.type, 19, Constraint.of(le, new Date(time + 1000 * 60)))))
                                .next(concrete(20, "smoge", DRAGON.type, "Display:smoge", "D")
                                    .next(eProp(21,EProp.of(Integer.toString(NAME.type), 22, Constraint.of(ConstraintOp.eq, "smoge"))))
                                )
                )
                .build();
    }

    @Test
    public void testPathSelectionNoConditionsReversePlan(){
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(simpleQuery2("q1","Dragons"));
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        Assert.assertEquals(first.getCost().getGlobalCost().cost,1003, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(400,op.getCost().cost, 0.1);
        Assert.assertTrue(op.getOpBase().get(0) instanceof EntityOp);
        Assert.assertEquals(PERSON.type,((ETyped)((EntityOp)op.getOpBase().get(0)).getAsgEBase().geteBase()).geteType());
        Assert.assertEquals(303,iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(300, iterator.next().getCost().cost, 0.1);

    }

    @Test
    public void testPathSelectionNoConditions(){
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(simpleQuery2("q1","Dragons"));
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        Assert.assertEquals(first.getCost().getGlobalCost().cost,1003, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(400, op.getCost().cost, 0.1);
        Assert.assertEquals(303, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(300, iterator.next().getCost().cost, 0.1);
    }

    @Test
    public void testPathSelectionFilterToSide(){
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(simpleQuery2("q1","Dragons"));
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        Assert.assertEquals(30.3, first.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(10.09,op.getCost().cost, 0.1);
        Assert.assertTrue(op.getOpBase().get(0) instanceof EntityOp);
        Assert.assertEquals(DRAGON.type,((ETyped)((EntityOp)op.getOpBase().get(0)).getAsgEBase().geteBase()).geteType());
        Assert.assertEquals(10.19, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(10.09, iterator.next().getCost().cost, 0.1);
    }

    @Test
    public void testPathSelectionFilterFromSide(){
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(simpleQuery2("q1","Dragons"));
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        Assert.assertEquals(30.7, first.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(133d/13d,op.getCost().cost, 0.1);
        Assert.assertTrue(op.getOpBase().get(0) instanceof EntityOp);
        Assert.assertEquals(PERSON.type,((ETyped)((EntityOp)op.getOpBase().get(0)).getAsgEBase().geteBase()).geteType());
        Assert.assertEquals(10.3, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(133d/13d, iterator.next().getCost().cost, 0.1);
    }

    @Test
    public void testPathSelectionFilterOnRel(){
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(simpleQuery2("q1","Dragons"));
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        Assert.assertEquals(601, first.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(400,op.getCost().cost, 0.1);
        Assert.assertTrue(op.getOpBase().get(0) instanceof EntityOp);
        Assert.assertEquals(PERSON.type,((ETyped)((EntityOp)op.getOpBase().get(0)).getAsgEBase().geteBase()).geteType());
        Assert.assertEquals(101, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(100, iterator.next().getCost().cost, 0.1);
    }


    @Test
    public void testFilterOnAllItems(){
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(simpleQuery2("q1","Dragons"));
        PlanWithCost<Plan, PlanDetailedCost> first = Iterables.getFirst(plans, null);
        Assert.assertNotNull(first);
        Assert.assertEquals(467, first.getCost().getGlobalCost().cost, 0.1);
        Iterator<PlanOpWithCost<Cost>> iterator = first.getCost().getOpCosts().iterator();
        PlanOpWithCost<Cost> op = iterator.next();
        Assert.assertEquals(266,op.getCost().cost, 0.1);
        Assert.assertTrue(op.getOpBase().get(0) instanceof EntityOp);
        Assert.assertEquals(PERSON.type,((ETyped)((EntityOp)op.getOpBase().get(0)).getAsgEBase().geteBase()).geteType());
        Assert.assertEquals(101, iterator.next().getCost().cost, 0.1);
        Assert.assertEquals(100, iterator.next().getCost().cost, 0.1);
    }


}
