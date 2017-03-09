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
public class BottomUpPlanBuilderImpl<P,Q,C> implements PlanSearcher<P,Q,C> {
    final Logger logger = LoggerFactory.getLogger(BottomUpPlanBuilderImpl.class);

    @Inject
    public BottomUpPlanBuilderImpl(PlanExtensionStrategy<P, Q> extensionStrategy,
                                   @Named("GlobalPruningStrategy") PlanPruneStrategy<P, C> globalPruneStrategy,
                                   @Named("LocalPruningStrategy") PlanPruneStrategy<P, C> localPruneStrategy,
                                   PlanValidator<P, Q> planValidator,
                                   PlanWrapperFactory<P,Q,C> wrapperFactory) {
        this.extensionStrategy = extensionStrategy;
        this.globalPruneStrategy = globalPruneStrategy;
        this.localPruneStrategy = localPruneStrategy;
        this.planValidator = planValidator;
        this.wrapperFactory = wrapperFactory;
    }

    //region Fields
    private PlanExtensionStrategy<P,Q> extensionStrategy;
    private PlanPruneStrategy<P,C> globalPruneStrategy;
    private PlanPruneStrategy<P,C> localPruneStrategy;
    private PlanValidator<P,Q> planValidator;
    private PlanWrapperFactory<P,Q,C> wrapperFactory;
    //endregion

    //region Methods
    @Override
    public Iterable<PlanWrapper<P,C>> build(Q query, ChoiceCriteria<P,C> choiceCriteria){
        boolean shouldStop = false;
        List<PlanWrapper<P,C>> currentPlans = new LinkedList<>();

        // Generate seed plans (plan is null)
        for(P seedPlan : extensionStrategy.extendPlan(Optional.empty(), query)){
            if(planValidator.isPlanValid(seedPlan, query)){
                PlanWrapper<P,C> planWrapper = wrapperFactory.wrapPlan(seedPlan, query);
                currentPlans.add(planWrapper);
                if(choiceCriteria.addPlanAndCheckEndCondition(planWrapper)){
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
                for(P extendedPlan : extensionStrategy.extendPlan(Optional.of(partialPlan.getPlan()), query)){
                    if(planValidator.isPlanValid(extendedPlan, query)){
                        planExtensions.add(wrapperFactory.wrapPlan(extendedPlan, query));
                    }
                }
                for(PlanWrapper<P,C> pw : localPruneStrategy.prunePlans(planExtensions))
                    newPlans.add(pw);
            }

            currentPlans.clear();
            for(PlanWrapper<P,C> pw : globalPruneStrategy.prunePlans(newPlans))
                currentPlans.add(pw);
            for(PlanWrapper<P,C> planWrapper : currentPlans){
                if(choiceCriteria.addPlanAndCheckEndCondition(planWrapper)) {
                    shouldStop = true;
                    break;
                }
            }
        }

        return choiceCriteria.getChosenPlans();
    }

    //endregion

}
