package com.kayhut.fuse.asg.strategy;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import javaslang.collection.Stream;

import java.util.Collections;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class SimpleStrategyRegisteredAsgDriver implements QueryCreationOperationContext.Processor {

    //region Constructors
    @Inject
    public SimpleStrategyRegisteredAsgDriver(EventBus eventBus, AsgStrategyRegistrar registrar) {
        this.eventBus = eventBus;
        this.eventBus.register(this);

        this.strategies = registrar != null ? registrar.register() : Collections.emptyList();
    }
    //endregion

    //region QueryCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public QueryCreationOperationContext process(QueryCreationOperationContext context) {
        if(context.getAsgQuery() != null) {
            return context;
        }
        //AsgQuery asgQuery = new RecTwoPassAsgQuerySupplier(input.getQuery()).get();
        AsgQuery asgQuery = AsgQuery.AsgQueryBuilder.anAsgQuery().build();

        Stream.ofAll(this.strategies).forEach(strategy -> strategy.apply(asgQuery));
        return submit(eventBus, context.of(asgQuery));
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    private Iterable<AsgStrategy> strategies;
    //endregion
}
