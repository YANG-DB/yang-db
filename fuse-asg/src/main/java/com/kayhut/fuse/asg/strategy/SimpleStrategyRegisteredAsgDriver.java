package com.kayhut.fuse.asg.strategy;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.asg.builder.RecTwoPassAsgQuerySupplier;
import com.kayhut.fuse.dispatcher.ProcessElement;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.dispatcher.context.QueryExecutionContext;
import javaslang.collection.Stream;

import java.util.Collections;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class SimpleStrategyRegisteredAsgDriver implements ProcessElement {

    //region Constructors
    @Inject
    public SimpleStrategyRegisteredAsgDriver(EventBus eventBus, AsgStrategyRegistrar registrar) {
        this.eventBus = eventBus;
        this.eventBus.register(this);

        this.strategies = registrar != null ? registrar.register() : Collections.emptyList();
    }
    //endregion

    //region AsgDriver Implementation
    @Override
    @Subscribe
    public QueryExecutionContext process(QueryExecutionContext input) {
        if(!shouldRun(input))
            return input;
        AsgQuery asgQuery = new RecTwoPassAsgQuerySupplier(input.getQuery()).get();
        Stream.ofAll(this.strategies).forEach(strategy -> strategy.apply(asgQuery));
        return submit(eventBus, input.of(asgQuery));
    }

    /**
     * execute the asg phase only if the executionContext doesnt already contains this phase
     * @param input
     * @return
     */
    private boolean shouldRun(QueryExecutionContext input) {
        return !input.phase(QueryExecutionContext.Phase.asg);
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    private Iterable<AsgStrategy> strategies;
    //endregion
}
