package com.kayhut.fuse.epb.plan;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 2/21/2017.
 */
public class BottomUpPlanBuilderImpl<P,Q> implements PlanSearcher<P,Q> {
    final Logger logger = LoggerFactory.getLogger(BottomUpPlanBuilderImpl.class);

    @Inject
    public BottomUpPlanBuilderImpl(PlanExtensionStrategy<P, Q> extensionStrategy,
                                   @Named("GlobalPruningStrategy") PlanPruneStrategy<P> globalPruneStrategy,
                                   @Named("LocalPruningStrategy") PlanPruneStrategy<P> localPruneStrategy,
                                   PlanValidator<P, Q> planValidator) {
        this.extensionStrategy = extensionStrategy;
        this.globalPruneStrategy = globalPruneStrategy;
        this.localPruneStrategy = localPruneStrategy;
        this.planValidator = planValidator;
    }

    //region Fields
    private PlanExtensionStrategy<P,Q> extensionStrategy;
    private PlanPruneStrategy<P> globalPruneStrategy;
    private PlanPruneStrategy<P> localPruneStrategy;
    private PlanValidator<P,Q> planValidator;
    //endregion

    //region Methods
    @Override
    public Iterable<P> build(Q query, ChoiceCriteria<P> choiceCriteria){
        boolean shouldStop = false;
        List<P> currentPlans = new LinkedList<>();

        // Generate seed plans (plan is null)
        for(P seedPlan : extensionStrategy.extendPlan(Optional.empty(), query)){
            if(planValidator.isPlanValid(seedPlan, query)){
                currentPlans.add(seedPlan);
                if(choiceCriteria.addPlanAndCheckEndCondition(seedPlan)){
                    shouldStop = true;
                    break;
                }
            }
        }

        // As long as we have search options, branch the search tree
        while(currentPlans.size() > 0 && !shouldStop)
        {
            List<P> newPlans = new LinkedList<>();
            for(P partialPlan : currentPlans){
                List<P> planExtensions = new LinkedList<>();
                for(P extendedPlan : extensionStrategy.extendPlan(Optional.of(partialPlan), query)){
                    if(planValidator.isPlanValid(extendedPlan, query)){
                        planExtensions.add(extendedPlan);
                    }
                }
                for(P plan : localPruneStrategy.prunePlans(planExtensions))
                    newPlans.add(plan);
            }

            currentPlans.clear();
            for(P plan : globalPruneStrategy.prunePlans(newPlans)){
                currentPlans.add(plan);
                if(choiceCriteria.addPlanAndCheckEndCondition(plan)) {
                    shouldStop = true;
                    break;
                }
            }
        }

        return choiceCriteria.getChosenPlans();
    }

    //endregion

}
