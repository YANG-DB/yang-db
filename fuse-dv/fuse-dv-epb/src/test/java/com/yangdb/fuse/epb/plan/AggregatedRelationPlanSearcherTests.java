package com.yangdb.fuse.epb.plan;

import com.yangdb.fuse.dispatcher.epb.PlanValidator;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.epb.plan.estimation.pattern.PredicateCostEstimator;
import com.yangdb.fuse.epb.plan.estimation.pattern.RegexPatternCostEstimator;
import com.yangdb.fuse.epb.plan.extenders.M1.M1DfsRedundantPlanExtensionStrategy;
import com.yangdb.fuse.epb.plan.pruners.CheapestPlanPruneStrategy;
import com.yangdb.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.yangdb.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.yangdb.fuse.epb.plan.validation.M1PlanValidator;
import com.yangdb.fuse.epb.plan.validation.M2PlanValidator;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanOp;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.FunctionRelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.constraint.CountConstraintOp;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import static com.yangdb.fuse.epb.utils.DfsTestUtils.buildSchemaProvider;
import static com.yangdb.fuse.epb.utils.DfsTestUtils.ruleBaseEstimator;
import static com.yangdb.fuse.model.OntologyTestUtils.FIRE;
import static com.yangdb.fuse.model.OntologyTestUtils.KNOW;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Q7 regression guard at planner level, with the planner the service runs (redundant DFS extension + M1/M2 plan
 * validators): a count-filtered relation is planned in its query direction, while a relation carrying only a
 * non-count function prop ({@code return distinct(f)}, Q9) must still be plannable.
 */
public class AggregatedRelationPlanSearcherTests {
    private final Ontology.Accessor ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;

    @Before
    public void setup() {
        ontologyProvider = mock(OntologyProvider.class);
        when(ontologyProvider.get(any())).thenReturn(Optional.of(OntologyTestUtils.createDragonsOntologyShort()));
        schemaProviderFactory = ontology -> buildSchemaProvider(new Ontology.Accessor(ontology));
    }

    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher(PlanValidator<Plan, AsgQuery> validator) {
        return new BottomUpPlanSearcher<>(
                new M1DfsRedundantPlanExtensionStrategy(ontologyProvider, schemaProviderFactory),
                new NoPruningPruneStrategy<>(),
                new CheapestPlanPruneStrategy(),
                new AllCompletePlanSelector<>(),
                new AllCompletePlanSelector<>(),
                validator,
                new PredicateCostEstimator<>(plan -> plan.getOps().size() <= 2,
                        new RegexPatternCostEstimator(ruleBaseEstimator(ont)),
                        (plan, context) -> new PlanWithCost<>(plan, context.getPreviousCost().get().getCost())));
    }

    /** {@code Match (d1:Dragon)-[f:Fire]->(d2:Dragon) return d1, distinct(f), d2}, as the Cypher translator builds it */
    private static AsgQuery distinctQuery() {
        return AsgQuery.Builder.start("Q9", "Dragons")
                .next(typed(1, OntologyTestUtils.DRAGON.type, "d1"))
                .next(ePropGroup(10))
                .next(rel(2, FIRE.getrType(), Rel.Direction.R, "f")
                        .below(relPropGroup(200, new FunctionRelProp(201, "distinct:f", Constraint.of(ConstraintOp.distinct, "f")))))
                .next(typed(3, OntologyTestUtils.DRAGON.type, "d2"))
                .next(ePropGroup(30))
                .build();
    }

    /** {@code Match (p1:Person)-[k:Know]->(p2:Person) WHERE count(k) > 15 return *} */
    private static AsgQuery countQuery() {
        return AsgQuery.Builder.start("Q7", "Dragons")
                .next(typed(1, OntologyTestUtils.PERSON.type, "p1"))
                .next(ePropGroup(10))
                .next(rel(2, KNOW.getrType(), Rel.Direction.R, "k")
                        .below(relPropGroup(200, new FunctionRelProp(201, "count", Constraint.of(CountConstraintOp.gt, "15")))))
                .next(typed(3, OntologyTestUtils.PERSON.type, "p2"))
                .next(ePropGroup(30))
                .build();
    }

    private static RelationOp relationOp(Plan plan) {
        return (RelationOp) plan.getOps().stream().filter(op -> op instanceof RelationOp).findFirst().get();
    }

    @Test
    public void distinctRelationIsPlannable() {
        for (PlanValidator<Plan, AsgQuery> validator : Arrays.<PlanValidator<Plan, AsgQuery>>asList(new M1PlanValidator(), new M2PlanValidator())) {
            PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher(validator).search(distinctQuery());
            Assert.assertNotNull(validator.getClass().getSimpleName() + ": no valid plan for distinct(f)", plan);
        }
    }

    @Test
    public void countFilteredRelationIsPlannedInQueryDirection() {
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher(new M1PlanValidator()).search(countQuery());
        Assert.assertNotNull("no valid plan for count(k)", plan);
        PlanOp first = plan.getPlan().getOps().get(0);
        Assert.assertEquals(plan.getPlan().toString(), 1, ((EntityOp) first).getAsgEbase().geteNum());
        Assert.assertEquals(plan.getPlan().toString(), Rel.Direction.R, relationOp(plan.getPlan()).getAsgEbase().geteBase().getDir());
    }
}
