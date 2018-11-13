package com.kayhut.fuse.asg;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2018 kayhut
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.asg.strategy.AsgStrategyRegistrar;
import com.kayhut.fuse.dispatcher.asg.AsgQuerySupplier;
import com.kayhut.fuse.dispatcher.asg.builder.BNextFactory;
import com.kayhut.fuse.dispatcher.asg.builder.NextEbaseFactory;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import javaslang.collection.Stream;

import java.util.Optional;

/**
 * Created by Roman on 12/15/2017.
 */
public class AsgQueryTransformer implements QueryTransformer<AsgQuery, AsgQuery> {
    //region Constructors
    @Inject
    public AsgQueryTransformer(AsgStrategyRegistrar asgStrategyRegistrar,
                               OntologyProvider ontologyProvider) {
        this.asgStrategies = asgStrategyRegistrar.register();
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region QueryTransformer Implementation
    @Override
    public AsgQuery transform(AsgQuery query) {
        Optional<Ontology> ontology = this.ontologyProvider.get(query.getOnt());
        if (!ontology.isPresent()) {
            throw new RuntimeException("unknown ontology");
        }

        AsgStrategyContext asgStrategyContext =  new AsgStrategyContext(new Ontology.Accessor(ontology.get()));
        Stream.ofAll(this.asgStrategies).forEach(strategy -> strategy.apply(query,asgStrategyContext));

        return query;
    }
    //endregion

    //region Fields
    private Iterable<AsgStrategy> asgStrategies;
    private OntologyProvider ontologyProvider;
    //endregion
}
