package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
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

        Ontology.Accessor $ont = new Ontology.Accessor(ontologyProvider.get(query.getOnt()).get());

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

        AtomicInteger maxEnum = new AtomicInteger(Stream.ofAll(AsgQueryUtil.eNums(query)).max().get());

        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get($ont.get());

        String relationTypeName = $ont.$relation$(lastRelationOp.get().getAsgEbase().geteBase().getrType()).getName();
        Optional<GraphEdgeSchema> edgeSchema = schemaProvider.getEdgeSchema(relationTypeName);

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
                edgeSchema.get().getSource().get().getLabel().get().equals(vTypes.get(0)) ?
                        edgeSchema.get().getSource().get() :
                        edgeSchema.get().getDestination().get() :
                edgeSchema.get().getDestination().get();

        RelPropGroup relPropGroup = lastRelationFilterOp.get().getAsgEbase().geteBase().clone();

        if(lastEntityOp.get().getAsgEbase().geteBase() instanceof ETyped) {
            Stream.ofAll(lastEntityOp.get().getAsgEbase().geteBase().getReportProps())
                    .map($ont::$property$).forEach(p -> {
                Optional<GraphRedundantPropertySchema> redundantVertexProperty = endSchema
                        .getRedundantProperty(schemaProvider.getPropertySchema($ont.$property$(p.getpType()).getName()).get());
                if (redundantVertexProperty.isPresent()) {
                    RelProp relProp = RedundantSelectionRelProp.of(maxEnum.addAndGet(1), redundantVertexProperty.get().getPropertyRedundantName(),
                            p.getpType());
                    relPropGroup.getProps().add(relProp);
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
