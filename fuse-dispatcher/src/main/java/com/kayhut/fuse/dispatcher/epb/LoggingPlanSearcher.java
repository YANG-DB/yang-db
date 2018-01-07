package com.kayhut.fuse.dispatcher.epb;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class LoggingPlanSearcher<P, C, Q> implements PlanSearcher<P, C, Q> {
    public static final String planSearcherParameter = "LoggingPlanSearcher.@planSearcher";
    public static final String loggerParameter = "LoggingPlanSearcher.@logger";

    //region Constructors
    @Inject
    public LoggingPlanSearcher(
            @Named(planSearcherParameter) PlanSearcher<P, C, Q> planSearcher,
            Descriptor<PlanWithCost<P, C>> descriptor,
            @Named(loggerParameter) Logger logger,
            MetricRegistry metricRegistry) {
        this.logger = logger;
        this.metricRegistry = metricRegistry;
        this.planSearcher = planSearcher;
        this.descriptor = descriptor;
    }
    //endregion

    //region PlanSearcher Implementation
    @Override
    public PlanWithCost<P, C> search(Q query) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "search")).time();

        boolean thrownException = false;

        try {
            this.logger.trace("start search");
            PlanWithCost<P, C> planWithCost = this.planSearcher.search(query);
            this.logger.debug("execution plan: {}", this.descriptor.describe(planWithCost));
            return planWithCost;
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed search", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "search", "failure")).mark();
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish search");
                this.metricRegistry.meter(name(this.logger.getName(), "search", "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private PlanSearcher<P, C, Q> planSearcher;
    private Descriptor<PlanWithCost<P, C>> descriptor;
    //endregion
}
