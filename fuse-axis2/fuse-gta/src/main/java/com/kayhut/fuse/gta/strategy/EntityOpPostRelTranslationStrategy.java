package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.unipop.*;
import com.kayhut.fuse.model.query.entity.*;
import javaslang.Tuple2;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by benishue on 08-Mar-17.
 *
 * EConcrete =   otherV().has('promise', P.eq(Promise.as(<EConcrete.Id>)).as(<EConcrete.ETag>)
 * ETyped = otherV().has('constraint', P.eq(Constraint.by(__.has('label', P.eq(<Ontology(<ETyped.EType>)>)))).as(<ETyped.ETag>)
 * EUntyped = otherV().as(<EUntyped.ETag>)
 */
public class EntityOpPostRelTranslationStrategy implements TranslationStrategy {

    @Override
    public GraphTraversal apply(Tuple2<Plan, PlanOpBase> context, GraphTraversal traversal) {
        Plan plan = context._1;
        PlanOpBase currentPlanOpBase = context._2;
        Optional<PlanOpBase> prev = plan.getPrev(currentPlanOpBase);
        if(!prev.isPresent())
            return traversal;

        //EntityOp Post RelOp
        if(currentPlanOpBase instanceof EntityOp && prev.get() instanceof RelationOp) {
            EEntityBase eEntityBase = ((EntityOp) currentPlanOpBase).getEntity().geteBase();
            if (eEntityBase instanceof EConcrete)
                traversal.otherV().has("promise", P.eq(Promise.as(((EConcrete) eEntityBase).geteID()))).as(eEntityBase.geteTag());
            else if (eEntityBase instanceof ETyped)
                traversal.otherV().has("constraint", P.eq(Constraint.by(__.has("label",P.eq(((ETyped) eEntityBase).geteType())))))
                        .as(eEntityBase.geteTag());
            else if (eEntityBase instanceof EUntyped)
                traversal.otherV().as(eEntityBase.geteTag());
        }
        return traversal;

    }


}
