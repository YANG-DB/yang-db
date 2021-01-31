package com.yangdb.fuse.epb.plan.extenders;

/*-
 * #%L
 * fuse-dv-epb
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

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.utils.PlanUtil;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.OntologyFinalizer;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.properties.*;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import javaslang.collection.Stream;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by moti on 5/14/2017.
 */
public class RedundantFilterPlanExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public RedundantFilterPlanExtensionStrategy(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if(!plan.isPresent()) {
            return Collections.emptyList();
        }
        Ontology.Accessor $ont = new Ontology.Accessor(ontologyProvider.get(query.getOnt())
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No target Ontology field found ", "No target Ontology found for " + query.getOnt()))));

        Plan flatPlan = PlanUtil.flat(plan.get());

        Optional<EntityOp> lastEntityOp = PlanUtil.last(flatPlan, EntityOp.class);
        if (!lastEntityOp.isPresent()) {
            return Collections.singleton(plan.get());
        }

        Optional<RelationOp> lastRelationOp = PlanUtil.prev(flatPlan, lastEntityOp.get(), RelationOp.class);
        if (!lastRelationOp.isPresent()) {
            return Collections.singleton(plan.get());
        }

        Optional<RelationFilterOp> lastRelationFilterOp = PlanUtil.next(flatPlan, lastRelationOp.get(), RelationFilterOp.class);
        if (!lastRelationFilterOp.isPresent()) {
            return Collections.singleton(plan.get());
        }

        Optional<EntityFilterOp> lastEntityFilterOp = PlanUtil.next(flatPlan, lastEntityOp.get(), EntityFilterOp.class);

        AtomicInteger maxEnum = new AtomicInteger(Stream.ofAll(AsgQueryUtil.eNums(query)).max().get());

        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get($ont.get());

        String relationTypeName = $ont.$relation$(lastRelationOp.get().getAsgEbase().geteBase().getrType()).getName();
        Iterable<GraphEdgeSchema> edgeSchemas = schemaProvider.getEdgeSchemas(relationTypeName);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return Collections.singleton(plan.get());
        }

        //currently supports a single edge schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        // label
        List<String> vTypes = new ArrayList<>();
        if(lastEntityOp.get().getAsgEbase().geteBase() instanceof ETyped) {
            vTypes.add(((ETyped) lastEntityOp.get().getAsgEbase().geteBase()).geteType());
        }
        if(lastEntityOp.get().getAsgEbase().geteBase() instanceof EUntyped){
            EUntyped eUntyped = (EUntyped) lastEntityOp.get().getAsgEbase().geteBase();
            if(eUntyped.getvTypes().size() > 0){
                vTypes.addAll(eUntyped.getvTypes());
            }else{
                vTypes.addAll(Stream.ofAll($ont.eTypes())
                        .filter(eType -> !eUntyped.getNvTypes().contains(eType)).toJavaList());
            }
        }

        // THIS IS A TEMPORARY PATCH!!!
        /*Set<String> vTypeNames = Stream.ofAll(vTypes).map(vType -> $ont.$entity$(vType).getTyped()).toJavaSet();
        Optional<GraphEdgeSchema> edgeSchema = Stream.ofAll(edgeSchemas)
                .filter(edgeSchema1 -> edgeSchema1.getDirection().isPresent() ?
                        vTypeNames.contains(edgeSchema1.getSource().get().label().get()) :
                        true)
                .toJavaOptional();*/
        // THIS IS A TEMPORARY PATCH!!!

        RelPropGroup relPropGroup = lastRelationFilterOp.get().getAsgEbase().geteBase().clone();

        //currently supports only ETyped
        GraphEdgeSchema.End endSchema = lastEntityOp.get().getAsgEbase().geteBase() instanceof ETyped ?
                                            edgeSchema.getEndA().get().getLabel().get().equals(vTypes.get(0)) ?
                                                edgeSchema.getEndA().get() :
                                                edgeSchema.getEndB().get() :
                                            edgeSchema.getEndB().get();

        if(vTypes.size() > 0){
            Constraint constraint = Constraint.of(ConstraintOp.inSet,
                    Stream.ofAll(vTypes).map(eType -> $ont.$entity$(eType).getName()).toJavaList());

            Optional<GraphRedundantPropertySchema> redundantTypeProperty = endSchema
                    .getRedundantProperty(schemaProvider.getPropertySchema($ont.$property$(OntologyFinalizer.TYPE_FIELD_PTYPE).getName()).get());

            if(redundantTypeProperty.isPresent()) {
                RelProp relProp = RedundantRelProp.of(0, redundantTypeProperty.get().getPropertyRedundantName(),
                        OntologyFinalizer.TYPE_FIELD_PTYPE, constraint);
                relPropGroup.getProps().add(relProp);
            }
        }

        if(lastEntityOp.get().getAsgEbase().geteBase() instanceof EConcrete){
            EConcrete eConcrete = (EConcrete) lastEntityOp.get().getAsgEbase().geteBase();
            Constraint constraint = Constraint.of(ConstraintOp.eq, eConcrete.geteID());

            Optional<GraphRedundantPropertySchema> redundantIdProperty = endSchema
                    .getRedundantProperty(schemaProvider.getPropertySchema($ont.$property$(OntologyFinalizer.ID_FIELD_PTYPE).getName()).get());

            if(redundantIdProperty.isPresent()) {
                RelProp relProp = RedundantRelProp.of(0, redundantIdProperty.get().getPropertyRedundantName(),
                        OntologyFinalizer.ID_FIELD_PTYPE, constraint);
                relPropGroup.getProps().add(relProp);
            }
        }

        Plan newPlan = new Plan(plan.get().getOps());

        RelPropGroup redundantRelPropGroup = null;
        EPropGroup nonRedundantEPropGroup = null;
        if(lastEntityFilterOp.isPresent()) {
            Map<EPropGroup, Redundancy> redundancy = buildRedundancyMap(lastEntityFilterOp.get().getAsgEbase().geteBase(), endSchema, schemaProvider, $ont);
            redundantRelPropGroup = buildRedundantRelPropGroup(lastEntityFilterOp.get().getAsgEbase().geteBase(), endSchema, schemaProvider, $ont, redundancy);
            nonRedundantEPropGroup = buildNonRedundantEpropGroup(lastEntityFilterOp.get().getAsgEbase().geteBase(), endSchema, schemaProvider, $ont, redundancy);
        }

        if (nonRedundantEPropGroup != null) {
            nonRedundantEPropGroup.seteNum(lastEntityFilterOp.get().getAsgEbase().geteBase().geteNum());
            nonRedundantEPropGroup.getProps().addAll(
                    Stream.ofAll(lastEntityFilterOp.get().getAsgEbase().geteBase().getProps()).filter(eprop -> eprop.getProj() != null).toJavaList());

            EntityFilterOp newEntityFilterOp = new EntityFilterOp(AsgEBase.Builder.<EPropGroup>get().withEBase(nonRedundantEPropGroup).build());
            newPlan = PlanUtil.replace(newPlan, lastEntityFilterOp.get(), newEntityFilterOp);
        }

        if (redundantRelPropGroup != null) {
            relPropGroup.getProps().addAll(redundantRelPropGroup.getProps());
            relPropGroup.getGroups().addAll(redundantRelPropGroup.getGroups());
        }

        RelationFilterOp newRelationFilterOp = new RelationFilterOp(AsgEBase.Builder.<RelPropGroup>get().withEBase(relPropGroup).build());
        newPlan = PlanUtil.replace(newPlan, lastRelationFilterOp.get(), newRelationFilterOp);

        return Collections.singleton(newPlan);
    }
    //region

    //region Private Methods
    private Map<EPropGroup, Redundancy> buildRedundancyMap(
            EPropGroup ePropGroup,
            GraphEdgeSchema.End endSchema,
            GraphElementSchemaProvider schemaProvider,
            Ontology.Accessor $ont) {
        Map<EPropGroup, Redundancy> redundancy = new HashMap<>();
        if (!ePropGroup.getGroups().isEmpty()) {
            for(EPropGroup childGroup : ePropGroup.getGroups()) {
                redundancy.putAll(buildRedundancyMap(childGroup, endSchema, schemaProvider, $ont));
            }
        }

        int numUnsafePartialGroups = Stream.ofAll(redundancy.values()).filter(redundancy1 -> redundancy1.equals(Redundancy.unsafePartial)).size();
        int numSafePartialGroups = Stream.ofAll(redundancy.values()).filter(redundancy1 -> redundancy1.equals(Redundancy.safePartial)).size();
        Redundancy groupRedundancy = numUnsafePartialGroups > 0 ?
                Redundancy.unsafePartial : numSafePartialGroups > 0 ?
                Redundancy.safePartial : Redundancy.full;

        boolean fullyRedundantProps =
                Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null)
                .map(eProp -> endSchema.getRedundantProperty(schemaProvider.getPropertySchema($ont.$property$(eProp.getpType()).getName()).get()))
                .filter(redudantProp -> !redudantProp.isPresent())
                .isEmpty();

        Redundancy propRedundancy = fullyRedundantProps ? Redundancy.full :
            ePropGroup.getQuantType().equals(QuantType.all) ?
            Redundancy.safePartial : Redundancy.unsafePartial;

        Set<Redundancy> propAndGroupRedundancies = Stream.of(groupRedundancy, propRedundancy).toJavaSet();
        Redundancy thisRedundancy = propAndGroupRedundancies.contains(Redundancy.unsafePartial) ?
                Redundancy.unsafePartial : propAndGroupRedundancies.contains(Redundancy.safePartial) ?
                Redundancy.safePartial : Redundancy.full;

        redundancy.put(ePropGroup, thisRedundancy);
        return redundancy;
    }

    private RelPropGroup buildRedundantRelPropGroup(
            EPropGroup ePropGroup,
            GraphEdgeSchema.End endSchema,
            GraphElementSchemaProvider schemaProvider,
            Ontology.Accessor $ont,
            Map<EPropGroup, Redundancy> redundancy) {

        List<RelPropGroup> childRedundantRelPropGroups =
                Stream.ofAll(ePropGroup.getGroups())
                .map(childGroup -> buildRedundantRelPropGroup(childGroup, endSchema, schemaProvider, $ont, redundancy))
                .toJavaList();

        List<RelProp> redundantRelProps = new ArrayList<>();
        Redundancy thisRedundancy = redundancy.get(ePropGroup);
        if (thisRedundancy.equals(Redundancy.safePartial) || thisRedundancy.equals(Redundancy.full)) {
            for (EProp eProp : Stream.ofAll(ePropGroup.getProps()).filter(eProp -> eProp.getCon() != null)) {
                Optional<GraphRedundantPropertySchema> redundantVertexProperty = endSchema
                        .getRedundantProperty(schemaProvider.getPropertySchema($ont.$property$(eProp.getpType()).getName()).get());
                redundantVertexProperty.ifPresent(graphRedundantPropertySchema -> redundantRelProps.add(RedundantRelProp.of(
                        0,
                        graphRedundantPropertySchema.getPropertyRedundantName(),
                        SchematicEProp.class.isAssignableFrom(eProp.getClass()) ?
                                ((SchematicEProp) eProp).getSchematicName() :
                                graphRedundantPropertySchema.getPropertyRedundantName(),
                        eProp.getpType(),
                        eProp.getCon())));
            }
        }

        return new RelPropGroup(0, ePropGroup.getQuantType(), redundantRelProps, childRedundantRelPropGroups);
    }

    private EPropGroup buildNonRedundantEpropGroup(
            EPropGroup ePropGroup,
            GraphEdgeSchema.End endSchema,
            GraphElementSchemaProvider schemaProvider,
            Ontology.Accessor $ont,
            Map<EPropGroup, Redundancy> redundancy) {

        if (redundancy.get(ePropGroup).equals(Redundancy.full)) {
            return new EPropGroup(0);
        }

        List<EPropGroup> childNonRedundantEPropGroups =
                Stream.ofAll(ePropGroup.getGroups())
                        .filter(childGroup -> !redundancy.get(childGroup).equals(Redundancy.full))
                        .toJavaList();

        List<EProp> nonRedundantEProps = new ArrayList<>();
        for (EProp eProp : Stream.ofAll(ePropGroup.getProps()).filter(eProp -> eProp.getCon() != null)) {
            if (!endSchema.getRedundantProperty(schemaProvider.getPropertySchema($ont.$property$(eProp.getpType()).getName()).get()).isPresent()) {
                nonRedundantEProps.add(eProp);
            }
        }

        return new EPropGroup(0, ePropGroup.getQuantType(), nonRedundantEProps, childNonRedundantEPropGroups);
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion

    private enum Redundancy {
        unsafePartial,
        safePartial,
        full
    }
}
