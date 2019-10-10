package com.yangdb.fuse.asg.translator.cypher.strategies;

/*-
 *
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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
 *
 */

import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerProvider;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import org.opencypher.v9_0.expressions.PatternElement;

public class LogicalGraphTransformerTranslatorStrategy implements CypherElementTranslatorStrategy<PatternElement> {

    private final OntologyProvider ontologyProvider;
    private final OntologyTransformerProvider transformerProvider;

    public LogicalGraphTransformerTranslatorStrategy(OntologyProvider ontologyProvider, OntologyTransformerProvider transformerProvider) {
        this.ontologyProvider = ontologyProvider;
        this.transformerProvider = transformerProvider;
    }

    @Override
    /**
     * expand logical graph elements to physical concrete graph elements according to the ontologyProvider & transformationProvider
     */
    public void apply(PatternElement element, AsgQuery query, CypherStrategyContext context) {
        //todo expand logical graph elements to physical concrete graph elements according to the ontologyProvider & transformationProvider
    }
}
