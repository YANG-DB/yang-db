package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanUtil;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.OptionalOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.entity.EntityNoOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.*;

public class OptionalSplitPlanSearcher implements PlanSearcher<Plan, Cost, AsgQuery> {

    public OptionalSplitPlanSearcher(PlanSearcher<Plan, Cost, AsgQuery> mainPlanSearcher, PlanSearcher<Plan, Cost, AsgQuery> optionalPlanSearcher) {
        this.mainPlanSearcher = mainPlanSearcher;
        this.optionalPlanSearcher = optionalPlanSearcher;
    }

    private PlanSearcher<Plan,Cost,AsgQuery> mainPlanSearcher;
    private PlanSearcher<Plan,Cost,AsgQuery> optionalPlanSearcher;

    @Override
    public PlanWithCost<Plan, Cost> search(AsgQuery query) {
        // strip plan of optionals
        AsgQueryUtil.OptionalStrippedQuery optionalStrippedQuery = AsgQueryUtil.stripOptionals(query);

        //plan main query
        PlanWithCost<Plan, Cost> planWithCost = mainPlanSearcher.search(optionalStrippedQuery.getMainQuery());

        if(planWithCost == null) {
            return null;
        }

        //plan each optional
        List<Tuple2<AsgEBase<OptionalComp>,PlanWithCost<Plan, Cost>>> optionalPlansWithCost = Stream.ofAll(optionalStrippedQuery.getOptionalQueries()).map(q -> new Tuple2<>(q._1, optionalPlanSearcher.search(q._2))).toJavaList();

        // append optional plans
        Plan mainPlan = planWithCost.getPlan();

        for (Tuple2<AsgEBase<OptionalComp>,PlanWithCost<Plan, Cost>> optionalPlanWithCost : optionalPlansWithCost) {
            Plan optionalPlan = optionalPlanWithCost._2.getPlan();
            if(optionalPlan == null){
                return null;
            }
            CompositePlanOp subPlan = optionalPlan.from(PlanUtil.first$(optionalPlan, RelationOp.class));
            List<PlanOp> ops = new ArrayList<>(subPlan.getOps().size()+1);
            ops.add(new EntityNoOp(PlanUtil.first(optionalPlan, EntityOp.class).get().getAsgEbase()));
            ops.addAll(subPlan.getOps());
            OptionalOp optionalOp = new OptionalOp(optionalPlanWithCost._1 , ops);

            mainPlan = mainPlan.withOp(new GoToEntityOp(PlanUtil.first(optionalPlan, EntityOp.class).get().getAsgEbase()));
            mainPlan = mainPlan.withOp(optionalOp);

        }

        return new PlanWithCost<>(new PlanWithCost<>(mainPlan, planWithCost.getCost()));
    }


}
