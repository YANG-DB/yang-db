package com.kayhut.fuse.epb.plan.pruners;

import com.kayhut.fuse.dispatcher.epb.PlanPruneStrategy;
import com.kayhut.fuse.model.execution.plan.IPlan;
import javaslang.collection.Stream;

import java.util.*;

public class CompositePruner<P extends IPlan> implements PlanPruneStrategy<P> {
    public CompositePruner(List<PlanPruneStrategy<P>> planPruners) {
        this.planPruners = planPruners;
    }

    @Override
    public Iterable<P> prunePlans(Iterable<P> plans) {
        List<Set<P>> prunedPlansList = new ArrayList<>();
        for (PlanPruneStrategy<P> planPruner : this.planPruners) {
            prunedPlansList.add(Stream.ofAll(planPruner.prunePlans(plans)).toJavaSet());
        }

        if(prunedPlansList.size() > 0){
            Set<P> plansIntersection = new HashSet<>(prunedPlansList.get(0));

            for(int i = 1;i<prunedPlansList.size();i++){
                plansIntersection.retainAll(prunedPlansList.get(i));
            }
            return plansIntersection;
        }
        return Collections.emptyList();
    }
    private List<PlanPruneStrategy<P>> planPruners;
}
