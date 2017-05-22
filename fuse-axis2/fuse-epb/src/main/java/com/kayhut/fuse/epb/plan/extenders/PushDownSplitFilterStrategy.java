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
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;

import java.util.*;
import java.util.stream.Collectors;

import static com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils.getNextAncestorOfType;
import static com.kayhut.fuse.model.ontology.OntologyUtil.getComplementaryTypes;

/**
 * Created by moti on 5/14/2017.
 */
public class PushDownSplitFilterStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProvider schemaProvider;

    @Inject
    public PushDownSplitFilterStrategy(OntologyProvider ontologyProvider, GraphElementSchemaProvider schemaProvider) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProvider = schemaProvider;
    }

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if(!plan.isPresent())
            return Collections.EMPTY_LIST;

        Ontology ontology = ontologyProvider.get(query.getOnt()).get();

        Plan newPlan = new Plan(plan.get().getOps());
        Optional<AsgEBase<Rel>> nextRelation = getNextAncestorOfType(plan.get(), Rel.class);
        if (!nextRelation.isPresent()) {
            return Collections.emptyList();
        }

        Optional<AsgEBase<RelPropGroup>> nextRelationPropGroup = AsgQueryUtil.bDescendant(nextRelation.get(), RelPropGroup.class);
        Optional<AsgEBase<EEntityBase>> toEntity = AsgQueryUtil.nextDescendant(nextRelation.get(), EEntityBase.class);
        Optional<AsgEBase<EPropGroup>> toEntityPropGroup = AsgQueryUtil.nextDescendant(toEntity.get(), EPropGroup.class);

        String relationTypeNameById = OntologyUtil.getRelationTypeNameById(ontology, nextRelation.get().geteBase().getrType());
        Optional<GraphEdgeSchema> edgeSchema = schemaProvider.getEdgeSchema(relationTypeNameById);

        // label
        List<Integer> labels = new ArrayList<>();
        if(toEntity.get().geteBase() instanceof ETyped) {
            labels.add(((ETyped) toEntity.get().geteBase()).geteType());
        }
        if(toEntity.get().geteBase() instanceof EUntyped){
            EUntyped eUntyped = (EUntyped) toEntity.get().geteBase();
            if(eUntyped.getvTypes().size() > 0){
                labels.addAll(eUntyped.getvTypes());
            }else{
                labels.addAll(getComplementaryTypes(ontology, eUntyped));
            }
        }

        RelPropGroup relPropGroup = nextRelationPropGroup.get().geteBase().clone();

        if(labels.size() > 0){
            Constraint constraint = Constraint.of(ConstraintOp.inSet, labels.stream().map(l -> OntologyUtil.getEntityTypeNameById(ontology, l)).collect(Collectors.toList()));
            Optional<GraphRedundantPropertySchema> redundantTypeProperty = edgeSchema.get().getDestination().get().getRedundantVertexProperty(OntologyUtil.getProperty(ontology, OntologyFinalizer.TYPE_FIELD_P_TYPE).get().getName());
            if(redundantTypeProperty.isPresent()) {
                RelProp relProp = PushdownRelProp.of(redundantTypeProperty.get().getPropertyRedundantName(), Integer.toString(OntologyFinalizer.TYPE_FIELD_P_TYPE), toEntity.get().geteNum(), constraint);
                relPropGroup.getProps().add(relProp);
            }
        }
        if(toEntity.get().geteBase() instanceof EConcrete){
            EConcrete eConcrete = (EConcrete) toEntity.get().geteBase();
            Constraint constraint = Constraint.of(ConstraintOp.eq, eConcrete.geteID());
            Optional<GraphRedundantPropertySchema> redundantIdProperty = edgeSchema.get().getDestination().get().getRedundantVertexProperty(OntologyUtil.getProperty(ontology, OntologyFinalizer.ID_FIELD_P_TYPE).get().getName());
            if(redundantIdProperty.isPresent()) {
                RelProp relProp = PushdownRelProp.of(redundantIdProperty.get().getPropertyRedundantName(), Integer.toString(OntologyFinalizer.ID_FIELD_P_TYPE), toEntity.get().geteNum(), constraint);
                relPropGroup.getProps().add(relProp);
            }
        }
        //
        if(toEntityPropGroup.isPresent()) {
            AsgEBase<EPropGroup> eProp = AsgEBase.Builder.<EPropGroup>get().withEBase(toEntityPropGroup.get().geteBase().clone()).build();
            List<EProp> ePropsToRemove = new LinkedList<>();
            eProp.geteBase().getProps().forEach(p -> {
                Optional<GraphRedundantPropertySchema> redundantVertexProperty = edgeSchema.get().getDestination().get().getRedundantVertexProperty(OntologyUtil.getProperty(ontology, Integer.parseInt(p.getpType())).get().getName());
                if(redundantVertexProperty.isPresent()){
                    RelProp relProp = PushdownRelProp.of(redundantVertexProperty.get().getPropertyRedundantName(),p.getpType(), toEntityPropGroup.get().geteNum(), p.getCon());
                    relPropGroup.getProps().add(relProp);
                    ePropsToRemove.add(p);
                }
            });
            eProp.geteBase().getProps().removeAll(ePropsToRemove);

            EntityFilterOp entityFilterOp = new EntityFilterOp(AsgEBase.Builder.<EPropGroup>get().withEBase(eProp.geteBase()).build());
            EntityFilterOp oldFilterOp = PlanUtil.findFirst(newPlan,EntityFilterOp.class,
                    predicate -> ((EntityFilterOp) predicate).getAsgEBase().equals(toEntityPropGroup.get()));

            PlanUtil.replace(newPlan,oldFilterOp,entityFilterOp);

        }

        RelationFilterOp relationFilterOp = new RelationFilterOp(AsgEBase.Builder.<RelPropGroup>get().withEBase(relPropGroup).build());
        RelationFilterOp oldFilterOp = PlanUtil.findFirst(newPlan,RelationFilterOp.class,
                predicate -> ((RelationFilterOp) predicate).getAsgEBase().equals(nextRelationPropGroup.get()));

        PlanUtil.replace(newPlan,oldFilterOp,relationFilterOp);

        return Collections.singleton(newPlan);
    }

}
