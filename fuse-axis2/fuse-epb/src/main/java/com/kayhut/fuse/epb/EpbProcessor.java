package com.kayhut.fuse.epb;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.utils.TimerAnnotation;
import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by liorp on 3/20/2017.
 */
public class EpbProcessor implements QueryCreationOperationContext.Processor {

    private EventBus bus;
    private PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;

    @Inject
    public EpbProcessor(EventBus bus, PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher) {
        this.bus = bus;
        this.planSearcher = planSearcher;
        this.bus.register(this);
    }

    @Override
    @Subscribe
    @TimerAnnotation
    public QueryCreationOperationContext process(QueryCreationOperationContext context) {
        //if asg not ready yet -> return
        if (context.getAsgQuery() == null) {
            return context;
        }
        //if execution plan already exist -> return
        if (context.getExecutionPlan() != null) {
            return context;
        }

        AsgQuery query = context.getAsgQuery();
        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query);

        return submit(bus, context.of(plan));
    }
}
