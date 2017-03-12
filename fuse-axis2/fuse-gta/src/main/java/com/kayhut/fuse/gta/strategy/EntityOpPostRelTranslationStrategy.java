package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.unipop.*;
import com.kayhut.fuse.model.query.entity.*;
import javaslang.Tuple2;
import org.apache.tinkerpop.gremlin.process.traversal.*;
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
    public GraphTraversal apply(TranslationStrategyContext context, GraphTraversal traversal) {
        Plan plan = context.getPlan();
        PlanOpBase currentPlanOpBase = context.getPlanOpBase();
        PlanUtil planUtil = new PlanUtil();
        Optional<PlanOpBase> prev = planUtil.getPrev(plan.getOps(), currentPlanOpBase);
        if(!prev.isPresent()) {
            return traversal;
        }

        //EntityOp Post RelOp
        if(currentPlanOpBase instanceof EntityOp && prev.get() instanceof RelationOp) {
            EEntityBase eEntityBase = ((EntityOp) currentPlanOpBase).getEntity().geteBase();

            traversal.otherV();

            if (eEntityBase instanceof EConcrete) {
                traversal.has("promise", P.eq(Promise.as(((EConcrete) eEntityBase).geteID())));
            } else if (eEntityBase instanceof ETyped) {
                traversal.has("constraint", P.eq(Constraint.by(__.has("label", P.eq(((ETyped) eEntityBase).geteType())))));
            } else if (eEntityBase instanceof EUntyped) {
                ;
            }

            traversal.as(eEntityBase.geteTag());
        }
        return traversal;

    }


}
