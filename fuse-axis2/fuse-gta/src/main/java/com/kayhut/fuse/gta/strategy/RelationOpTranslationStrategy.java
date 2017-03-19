package com.kayhut.fuse.gta.strategy;

import com.google.common.base.Strings;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyUtil;
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
            Rel rel = ((RelationOp) planOpBase).getRelation().geteBase();
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
            relLabel.append(((EntityOp)prev.get()).getEntity().geteBase().geteTag());
        relLabel.append("-->");
        if (next.isPresent())
            relLabel.append(((EntityOp)next.get()).getEntity().geteBase().geteTag());

        return relLabel.toString();
    }

    private String getTinkerPopDirection(String dir) {
        String tinkerPopDirection = "";
        if (!Strings.isNullOrEmpty(dir)) {
            switch (dir.toLowerCase()) {
                case "r":
                    tinkerPopDirection = "out";
                    break;
                case "l":
                    tinkerPopDirection = "in";
                    break;
                default:
                    throw new IllegalArgumentException("Not Supported Relation Direction: " + dir);
            }
        }
        else
            throw new IllegalArgumentException("Empty Relation Direction");
        return tinkerPopDirection;
    }

    //region Fields
    private Ontology ontology;
    //endregion

}
