package com.kayhut.fuse.epb.plan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 2/21/2017.
 */
public class BottomUpPlanBuilderImpl<P,C,Q> implements PlanSearcher<P,C,Q> {
    final Logger logger = LoggerFactory.getLogger(BottomUpPlanBuilderImpl.class);

    //region Fields
    private PlanExtensionStrategy<P,Q> extensionStrategy;
    private PlanPruneStrategy<P,C> globalPruneStrategy;
    private PlanPruneStrategy<P,C> localPruneStrategy;
    private PlanValidator<P,Q> planValidator;
    private PlanWrapperFactory<P, C, Q> wrapperFactory;
    //endregion

    @Override
    public Iterable<PlanWrapper<P,C>> build(Q query, ChoiceCriteria<P,C,Q> choiceCriteria){
        boolean shouldStop = false;
        List<PlanWrapper<P,C>> currentPlans = new LinkedList<>();

        // Generate seed plans (plan is null)
        for(P seedPlan : extensionStrategy.extendPlan(null, query)){
            if(planValidator.isPlanValid(seedPlan, query)){
                PlanWrapper<P, C> planWrapper = wrapperFactory.wrapPlan(seedPlan, query);
                currentPlans.add(planWrapper);
                if(choiceCriteria.addPlanAndCheckEndCondition(planWrapper, query)){
                    shouldStop = true;
                    break;
                }
            }
        }

        // As long as we have search options, branch the search tree
        while(currentPlans.size() > 0 && !shouldStop)
        {
            List<PlanWrapper<P,C>> newPlans = new LinkedList<>();
            for(PlanWrapper<P,C> partialPlan : currentPlans){
                List<PlanWrapper<P,C>> planExtensions = new LinkedList<>();
                for(P extendedPlan : extensionStrategy.extendPlan(partialPlan.getPlan(), query)){
                    if(planValidator.isPlanValid(extendedPlan, query)){
                        planExtensions.add(wrapperFactory.wrapPlan(extendedPlan, query));
                    }
                }
                newPlans.addAll(localPruneStrategy.prunePlans(planExtensions));
            }

            currentPlans = globalPruneStrategy.prunePlans(newPlans);
            for(PlanWrapper<P,C> planWrapper : currentPlans){
                if(choiceCriteria.addPlanAndCheckEndCondition(planWrapper, query)) {
                    shouldStop = true;
                    break;
                }
            }
        }

        return choiceCriteria.getChosenPlans();
    }


}
