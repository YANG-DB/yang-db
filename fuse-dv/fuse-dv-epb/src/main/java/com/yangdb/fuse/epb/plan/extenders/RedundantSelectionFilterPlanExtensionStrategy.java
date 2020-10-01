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
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.dispatcher.utils.PlanUtil;
import com.yangdb.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.properties.*;
import com.yangdb.fuse.model.query.properties.projection.CalculatedFieldProjection;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by moti on 5/14/2017.
 */
public class RedundantSelectionFilterPlanExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public RedundantSelectionFilterPlanExtensionStrategy(
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

        //currently supports only ETyped
        GraphEdgeSchema.End endSchema = lastEntityOp.get().getAsgEbase().geteBase() instanceof ETyped ?
                edgeSchema.getEndA().get().getLabel().get().equals(vTypes.get(0)) ?
                        edgeSchema.getEndA().get() :
                        edgeSchema.getEndB().get() :
                edgeSchema.getEndB().get();

        RelPropGroup relPropGroup = lastRelationFilterOp.get().getAsgEbase().geteBase().clone();

        //add redundant (if such exist) vertex property to rel (exclude calculated fields)
        if(lastEntityFilterOp.isPresent()) {
            Stream.ofAll(lastEntityFilterOp.get().getAsgEbase().geteBase().getProps())
                    .filter(eProp -> eProp.getProj() != null)
                    .filter(eProp -> !(eProp.getProj() instanceof CalculatedFieldProjection))
                    .toJavaList()
                    .forEach(p -> {
                Optional<GraphRedundantPropertySchema> redundantVertexProperty = endSchema
                        .getRedundantProperty(schemaProvider.getPropertySchema($ont.$property$(p.getpType()).getName()).get());
                if (redundantVertexProperty.isPresent()) {
                    RelProp relProp = RedundantSelectionRelProp.of(
                            maxEnum.addAndGet(1),
                            p.getpType(),
                            redundantVertexProperty.get().getPropertyRedundantName(),
                            p.getProj());
                    relPropGroup.getProps().add(relProp);
                    lastEntityFilterOp.get().getAsgEbase().geteBase().getProps().remove(p);
                }
            });
        }

        RelationFilterOp newRelationFilterOp = new RelationFilterOp(AsgEBase.Builder.<RelPropGroup>get().withEBase(relPropGroup).build());

        Plan newPlan = new Plan(plan.get().getOps());
        newPlan = PlanUtil.replace(newPlan, lastRelationFilterOp.get(), newRelationFilterOp);

        return Collections.singleton(newPlan);
    }
    //region

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion
}
