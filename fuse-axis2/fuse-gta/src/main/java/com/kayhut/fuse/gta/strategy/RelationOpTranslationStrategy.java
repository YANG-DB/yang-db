package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.strategy.utils.ConverstionUtil;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;

/**
 * Created by benishue on 08-Mar-17.
 *
 *
 *
 * relOp = outE('promise').has('constraint', P.eq(Constraint.by(__.and(
 *                  __.has('label', P.eq(<Ontology(<rel.rType>)>)),
 *                  __.has('direction', P.eq(TinkerPop.direction(<Rel.dir>)))))))
 *                  .as(source(<EEntityBase.Etag>)-->target(<EEntityBase.Etag></EEntityBase.Etag>))  // A-->B
 *
 */
public class RelationOpTranslationStrategy implements TranslationStrategy {
    //region TranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanOpBase planOp, TranslationStrategyContext context) {
        if (!(planOp instanceof RelationOp)) {
            return traversal;
        }

        Optional<EntityOp> prev = PlanUtil.getPrev(context.getPlan(), planOp, EntityOp.class);
        Optional<EntityOp> next = PlanUtil.getNext(context.getPlan(), planOp, EntityOp.class);

        Rel rel = ((RelationOp)planOp).getAsgEBase().geteBase();
        String rTypeName = OntologyUtil.getRelationTypeNameById(context.getOntology(), rel.getrType());
        return traversal.outE(GlobalConstants.Labels.PROMISE)
                .as(createLabelForRelation(prev.get().getAsgEBase().geteBase(), next.get().getAsgEBase().geteBase()))
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                                __.has(T.label, P.eq(rTypeName)),
                                __.has(GlobalConstants.HasKeys.DIRECTION, ConverstionUtil.convertDirection(rel.getDir())))));
    }
    //endregion

    //region Private Methods
    private String createLabelForRelation(EEntityBase prev, EEntityBase next) {
        return prev.geteTag() + "-->" + next.geteTag();
    }
    //endregion
}
