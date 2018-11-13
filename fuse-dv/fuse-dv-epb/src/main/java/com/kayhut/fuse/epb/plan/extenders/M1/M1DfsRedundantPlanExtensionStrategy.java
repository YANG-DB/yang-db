package com.kayhut.fuse.epb.plan.extenders.M1;

/*-
 * #%L
 * fuse-dv-epb
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
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.*;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

/**
 * Created by Roman on 22/05/2017.
 */
public class M1DfsRedundantPlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public M1DfsRedundantPlanExtensionStrategy(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        super();

        this.innerExtenders = Stream.<PlanExtensionStrategy<Plan, AsgQuery>>of(
                        new ChainPlanExtensionStrategy<>(
                                new CompositePlanExtensionStrategy<>(
                                        new InitialPlanGeneratorExtensionStrategy(),
                                        new StepAdjacentDfsStrategy(),
                                        new OptionalOpExtensionStrategy(this)
                                ),
                                new OptionalInitialExtensionStrategy(),
                                new RedundantFilterPlanExtensionStrategy(
                                        ontologyProvider,
                                        schemaProviderFactory),
                                new RedundantSelectionFilterPlanExtensionStrategy(
                                        ontologyProvider,
                                        schemaProviderFactory)
                        )
        ).toJavaList();
    }
    //endregion
}
