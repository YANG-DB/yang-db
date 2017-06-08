package com.kayhut.fuse.asg.strategy;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.dispatcher.asg.AsgQuerySupplier;
import com.kayhut.fuse.dispatcher.asg.builder.BNextFactory;
import com.kayhut.fuse.dispatcher.asg.builder.NextEbaseFactory;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import javaslang.collection.Stream;

import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by lior on 20/02/2017.
 */
@Singleton
public class ValidatorStrategyRegisteredAsgDriver implements QueryCreationOperationContext.Processor {

    //region Constructors
    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    public ValidatorStrategyRegisteredAsgDriver(
            EventBus eventBus,
            AsgValidatorStrategyRegistrar registrar,
            OntologyProvider ontologyProvider) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
        this.ontologyProvider = ontologyProvider;

        this.strategies = registrar.register();
    }
    //endregion

    //region QueryCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public QueryCreationOperationContext process(QueryCreationOperationContext context) {
        if(context.getAsgQuery() != null) {
            return context;
        }

        Optional<Ontology> ontology = this.ontologyProvider.get(context.getQuery().getOnt());
        if (!ontology.isPresent()) {
            throw new RuntimeException("No ontology provided");
        }

        Timer.Context time = metricRegistry.timer(
                name(QueryCreationOperationContext.class.getSimpleName(),
                        context.getQueryMetadata().getId(),
                        ValidatorStrategyRegisteredAsgDriver.class.getSimpleName())).time();


        AsgStrategyContext asgStrategyContext =  new AsgStrategyContext(new Ontology.Accessor(ontology.get()));
        AsgQuery asgQuery = new AsgQuerySupplier(context.getQuery(),new NextEbaseFactory(), new BNextFactory() ).get();
        Stream.ofAll(this.strategies).forEach(strategy -> strategy.apply(asgQuery,asgStrategyContext));

        time.stop();
        return submit(eventBus, context.of(asgQuery));
    }
    //endregion

    //region Fields
    private EventBus eventBus;
    private Iterable<AsgValidatorStrategy> strategies;
    private OntologyProvider ontologyProvider;
    //endregion
}
