package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.common.EntityTranslationOptions;
import com.kayhut.fuse.gta.strategy.utils.ConversionUtil;
import com.kayhut.fuse.gta.strategy.utils.TraversalUtil;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by Roman on 09/05/2017.
 */
public class EntityFilterOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public EntityFilterOpTranslationStrategy(EntityTranslationOptions options) {
        super(EntityFilterOp.class);
        this.options = options;
    }
    //endregion
    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, Plan plan, PlanOpBase planOp, TranslationContext context) {
        EntityFilterOp entityFilterOp = (EntityFilterOp) planOp;

        Optional<PlanOpBase> previousPlanOp = PlanUtil.adjacentPrev(plan, planOp);
        if (!previousPlanOp.isPresent()) {
            return traversal;
        }

        EntityOp entityOp = (EntityOp) previousPlanOp.get();
        traversal = appendPropertyGroup(
                traversal,
                entityOp.getAsgEBase().geteBase(),
                entityFilterOp.getAsgEBase().geteBase(),
                context.getOnt());

        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal appendPropertyGroup(
            GraphTraversal<?, ?> traversal,
            EEntityBase entity,
            EPropGroup ePropGroup,
            Ontology.Accessor ont) {

        Stream.ofAll(TraversalUtil.<Step>lastSteps(traversal, step -> step.getLabels().contains(entity.geteTag())))
                .forEach(step -> step.removeLabel(entity.geteTag()));

        traversal.as(entity.geteTag());
        for(EProp eProp : ePropGroup.getProps()) {
            Optional<Property> property = ont.$property(eProp.getpType());
            property.ifPresent(property1 -> traversal.has(property1.getName(), ConversionUtil.convertConstraint(eProp.getCon())));
        }

        return traversal;
    }
    //endregion

    //region Fields
    private EntityTranslationOptions options;
    //endregion
}
