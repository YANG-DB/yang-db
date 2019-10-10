package com.yangdb.fuse.asg.strategy;

/*-
 *
 * fuse-dv-asg
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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

import com.google.inject.Inject;
import com.yangdb.fuse.asg.strategy.constraint.*;
import com.yangdb.fuse.asg.strategy.propertyGrouping.EPropGroupingAsgStrategy;
import com.yangdb.fuse.asg.strategy.propertyGrouping.HQuantPropertiesGroupingAsgStrategy;
import com.yangdb.fuse.asg.strategy.propertyGrouping.Quant1PropertiesGroupingAsgStrategy;
import com.yangdb.fuse.asg.strategy.propertyGrouping.RelPropGroupingAsgStrategy;
import com.yangdb.fuse.asg.strategy.schema.ExactConstraintTransformationAsgStrategy;
import com.yangdb.fuse.asg.strategy.schema.LikeAnyConstraintTransformationAsgStrategy;
import com.yangdb.fuse.asg.strategy.schema.LikeConstraintTransformationAsgStrategy;
import com.yangdb.fuse.asg.strategy.selection.DefaultSelectionAsgStrategy;
import com.yangdb.fuse.asg.strategy.type.RelationPatternRangeAsgStrategy;
import com.yangdb.fuse.asg.strategy.type.UntypedInferTypeLeftSideRelationAsgStrategy;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;

import java.util.Arrays;

/**
 * Created by roman.margolis on 07/02/2018.
 */
public class M2AsgStrategyRegistrar  implements AsgStrategyRegistrar {
    //region Constructors
    @Inject
    public M2AsgStrategyRegistrar(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new AsgNamedParametersStrategy(),
                new UntypedInferTypeLeftSideRelationAsgStrategy(),
                new RelationPatternRangeAsgStrategy(),
                new EPropGroupingAsgStrategy(),
                new HQuantPropertiesGroupingAsgStrategy(),
                new Quant1PropertiesGroupingAsgStrategy(),
                new RelPropGroupingAsgStrategy(),
                new ConstraintTypeTransformationAsgStrategy(),
                new ConstraintIterableTransformationAsgStrategy(),
                new RedundantLikeConstraintAsgStrategy(),
                new RedundantLikeAnyConstraintAsgStrategy(),
                new LikeToEqTransformationAsgStrategy(),
                new ExactConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new LikeConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new LikeAnyConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new RedundantInSetConstraintAsgStrategy(),
                new RedundantInRangeConstraintAsgStrategy(),
                new RedundantPropGroupAsgStrategy(),
                new DefaultSelectionAsgStrategy(this.ontologyProvider)
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion
}
