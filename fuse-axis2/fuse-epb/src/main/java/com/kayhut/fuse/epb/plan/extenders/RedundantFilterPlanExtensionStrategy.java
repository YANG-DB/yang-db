package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.unipop.schemaProviders.*;
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
                RelProp relProp = RedundantRelProp.of(maxEnum.addAndGet(1), redundantTypeProperty.get().getPropertyRedundantName(),
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
                RelProp relProp = RedundantRelProp.of(maxEnum.addAndGet(1), redundantIdProperty.get().getPropertyRedundantName(),
                        OntologyFinalizer.ID_FIELD_PTYPE, constraint);
                relPropGroup.getProps().add(relProp);
            }
        }

        Plan newPlan = new Plan(plan.get().getOps());

        if(lastEntityFilterOp.isPresent()) {
            AsgEBase<EPropGroup> ePropGroup = AsgEBase.Builder.<EPropGroup>get().withEBase(lastEntityFilterOp.get().getAsgEbase().geteBase().clone()).build();
            Stream.ofAll(ePropGroup.geteBase().getProps()).toJavaList().forEach(p -> {
                Optional<GraphRedundantPropertySchema> redundantVertexProperty = endSchema
                        .getRedundantProperty(schemaProvider.getPropertySchema($ont.$property$(p.getpType()).getName()).get());
                if(redundantVertexProperty.isPresent()){
                    RelProp relProp = RedundantRelProp.of(
                            maxEnum.addAndGet(1),
                            redundantVertexProperty.get().getPropertyRedundantName(),
                            SchematicEProp.class.isAssignableFrom(p.getClass()) ?
                                    ((SchematicEProp)p).getSchematicName() :
                                    redundantVertexProperty.get().getPropertyRedundantName(),
                            p.getpType(),
                            p.getCon());
                    relPropGroup.getProps().add(relProp);
                    ePropGroup.geteBase().getProps().remove(p);
                }
            });

            EntityFilterOp newEntityFilterOp = new EntityFilterOp(AsgEBase.Builder.<EPropGroup>get().withEBase(ePropGroup.geteBase()).build());
            newPlan = PlanUtil.replace(newPlan, lastEntityFilterOp.get(), newEntityFilterOp);
        }
        RelationFilterOp newRelationFilterOp = new RelationFilterOp(AsgEBase.Builder.<RelPropGroup>get().withEBase(relPropGroup).build());
        newPlan = PlanUtil.replace(newPlan, lastRelationFilterOp.get(), newRelationFilterOp);

        return Collections.singleton(newPlan);
    }
    //region

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion
}
