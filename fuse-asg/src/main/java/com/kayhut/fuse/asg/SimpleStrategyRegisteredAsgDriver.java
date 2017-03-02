package com.kayhut.fuse.asg;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.model.process.AsgData;
import com.kayhut.fuse.model.process.ProcessElement;
import com.kayhut.fuse.model.process.QueryData;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import org.jooq.lambda.Seq;

import java.util.Collections;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class SimpleStrategyRegisteredAsgDriver implements ProcessElement<QueryData, AsgData>, AsgDriver {
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
    public AsgData process(QueryData input) {
        //// TODO: 01/03/2017 apply asg builder
        AsgQuery asgQuery = null;
        Seq.seq(this.strategies).forEach(strategy -> strategy.apply(asgQuery));
        return submit(eventBus, new AsgData(input.getQueryMetadata(), input.getQuery()));
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    private Iterable<AsgStrategy> strategies;
    //endregion
}
