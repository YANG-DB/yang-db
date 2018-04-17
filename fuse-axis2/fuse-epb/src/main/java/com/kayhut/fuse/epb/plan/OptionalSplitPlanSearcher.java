package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.IQuery;
import com.kayhut.fuse.model.execution.plan.IPlan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.optional.OptionalComp;

import java.util.*;
import java.util.stream.Collectors;

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


        //plan each optional

        // append optional plans

        return null;
    }


}
