package com.kayhut.fuse.asg.strategy.selection;

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

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.projection.IdentityProjection;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Roman on 13/06/2017.
 *
 * Go over all EPropGroups - if no projection was requested - go over all existing entity's props and add them as default
 * - similar to "select * from entity type"
 */
public class DefaultSelectionAsgStrategy implements AsgStrategy {
    //region Constructors
    public DefaultSelectionAsgStrategy(OntologyProvider ontologyProvider) {
        this.ontologyProvider = ontologyProvider;
        this.nonSelectablePTypes = Stream.of(OntologyFinalizer.ID_FIELD_PTYPE, OntologyFinalizer.TYPE_FIELD_PTYPE)
                .toJavaSet();
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor ont = new Ontology.Accessor(this.ontologyProvider.get(query.getOnt()).get());

        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
                    if (Stream.ofAll(ePropGroupAsgEBase.geteBase().getProps())
                            .filter(eProp -> eProp.getProj() != null).isEmpty()) {

                        Optional<AsgEBase<ETyped>> eTypedAsgEBase = AsgQueryUtil.ancestor(ePropGroupAsgEBase, ETyped.class);
                        List<EProp> projectionProps = Collections.emptyList();

                         if (eTypedAsgEBase.isPresent()) {
                            projectionProps =
                                    Stream.ofAll(ont.$entity$(eTypedAsgEBase.get().geteBase().geteType()).getProperties())
                                    .filter(pType -> !this.nonSelectablePTypes.contains(pType))
                                    .map(pType -> new EProp(0, pType, new IdentityProjection()))
                                    .toJavaList();
                        } else {
                            Optional<AsgEBase<EUntyped>> eUntypedAsgEBase = AsgQueryUtil.ancestor(ePropGroupAsgEBase, EUntyped.class);
                            if (eUntypedAsgEBase.isPresent()) {
                                projectionProps =
                                        Stream.ofAll(ont.entities())
                                        .flatMap(EntityType::getProperties)
                                        .filter(pType -> !this.nonSelectablePTypes.contains(pType))
                                        .map(pType -> new EProp(0, pType, new IdentityProjection()))
                                        .toJavaList();
                            }
                        }

                        if (ePropGroupAsgEBase.geteBase().getQuantType().equals(QuantType.all)) {
                            ePropGroupAsgEBase.geteBase().getProps().addAll(projectionProps);
                        } else if (ePropGroupAsgEBase.geteBase().getQuantType().equals(QuantType.some)) {
                             EPropGroup clone = new EPropGroup(
                                     0,
                                     QuantType.some,
                                     ePropGroupAsgEBase.geteBase().getProps(),
                                     ePropGroupAsgEBase.geteBase().getGroups());

                             ePropGroupAsgEBase.geteBase().getProps().clear();
                             ePropGroupAsgEBase.geteBase().getGroups().clear();
                             ePropGroupAsgEBase.geteBase().setQuantType(QuantType.all);
                             ePropGroupAsgEBase.geteBase().getGroups().add(clone);
                             ePropGroupAsgEBase.geteBase().getProps().addAll(projectionProps);
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
