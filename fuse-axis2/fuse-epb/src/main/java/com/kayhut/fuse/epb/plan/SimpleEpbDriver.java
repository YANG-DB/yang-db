package com.kayhut.fuse.epb.plan;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.Cost;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by liorp on 3/20/2017.
 */
public class SimpleEpbDriver implements QueryCreationOperationContext.Processor {

    private EventBus bus;
    private PlanSearcher<Plan<Cost>, AsgQuery> planSearcher;

    @Inject
    public SimpleEpbDriver(EventBus bus,PlanSearcher<Plan<Cost>, AsgQuery> planSearcher) {
        this.bus = bus;
        this.planSearcher = planSearcher;
        this.bus.register(this);
    }

    @Override
    @Subscribe
    public QueryCreationOperationContext process(QueryCreationOperationContext context) {
        //if asg not ready yet -> return
        if(context.getAsgQuery()==null) {
            return context;
        }
        //if execution plan already exist -> return
        if(context.getExecutionPlan()!=null) {
            return context;
        }

        AsgQuery query = context.getAsgQuery();
        Iterable<Plan<Cost>> plans = planSearcher.build(query, new DefaultChoiceCriteria<Cost>());
        //get first
        Plan first = plans.iterator().next();
        return submit(bus, context.of(first));
    }
}
