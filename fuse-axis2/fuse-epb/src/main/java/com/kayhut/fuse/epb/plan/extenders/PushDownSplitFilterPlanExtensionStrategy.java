package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.unipop.schemaProviders.*;
import javaslang.collection.Stream;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.ontology.OntologyUtil.getComplementaryTypes;

/**
 * Created by moti on 5/14/2017.
 */
public class PushDownSplitFilterPlanExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public PushDownSplitFilterPlanExtensionStrategy(OntologyProvider ontologyProvider, GraphElementSchemaProvider schemaProvider) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProvider = schemaProvider;
    }
    //endregion

    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if(!plan.isPresent()) {
            return Collections.emptyList();
        }

        Ontology.Accessor $ont = new Ontology.Accessor(ontologyProvider.get(query.getOnt()).get());

        Optional<EntityOp> lastEntityOp = PlanUtil.last(plan.get(), EntityOp.class);
        if (!lastEntityOp.isPresent()) {
            return Collections.emptyList();
        }

        Optional<RelationOp> lastRelationOp = PlanUtil.prev(plan.get(), lastEntityOp.get(), RelationOp.class);
        if (!lastRelationOp.isPresent()) {
            return Collections.emptyList();
        }

        Optional<RelationFilterOp> lastRelationFilterOp = PlanUtil.next(plan.get(), lastRelationOp.get(), RelationFilterOp.class);
        if (!lastRelationFilterOp.isPresent()) {
            return Collections.emptyList();
        }

        Optional<EntityFilterOp> lastEntityFilterOp = PlanUtil.next(plan.get(), lastEntityOp.get(), EntityFilterOp.class);

        AtomicInteger maxEnum = new AtomicInteger(Stream.ofAll(AsgQueryUtil.eNums(query)).max().get());

        Plan newPlan = new Plan(plan.get().getOps());

        //String relationTypeNameById = OntologyUtil.getRelationTypeNameById(ontology, nextRelation.get().geteBase().getrType());
        String relationTypeName = $ont.$relation$(lastRelationOp.get().getAsgEBase().geteBase().getrType()).getName();
        Optional<GraphEdgeSchema> edgeSchema = schemaProvider.getEdgeSchema(relationTypeName);

        // label
        List<Integer> vTypes = new ArrayList<>();
        if(lastEntityOp.get().getAsgEBase().geteBase() instanceof ETyped) {
            vTypes.add(((ETyped) lastEntityOp.get().getAsgEBase().geteBase()).geteType());
        }
        if(lastEntityOp.get().getAsgEBase().geteBase() instanceof EUntyped){
            EUntyped eUntyped = (EUntyped) lastEntityOp.get().getAsgEBase().geteBase();
            if(eUntyped.getvTypes().size() > 0){
                vTypes.addAll(eUntyped.getvTypes());
            }else{
                vTypes.addAll(Stream.ofAll($ont.eTypes())
                        .filter(eType -> !eUntyped.getNvTypes().contains(eType)).toJavaList());
            }
        }

        RelPropGroup relPropGroup = lastRelationFilterOp.get().getAsgEBase().geteBase().clone();

        if(vTypes.size() > 0){
            Constraint constraint = Constraint.of(ConstraintOp.inSet,
                    Stream.ofAll(vTypes).map(eType -> $ont.$entity$(eType).getName()).toJavaList());

            Optional<GraphRedundantPropertySchema> redundantTypeProperty = edgeSchema.get().getDestination().get()
                    .getRedundantVertexProperty($ont.$property$(OntologyFinalizer.TYPE_FIELD_P_TYPE).getName());

            if(redundantTypeProperty.isPresent()) {
                RelProp relProp = PushdownRelProp.of(maxEnum.addAndGet(1), redundantTypeProperty.get().getPropertyRedundantName(),
                        Integer.toString(OntologyFinalizer.TYPE_FIELD_P_TYPE), constraint);
                relPropGroup.getProps().add(relProp);
            }
        }

        if(lastEntityOp.get().getAsgEBase().geteBase() instanceof EConcrete){
            EConcrete eConcrete = (EConcrete) lastEntityOp.get().getAsgEBase().geteBase();
            Constraint constraint = Constraint.of(ConstraintOp.eq, eConcrete.geteID());

            Optional<GraphRedundantPropertySchema> redundantIdProperty = edgeSchema.get().getDestination().get()
                    .getRedundantVertexProperty($ont.$property$(OntologyFinalizer.ID_FIELD_P_TYPE).getName());

            if(redundantIdProperty.isPresent()) {
                RelProp relProp = PushdownRelProp.of(maxEnum.addAndGet(1), redundantIdProperty.get().getPropertyRedundantName(),
                        Integer.toString(OntologyFinalizer.ID_FIELD_P_TYPE), constraint);
                relPropGroup.getProps().add(relProp);
            }
        }

        if(lastEntityFilterOp.isPresent()) {
            AsgEBase<EPropGroup> ePropGroup = AsgEBase.Builder.<EPropGroup>get().withEBase(lastEntityFilterOp.get().getAsgEBase().geteBase().clone()).build();
            Stream.ofAll(ePropGroup.geteBase().getProps()).forEach(p -> {
                Optional<GraphRedundantPropertySchema> redundantVertexProperty = edgeSchema.get().getDestination().get()
                        .getRedundantVertexProperty($ont.$property$(Integer.parseInt(p.getpType())).getName());

                if(redundantVertexProperty.isPresent()){
                    RelProp relProp = PushdownRelProp.of(maxEnum.addAndGet(1), redundantVertexProperty.get().getPropertyRedundantName(),
                            p.getpType(), p.getCon());
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
    private GraphElementSchemaProvider schemaProvider;
    //endregion
}
