package com.yangdb.fuse.asg.strategy.selection;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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


import com.yangdb.fuse.asg.strategy.AsgStrategy;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.Tagged;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.OntologyFinalizer;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.properties.CalculatedEProp;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.projection.IdentityProjection;
import com.yangdb.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;
import org.opensearch.common.Strings;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor.describe;

/**
 *  verify all steps have an eTag identifier - create on if none exist (will be proficient for profiling)
 */
public class DefaultETagAsgStrategy implements AsgStrategy {
    //region Constructors
    public DefaultETagAsgStrategy(OntologyProvider ontologyProvider) {
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor ont = new Ontology.Accessor(this.ontologyProvider.get(query.getOnt()).get());
        AsgQueryUtil.elements(query, element->(element.geteBase() instanceof Tagged)).forEach(e-> {
            if(Strings.isEmpty(((Tagged)e.geteBase()).geteTag())){
                //if no tag exists - create one based on the entity description
                ((Tagged)e.geteBase()).seteTag(describe(e.geteBase()));
            }
        });
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    //endregion
}
