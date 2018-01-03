package com.kayhut.fuse.dispatcher.epb;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.descriptors.Descriptor;
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
        this.logger = LoggerFactory.getLogger(innerPlanSearcher.getClass());
        this.innerPlanSearcher = innerPlanSearcher;
        this.descriptor = descriptor;
    }
    //endregion

    //region PlanSearcher Implementation
    @Override
    public PlanWithCost<P, C> search(Q query) {
        boolean thrownException = false;

        try {
            this.logger.trace("start search");
            PlanWithCost<P, C> planWithCost = this.innerPlanSearcher.search(query);
            this.logger.debug("execution plan: {}", this.descriptor.describe(planWithCost));
            return planWithCost;
        } catch (Exception ex) {
            this.logger.error("failed search", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish search");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private PlanSearcher<P, C, Q> innerPlanSearcher;
    private Descriptor<PlanWithCost<P, C>> descriptor;
    //endregion
}
