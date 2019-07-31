package com.yangdb.fuse.assembly.knowledge.asg;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.yangdb.fuse.asg.strategy.AsgStrategy;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.Typed;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * replace logical graph query (fields within the Entity) into knowledge graph query (RDF structure - fields as separate nodes)
 */
public class KnowledgeLogicalEntityGraphTranslatorStrategy implements AsgStrategy {
    //region Constructors

    public KnowledgeLogicalEntityGraphTranslatorStrategy(OntologyProvider ontologyProvider,
                                                         String eType, String pType, Class<? extends EBase> clazz) {
        this.ontologyProvider = ontologyProvider;
        this.eType = eType;
        this.pType = pType;
        this.clazz = clazz;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor ont = new Ontology.Accessor(this.ontologyProvider.get(query.getOnt()).get());
        AtomicInteger counter = new AtomicInteger(AsgQueryUtil.max(query));

        //exit if base is of Evalue
        if (filterOut(query)) return;

        //break group into multiple eProps within quant
        List<EPropGroup> ePropsGroup = Stream.ofAll(AsgQueryUtil.elements(query, EPropGroup.class))
                .map(AsgEBase::geteBase).toJavaList();

        ePropsGroup.forEach(g -> {
            AsgEBase<EBase> group = AsgQueryUtil.get(query.getStart(), g.geteNum()).get();
            //if group has no quant parent -> make one ...
            if (group.getParents().stream().noneMatch(p -> p.geteBase() instanceof QuantBase)) {
                //create quant as parent to group
                group.getParents().forEach(p -> AsgQueryUtil.addAsNext(new AsgEBase<>(new Quant1(counter.incrementAndGet(), QuantType.all)), p));
            }
            AsgEBase<? extends EBase> quant = group.getParents().stream().filter(p -> p.geteBase() instanceof QuantBase).findAny().get();
            //add eprops directly under quant
            g.findAll(p -> true).forEach(p -> quant.addNext(p.isProjection() ?
                    new AsgEBase<>(new EProp(counter.incrementAndGet(), p.getpType(), p.getProj())) :
                    new AsgEBase<>(new EProp(counter.incrementAndGet(), p.getpType(), p.getCon()))));

            quant.removeNextChild(group);
        });

        //process Fields (EProps)
        AsgQueryUtil.getEprops(query)
                .stream()
                //skip entity (metadata) properties
                .filter(eProp -> !ont.entity$(eType).getProperties().contains(eProp.getpType()))
                .forEach(eProp -> {
                    //this eprop is not metadata -> should be moved to EValue step
                    Optional<AsgEBase<EBase>> asgEprop = AsgQueryUtil.get(query.getStart(), eProp.geteNum());
                    //replace Evalue
                    replace(counter, ont, eProp, asgEprop, AsgQueryUtil.pathToAncestor(asgEprop.get(), clazz), pType);
                });
    }

    private void replace(AtomicInteger counter, Ontology.Accessor ont, EProp eProp, Optional<AsgEBase<EBase>> asgEprop, List<AsgEBase<? extends EBase>> path, String pType) {
        if (path.isEmpty())
            return;

        //property must be directly under EEntityBase or under QuantBase which in turn is under EEntityBase - total max 2 hops
        AsgEBase<EBase> eBase = (AsgEBase<EBase>) path.get(path.size() - 1);

        //only manage eType entities according to ctor param
        if(!((Typed) eBase.geteBase()).getTyped().equals(eType))
            return;

        //exit if base is of Evalue / Rvalue
        if (path.stream()
                .filter(p -> p instanceof Typed)
                .anyMatch(e -> ((Typed) e.geteBase()).getTyped().equals(pType))) return;


        // skip unmatched properties which are not a composite propertyType structure ${fieldId}.pType
        if (!eProp.getpType().contains(".") && !ont.entity$(pType).getProperties().contains(eProp.getpType()))
            return;

        // skip unmatched properties which are a composite propertyType structure ${fieldId}.pType and second property is not unmatched
        if (eProp.getpType().contains("[.]") && !ont.entity$(pType).getProperties().contains(eProp.getpType().split("[.]")[1]))
            return;


        if (path.size() <= 3) {
            //add the EValue type node to the quant with hasEvalue rel in between
            Optional<AsgEBase<? extends EBase>> quant = path.stream().filter(e -> e.geteBase() instanceof QuantBase).findAny();
            if (!quant.isPresent()) {
                //add new Quant
                quant = Optional.of(new AsgEBase<>(new Quant1(counter.incrementAndGet(), QuantType.all)));
                AsgQueryUtil.addAsNext((AsgEBase<Quant1>) quant.get(), eBase);
            }
            //remove eprop since it will be added later as part of the evalue chain
            quant.get().removeNextChild(asgEprop.get());

            //create rel->eValue->eProp chain
            AsgEBase<Rel> hasValue = new AsgEBase<>(new Rel(counter.incrementAndGet(), "has" + pType, Rel.Direction.R, null, counter.incrementAndGet()));
            AsgEBase<EBase> value = new AsgEBase<>(new ETyped(counter.get(), "V." + eProp.geteNum(), pType, asgEprop.get().geteNum()));
            hasValue.addNext(value);

            //in case of composite eProp name -> add fieldId & ***Value as EpropGroup
            if (eProp.getpType().contains(".")) {
                String fieldIdName = eProp.getpType().split("[.]")[0];
                String fieldType = eProp.getpType().split("[.]")[1];
                //change composite field type to specific field type
                eProp.setpType(fieldType);

                EPropGroup group = new EPropGroup(asgEprop.get().geteNum(),
                        new EProp(counter.get(), "fieldId", Constraint.of(ConstraintOp.eq, fieldIdName)),
                        eProp);
                asgEprop.get().seteBase(group);
            }

            value.addNext(asgEprop.get());
            //add chain to quant
            quant.get().addNext(hasValue);
        }
    }

    private boolean filterOut(AsgQuery query) {
        return AsgQueryUtil.element(query, asgEBase -> (asgEBase.geteBase() instanceof Typed)
                && pType.equals(((Typed) asgEBase.geteBase()).getTyped())).isPresent();
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private String eType;
    private String pType;
    private Class<? extends EBase> clazz;
    //endregion
}
