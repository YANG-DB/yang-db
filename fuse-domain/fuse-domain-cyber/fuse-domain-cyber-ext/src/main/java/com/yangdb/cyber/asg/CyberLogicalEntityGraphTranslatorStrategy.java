package com.yangdb.cyber.asg;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
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
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * replace logical graph query (fields within the Entity) into knowledge graph query (RDF structure - fields as separate nodes)
 */
public class CyberLogicalEntityGraphTranslatorStrategy implements AsgStrategy {
    public static final String CYBER = "Cyber";
    public static final String ENTITY = "Entity";
    public static final String EVALUE = "Evalue";
    public static final String FIELD_ID = "fieldId";
    public static final String RELATED_ENTITY = "relatedEntity";

    //region Constructors

    public CyberLogicalEntityGraphTranslatorStrategy(GraphElementSchemaProviderFactory schemaProviderFactory, OntologyProvider ontologyProvider,
                                                     Class<? extends EBase> clazz) {
        this.schemaProviderFactory = schemaProviderFactory;
        this.ontologyProvider = ontologyProvider;
        this.clazz = clazz;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        //only transform logical ontologies
        if (query.getOnt().equals(CYBER))
            return;

        Ontology rdfOntology = ontologyProvider.get(CYBER)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology found ", "No Ontology found with name [" + CYBER+"]")));

        String labelFieldName = schemaProviderFactory.get(rdfOntology).getLabelFieldName().get();
        Ontology logicalOntology = this.ontologyProvider.get(query.getOnt()).get();
        Ontology.Accessor logicalOntAccessor = new Ontology.Accessor(logicalOntology);
        Ontology.Accessor knowledgeOntAccessor = new Ontology.Accessor(rdfOntology);

        AtomicInteger counter = new AtomicInteger(AsgQueryUtil.max(query));

        //break logical properties consttaints to knowledge entity constraint (Evalue)
        translateLogicalProperties(labelFieldName, query, counter, logicalOntAccessor, knowledgeOntAccessor);

        //break logical entity to knowledge entity with constraint on Category (label)
        translateLogicalEntity(labelFieldName, query, counter, logicalOntAccessor, knowledgeOntAccessor);

        //break logical relations to knowledge entity with constraint on Category (label)
        translateLogicalRelation(labelFieldName, query, counter, logicalOntAccessor, knowledgeOntAccessor);

        // after logical transformation finished, change ontology to Knowledge
        query.setOnt(CYBER);
    }

    /**
     * translate logical entity to knowledge entity with constraint on Category (label)
     *
     * @param labelFieldName
     * @param query
     * @param counter
     * @param logicalOntAccessor
     * @param knowledgeOntAccessor
     */
    private void translateLogicalRelation(String labelFieldName, AsgQuery query, AtomicInteger counter, Ontology.Accessor logicalOntAccessor, Ontology.Accessor knowledgeOntAccessor) {
        //get all relation types from query skip knowledge ontology types
        List<Rel> rTyped = Stream.ofAll(AsgQueryUtil.elements(query, Rel.class))
                .filter(r -> !knowledgeOntAccessor.rType(r.geteBase().getrType()).isPresent())
                .map(AsgEBase::geteBase).toJavaList();

        rTyped.forEach(e -> {
            AsgEBase<EBase> rel = AsgQueryUtil.get(query.getStart(), e.geteNum()).get();
            //change logical type to Knowledge type
            String logicalType = ((Rel) rel.geteBase()).getrType();
            ((Rel) rel.geteBase()).setrType(RELATED_ENTITY);
            //set logical type as label constraint
            rel.addBChild(new AsgEBase<>(new RelProp(counter.incrementAndGet(), labelFieldName, Constraint.of(ConstraintOp.eq, logicalType))));

        });

    }

