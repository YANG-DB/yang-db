package com.kayhut.fuse.dispatcher.epb;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.descriptors.Descriptor;
import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class LoggingPlanSearcher<P, C, Q> implements PlanSearcher<P, C, Q> {
    public static final String injectionName = "LoggingPlanSearcher.inner";

    //region Constructors
    @Inject
    public LoggingPlanSearcher(@Named(injectionName) PlanSearcher<P, C, Q> innerPlanSearcher, Descriptor<PlanWithCost<P, C>> descriptor) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.innerPlanSearcher = innerPlanSearcher;
        this.descriptor = descriptor;
    }
    //endregion

    //region PlanSearcher Implementation
    @Override
    public PlanWithCost<P, C> search(Q query) {
       PlanWithCost<P, C> planWithCost = this.innerPlanSearcher.search(query);
       this.logger.debug("execution plan: {}", this.descriptor.describe(planWithCost));
       return planWithCost;
    }
    //endregion

    //region Fields
    private Logger logger;
    private PlanSearcher<P, C, Q> innerPlanSearcher;
    private Descriptor<PlanWithCost<P, C>> descriptor;
    //endregion
}
