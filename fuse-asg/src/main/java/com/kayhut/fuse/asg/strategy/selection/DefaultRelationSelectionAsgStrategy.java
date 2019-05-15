package com.kayhut.fuse.asg.strategy.selection;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.model.query.properties.projection.IdentityProjection;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * Go over all RelPropGroups - if no projection was requested - go over all existing entity's props and add them as default
 */
public class DefaultRelationSelectionAsgStrategy implements AsgStrategy {
    //region Constructors
    public DefaultRelationSelectionAsgStrategy(OntologyProvider ontologyProvider) {
        this.ontologyProvider = ontologyProvider;
        this.nonSelectablePTypes = Stream.of(OntologyFinalizer.ID_FIELD_PTYPE, OntologyFinalizer.TYPE_FIELD_PTYPE)
                .toJavaSet();
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor ont = new Ontology.Accessor(this.ontologyProvider.get(query.getOnt()).get());

        AsgQueryUtil.elements(query, RelPropGroup.class).forEach(relPropGroup -> {
                    if (Stream.ofAll(relPropGroup.geteBase().getProps())
                            .filter(rProp -> rProp.getProj() != null)
//                          .filter(rProp -> !(rProp instanceof CalculatedEProp))
                            .isEmpty()) {

                        Optional<AsgEBase<Rel>> relAsgEBase = AsgQueryUtil.ancestor(relPropGroup, Rel.class);
                        if (relAsgEBase.isPresent()) {
                            List<RelProp> projectionProps =
                                    Stream.ofAll(ont.$relation$(relAsgEBase.get().geteBase().getrType()).getProperties())
                                    .filter(pType -> !this.nonSelectablePTypes.contains(pType))
                                            .map(pType -> new RelProp(0, pType, new IdentityProjection(),0))
                                            .toJavaList();

                            if (relPropGroup.geteBase().getQuantType().equals(QuantType.all)) {
                                relPropGroup.geteBase().getProps().addAll(projectionProps);
                            } else if (relPropGroup.geteBase().getQuantType().equals(QuantType.some)) {
                                RelPropGroup clone = new RelPropGroup(
                                        0,
                                        QuantType.some,
                                        relPropGroup.geteBase().getProps(),
                                        relPropGroup.geteBase().getGroups());

                                relPropGroup.geteBase().getProps().clear();
                                relPropGroup.geteBase().getGroups().clear();
                                relPropGroup.geteBase().setQuantType(QuantType.all);
                                relPropGroup.geteBase().getGroups().add(clone);
                                relPropGroup.geteBase().getProps().addAll(projectionProps);
                            }
                        }
                    }
                }
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private Set<String> nonSelectablePTypes;
    //endregion
}
