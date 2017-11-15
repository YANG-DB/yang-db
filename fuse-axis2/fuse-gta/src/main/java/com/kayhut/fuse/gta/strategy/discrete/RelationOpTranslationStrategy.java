package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.utils.ConversionUtil;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;

public class RelationOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public RelationOpTranslationStrategy() {
        super(RelationOp.class);
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOpBase planOp, TranslationContext context) {
        Optional<EntityOp> prev = PlanUtil.prev(plan.getPlan(), planOp, EntityOp.class);
        Optional<EntityOp> next = PlanUtil.next(plan.getPlan(), planOp, EntityOp.class);

        Rel rel = ((RelationOp)planOp).getAsgEBase().geteBase();
        String rTypeName = context.getOnt().$relation$(rel.getrType()).getName();

        switch (rel.getDir()) {
            case R: traversal.outE(rTypeName);
                break;
            case L: traversal.inE(rTypeName);
                break;
            case RL: traversal.bothE(rTypeName);
                break;
        }

        return traversal.as(createLabelForRelation(prev.get().getAsgEBase().geteBase(), rel.getDir(), next.get().getAsgEBase().geteBase()));

    }
    //endregion

    //region Private Methods
    private String createLabelForRelation(EEntityBase prev, Rel.Direction direction, EEntityBase next) {
        return prev.geteTag() + ConversionUtil.convertDirectionGraphic(direction) + next.geteTag();
    }
    //endregion
}