    /**
     * translate logical entity to knowledge entity with constraint on Category (label)
     *
     * @param query
     * @param counter
     * @param logicalOntAccessor
     * @param knowledgeOntAccessor
     */
    private void translateLogicalEntity(String labelFieldName, AsgQuery query, AtomicInteger counter, Ontology.Accessor logicalOntAccessor, Ontology.Accessor knowledgeOntAccessor) {
        //get all entity types from query skip knowledge ontology types
        List<ETyped> eTyped = Stream.ofAll(AsgQueryUtil.elements(query, ETyped.class))
                .filter(r -> !knowledgeOntAccessor.eType(r.geteBase().geteType()).isPresent())
                .map(AsgEBase::geteBase).toJavaList();

        eTyped.forEach(e -> {
            AsgEBase<EBase> entity = AsgQueryUtil.get(query.getStart(), e.geteNum()).get();
            //if group has no quant children -> make one ...
            if (entity.getNext().stream().noneMatch(p -> p.geteBase() instanceof QuantBase)) {
                //create quant as child to entity
                AsgQueryUtil.addAsNext(new AsgEBase<>(new Quant1(counter.incrementAndGet(), QuantType.all)), entity);
            }
            AsgEBase<? extends EBase> quant = entity.getNext().stream().filter(p -> p.geteBase() instanceof QuantBase).findAny().get();

            //change logical type to Knowledge type
            String logicalType = ((ETyped) entity.geteBase()).geteType();
            ((ETyped) entity.geteBase()).seteType(ENTITY);

            //get property group if exists
            if (quant.getNext().stream().anyMatch(c -> c.geteBase() instanceof EPropGroup)) {
                //find or create property group
                AsgEBase<? extends EBase> group = quant.getNext().stream().filter(p -> p.geteBase() instanceof EPropGroup).findAny().get();
                ((EPropGroup) group.geteBase()).getProps().add(new EProp(counter.incrementAndGet(), labelFieldName, Constraint.of(ConstraintOp.eq, logicalType)));
            } else {
                //add property constraint directly to quant
                quant.next(new AsgEBase<>(new EProp(counter.incrementAndGet(), labelFieldName, Constraint.of(ConstraintOp.eq, logicalType))));
            }

        });
    }

    /**
     * translate logical properties into Evalue constraints
     *
     * @param labelFieldName
     * @param query
     * @param counter
     * @param logicalOntAccessor
     */
    private void translateLogicalProperties(String labelFieldName, AsgQuery query, AtomicInteger counter, Ontology.Accessor logicalOntAccessor, Ontology.Accessor knowledgeOntAccessor) {
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
                .filter(eProp -> !logicalOntAccessor.containsMetadata(eProp.getpType()))
                .forEach(eProp -> {
                    //this eprop is not metadata -> should be moved to EValue step
                    Optional<AsgEBase<EBase>> asgEprop = AsgQueryUtil.get(query.getStart(), eProp.geteNum());
                    //replace Evalue
                    replace(counter, logicalOntAccessor, eProp, asgEprop, AsgQueryUtil.pathToAncestor(asgEprop.get(), clazz), EVALUE);
                });

    }

    private void replace(AtomicInteger counter, Ontology.Accessor ont, EProp eProp, Optional<AsgEBase<EBase>> asgEprop, List<AsgEBase<? extends EBase>> path, String pType) {
        if (path.isEmpty())
            return;

        //property must be directly under EEntityBase or under QuantBase which in turn is under EEntityBase - total max 2 hops
        AsgEBase<EBase> eBase = (AsgEBase<EBase>) path.get(path.size() - 1);

        //exit if base is of Evalue / Rvalue
        if (path.stream()
                .filter(p -> p instanceof Typed)
                .anyMatch(e -> ((Typed) e.geteBase()).getTyped().equals(pType))) return;


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

            //change logical field type to knowledge field type according to logical ontology
            String fieldIdName = eProp.getpType();
            String fieldType = String.format("%sValue", ont.property$(fieldIdName).getType());
            eProp.setpType(fieldType);

            EPropGroup group = new EPropGroup(asgEprop.get().geteNum(),
                    new EProp(counter.get(), FIELD_ID, Constraint.of(ConstraintOp.eq, fieldIdName)),
                    eProp);
            asgEprop.get().seteBase(group);

            value.addNext(asgEprop.get());
            //add chain to quant
            quant.get().addNext(hasValue);
        }
    }
    //endregion

    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //region Fields
    private OntologyProvider ontologyProvider;
    private Class<? extends EBase> clazz;
    //endregion
}
