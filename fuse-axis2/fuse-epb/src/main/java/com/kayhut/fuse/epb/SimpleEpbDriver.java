package com.kayhut.fuse.epb;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.epb.plan.PlanSearcher;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by liorp on 3/20/2017.
 */
public class SimpleEpbDriver implements QueryCreationOperationContext.Processor {

    private EventBus bus;
    private PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    public SimpleEpbDriver(EventBus bus, PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher) {
        this.bus = bus;
        this.planSearcher = planSearcher;
        this.bus.register(this);
    }

    @Override
    @Subscribe
    public QueryCreationOperationContext process(QueryCreationOperationContext context) {
        //if asg not ready yet -> return
        if (context.getAsgQuery() == null) {
            return context;
        }
        //if execution plan already exist -> return
        if (context.getExecutionPlan() != null) {
            return context;
        }

        Timer.Context time = metricRegistry.timer(
                name(QueryCreationOperationContext.class.getSimpleName(),
                        context.getQueryMetadata().getId(),
                        SimpleEpbDriver.class.getSimpleName())).time();

        AsgQuery query = context.getAsgQuery();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);
        //get first
        PlanWithCost<Plan, PlanDetailedCost> first = plans.iterator().next();
        System.out.println("Selected Plan: " + first.getPlan().toString());
        time.stop();
        return submit(bus, context.of(first));
    }
}
