package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;

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

    public RelationOpTranslationStrategy() {
    }

    @Override
    public GraphTraversal apply(TranslationStrategyContext context, GraphTraversal traversal) {
        Plan plan = context.getPlan();
        PlanOpBase planOpBase = context.getPlanOpBase();
        Ontology ontology = context.getOntology();

        PlanUtil planUtil = new PlanUtil();
        Optional<PlanOpBase> prev = planUtil.getPrev(plan.getOps(),planOpBase);
        Optional<PlanOpBase> next = planUtil.getNext(plan.getOps(),planOpBase);

        if(planOpBase instanceof RelationOp) {
            Rel rel = ((RelationOp) planOpBase).getAsgEBase().geteBase();
            String rTypeName = OntologyUtil.getRelationTypeNameById(ontology, rel.getrType());
            traversal.outE("promise").has("constraint", P.eq(Constraint.by(__.and(
                   __.has("label",P.eq(rTypeName)),
                   __.has("direction", P.eq(getTinkerPopDirection(rel.getDir())))
            )))).as(createLabelForRelation(prev, next));
        }
        return traversal;
    }

    private String createLabelForRelation(Optional<PlanOpBase> prev, Optional<PlanOpBase> next) {
        StringBuilder relLabel = new StringBuilder();

        if (prev.isPresent())
            relLabel.append(((EntityOp)prev.get()).getAsgEBase().geteBase().geteTag());
        relLabel.append("-->");
        if (next.isPresent())
            relLabel.append(((EntityOp)next.get()).getAsgEBase().geteBase().geteTag());

        return relLabel.toString();
    }

    private Direction getTinkerPopDirection(Rel.Direction dir) {
        switch (dir) {
            case R:
                return Direction.OUT;
            case L:
                return Direction.IN;
            default:
                throw new IllegalArgumentException("Not Supported Relation Direction: " + dir);
        }
    }

    //region Fields
    private Ontology ontology;
    //endregion

}
