package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.epb.plan.estimation.CostEstimator;
import com.kayhut.fuse.epb.plan.estimation.dummy.DummyCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.epb.plan.extenders.M1.M1NonRedundantPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.AsgEBaseContainer;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Value;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.log.Trace.asString;
import static com.kayhut.fuse.model.query.ConstraintOp.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by moti on 2/23/2017.
 */
public class CompoundBottomUpPlanSearcherTests {

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        long time = System.currentTimeMillis();
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type)
                        .next(eProp(2,EProp.of(HEIGHT.type, 3, Constraint.of(ConstraintOp.gt, 189)))))
                .next(rel(4, OWN.getrType(), R)
                        .below(relProp(5, of(START_DATE.type, 6, Constraint.of(eq, new Date())))))
                .next(typed(7, DRAGON.type))
                .next(quant1(8, all))
                .in(eProp(9, EProp.of(NAME.type, 10, Constraint.of(eq, "smith")), EProp.of(GENDER.type, 11, Constraint.of(gt, new Value(MALE.ordinal(),MALE.name()))))
                        , rel(12, FREEZE.getrType(), R)
                                .below(relProp(122))
                                .next(unTyped(13)
                                        .next(eProp(14,EProp.of(NAME.type, 15, Constraint.of(ConstraintOp.notContains, "bob"))))
                                )
                        , rel(16, FIRE.getrType(), R)
                                .below(relProp(18, of(START_DATE.type, 19,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(END_DATE.type, 19, Constraint.of(le, new Date(time + 1000 * 60)))))
                                .next(concrete(20, "smoge", DRAGON.type, "Display:smoge", "D")
                                        .next(eProp(21,EProp.of(NAME.type, 22, Constraint.of(ConstraintOp.eq, "smoge"))))
                                )
                )
                .build();
    }

    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B")).build();
    }

    @Test
    public void TestBuilderSimpleQ2Path(){
        AsgQuery query = simpleQuery2("name", "o1");

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);

        List<PlanWithCost<Plan, PlanDetailedCost>> planList = new LinkedList<>();
        plans.forEach(planList::add);

        Assert.assertEquals(12, planList.size());
//        PlanAssert.assertEquals(mock(query).entity(1).rel(2).relFilter(10).entity(3).entityFilter(9).rel(5).entity(6).goTo(3).rel(7).relFilter(11).entity(8).plan(), planList.get(1).getPlan());
//        PlanAssert.assertEquals(mock(query).entity(1).rel(2).relFilter(10).entity(3).entityFilter(9).rel(7).relFilter(11).entity(8).goTo(3).rel(5).entity(6).plan(), planList.get(0).getPlan());
    }

    @Test
    public void TestBuilderSimpleQ1Path(){
        AsgQuery query = simpleQuery1("name", "o1");

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);

        List<PlanWithCost<Plan, PlanDetailedCost>> planList = new LinkedList<>();
        plans.forEach(planList::add);

        Assert.assertEquals(2, planList.size());
        Assert.assertEquals(3, planList.get(0).getPlan().getOps().size());
        Assert.assertEquals(3, planList.get(1).getPlan().getOps().size());
    }

    @Test
    public void TestBuilderSimplePath(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query.getLeft());

        List<PlanWithCost<Plan, PlanDetailedCost>> planList = new LinkedList<>();
        plans.forEach(planList::add);

        Assert.assertEquals(2, planList.size());
        Assert.assertEquals(3, planList.get(0).getPlan().getOps().size());
        Assert.assertEquals(3, planList.get(1).getPlan().getOps().size());
    }

    @Test
    public void TestBuilderSingleEntity(){

        Pair<AsgQuery, AsgEBase> query = BuilderTestUtil.createSingleEntityQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query.getLeft());

        List<PlanWithCost<Plan, PlanDetailedCost>> planList = new LinkedList<>();
        plans.forEach(planList::add);

        Assert.assertEquals(1, planList.size());
        Assert.assertEquals(1, planList.get(0).getPlan().getOps().size());
    }

    @Test
    public void TestBuilderAllPaths(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> planWrappers = planSearcher.search(query.getLeft());

        List<PlanWithCost<Plan, PlanDetailedCost>> planList = new LinkedList<>();
        planWrappers.forEach(planList::add);

        Assert.assertEquals(2, planList.size());
        Assert.assertEquals(3, planList.get(0).getPlan().getOps().size());
        Assert.assertEquals(3, planList.get(1).getPlan().getOps().size());

        AsgEBase firstElement = query.getLeft().getStart().getNext().get(0);
        AsgEBase secondElement = (AsgEBase) firstElement.getNext().get(0);
        AsgEBase thirdElement = (AsgEBase) secondElement.getNext().get(0);
        boolean foundFirstPlan = false;
        for(PlanWithCost<Plan, PlanDetailedCost> planWithCost : planList){
            List<PlanOp> ops = planWithCost.getPlan().getOps();

            if(firstElement.geteNum() == ((AsgEBaseContainer)ops.get(0)).getAsgEbase().geteNum() &&
                    secondElement.geteNum() == ((AsgEBaseContainer)ops.get(1)).getAsgEbase().geteNum() &&
                    thirdElement.geteNum() == ((AsgEBaseContainer)ops.get(2)).getAsgEbase().geteNum()) {
                foundFirstPlan = true;
            }
        }

        System.out.println(asString(planSearcher,Level.INFO,new StringJoiner("\n","[","]")));
        Assert.assertTrue(foundFirstPlan);
    }

    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> createBottomUpPlanSearcher() {

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();

        CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator =
                new DummyCostEstimator<>(new PlanDetailedCost());

        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> planSelector = new AllCompletePlanSelector<>();

        return new BottomUpPlanSearcher<>(
                new M1NonRedundantPlanExtensionStrategy(),
                pruneStrategy,
                pruneStrategy,
                planSelector,
                planSelector,
                validator,
                costEstimator);
    }


}
