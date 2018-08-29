package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.epb.plan.estimation.pattern.PredicateCostEstimator;
import com.kayhut.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.M1.M1DfsRedundantPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.pruners.CheapestPlanPruneStrategy;
import com.kayhut.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.kayhut.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Value;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Optional;

import static com.kayhut.fuse.epb.utils.DfsTestUtils.buildSchemaProvider;
import static com.kayhut.fuse.epb.utils.DfsTestUtils.ruleBaseEstimator;
import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.quant.QuantType.some;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnionPlanSearcherTests {
    //region Fields
    private OntologyProvider ontologyProvider;
    //ontology
    private Ontology.Accessor ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion

    @Before
    public void setup() {
        this.ontologyProvider = mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenReturn(Optional.of(OntologyTestUtils.createDragonsOntologyShort()));

        this.schemaProviderFactory = ontology -> buildSchemaProvider(new Ontology.Accessor(ontology));
    }


    private AsgQuery queryComplexSingleSomeQuant() {
        return AsgQuery.Builder.start("q", "O")
                .next(typed(1, PERSON.type)
                        .next(eProp(2, EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189)))))
                .next(rel(4, OWN.getrType(), R)
                        .below(relProp(5, of(6, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(7, DRAGON.type))
                .next(quant1(8, some))
                .in(eProp(9, EProp.of(10, NAME.type, Constraint.of(eq, "smith")), EProp.of(11, GENDER.type, Constraint.of(gt, new Value(MALE.ordinal(), MALE.name()))))
                        , optional(50).next(rel(12, FREEZE.getrType(), R)
                                .next(unTyped(13)
                                        .next(eProp(14, EProp.of(15, NAME.type, Constraint.of(ConstraintOp.notContains, "bob"))))
                                ))
                        , optional(60).next(rel(16, FIRE.getrType(), R)
                                .next(concrete(20, "smoge", DRAGON.type, "Display:smoge", "D")
                                        .next(eProp(21, EProp.of(22, NAME.type, Constraint.of(ConstraintOp.eq, "smoge"))))
                                ))
                )
                .build();
    }

    private AsgQuery querySimpleSingleSomeQuantSingleBranch() {
        return AsgQuery.Builder.start("q", "O")
                .next(typed(1, PERSON.type))
                .next(eProp(2, EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                .next(quant1(50, some))
                .in(rel(12, FREEZE.getrType(), R)
                        .next(unTyped(13)
                                .next(eProp(14, EProp.of(15, NAME.type, Constraint.of(ConstraintOp.notContains, "bob"))))
                        )).build();
    }

    private AsgQuery querySimpleSingleSomeQuantMultiBranch() {
        return start("q", "O")
                .next(typed(1, PERSON.type))
                .next(quant1(2, some)).
                        in(rel(13, FREEZE.getrType(), R).next(typed(3, PERSON.type)),
                           rel(14, FREEZE.getrType(), R).next(typed(4, PERSON.type)),
                           rel(15, FREEZE.getrType(), R).next(typed(5, PERSON.type))
                        ).build();
    }

    private AsgQuery queryNoQuant() {
        return AsgQuery.Builder.start("q", "O")
                .next(typed(1, PERSON.type)
                        .next(eProp(2, EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189)))))
                .build();
    }

    @Test
    public void testNoQuant() {
        final BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> searcher = createBottomUpPlanSearcher();
        PlanWithCost<Plan, Cost> expectedPlan = new PlanWithCost(
                new Plan(
                        new Plan(
                                new EntityOp(new AsgEBase<>(new ETyped(1, "", PERSON.type, 2, 0))),
                                new EntityFilterOp(eProp(2, EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                        ).getOps()),
                new DoubleCost(0));

        UnionPlanSearcher planSearcher = new UnionPlanSearcher(searcher);
        AsgQuery query = queryNoQuant();
        final PlanWithCost<Plan, PlanDetailedCost> planWithCost = planSearcher.search(query);

        Assert.assertNotNull(planWithCost);
        Assert.assertEquals(expectedPlan.getPlan(), planWithCost.getPlan());
    }

    @Test
    public void testOneQuantSingleBranch() {
        final BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> searcher = createBottomUpPlanSearcher();
        PlanWithCost<Plan, Cost> expectedPlan = new PlanWithCost(
                new Plan(
                        new Plan(
                                new EntityOp(new AsgEBase<>(new ETyped(1, "", PERSON.type, 2, 0))),
                                new EntityFilterOp(eProp(2, EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189)))),
                                new RelationOp(new AsgEBase<>(new Rel(12, FREEZE.getrType(), R, "", 1, 0)), R),
                                new EntityOp(new AsgEBase<>(new EUntyped(13, "", 14, 0))),
                                new EntityFilterOp(eProp(14, EProp.of(14, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                        ).getOps()),
                new DoubleCost(0));

        UnionPlanSearcher planSearcher = new UnionPlanSearcher(searcher);
        AsgQuery query = querySimpleSingleSomeQuantSingleBranch();
        final PlanWithCost<Plan, PlanDetailedCost> planWithCost = planSearcher.search(query);

        Assert.assertNotNull(planWithCost);
        Assert.assertEquals(expectedPlan.getPlan(), planWithCost.getPlan());
    }

    @Test
    public void testOneQuantMultiBranch() {
        final BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> searcher = createBottomUpPlanSearcher();
        PlanWithCost<Plan, Cost> expectedPlan = new PlanWithCost(
                new Plan(
                        new Plan(
                                new EntityOp(new AsgEBase<>(new ETyped(1, "", PERSON.type, 2, 0))),
                                new EntityFilterOp(eProp(2, EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189)))),
                                new RelationOp(new AsgEBase<>(new Rel(12, FREEZE.getrType(), R, "", 1, 0)), R),
                                new EntityOp(new AsgEBase<>(new EUntyped(13, "", 14, 0))),
                                new EntityFilterOp(eProp(14, EProp.of(14, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                        ).getOps()),
                new DoubleCost(0));

        UnionPlanSearcher planSearcher = new UnionPlanSearcher(searcher);
        AsgQuery query = querySimpleSingleSomeQuantMultiBranch();
        final PlanWithCost<Plan, PlanDetailedCost> planWithCost = planSearcher.search(query);

        Assert.assertNotNull(planWithCost);
        Assert.assertEquals(expectedPlan.getPlan(), planWithCost.getPlan());
    }


    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> createBottomUpPlanSearcher() {

        return new BottomUpPlanSearcher<>(
                new M1DfsRedundantPlanExtensionStrategy(
                        ontologyProvider,
                        schemaProviderFactory),
                new NoPruningPruneStrategy<>(),
                new CheapestPlanPruneStrategy(),
                new AllCompletePlanSelector<>(),
                new AllCompletePlanSelector<>(),
                new M1PlanValidator(),
                new PredicateCostEstimator<>(plan -> plan.getOps().size() <= 2,
                        new RegexPatternCostEstimator(ruleBaseEstimator(ont)),
                        (plan, context) -> new PlanWithCost<>(plan, context.getPreviousCost().get().getCost())));
    }


}
