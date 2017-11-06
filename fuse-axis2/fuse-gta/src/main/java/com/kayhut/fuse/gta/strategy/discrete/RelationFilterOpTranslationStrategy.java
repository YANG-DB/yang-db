package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.utils.ConversionUtil;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RedundantRelProp;
import com.kayhut.fuse.model.query.properties.RedundantSelectionRelProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.unipop.predicates.SelectP;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by Roman on 09/05/2017.
 */
public class RelationFilterOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public RelationFilterOpTranslationStrategy() {
        super(RelationFilterOp.class);
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, Plan plan, PlanOpBase planOp, TranslationContext context) {
        RelationFilterOp relationFilterOp = (RelationFilterOp)planOp;
        Optional<RelationOp> relationOp = PlanUtil.adjacentPrev(plan, relationFilterOp);
        if (!relationOp.isPresent()) {
            return traversal;
        }

        traversal = appendRelationAndPropertyGroup(
                traversal,
                relationFilterOp.getAsgEBase().geteBase(),
                context.getOnt());

        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal appendRelationAndPropertyGroup(
            GraphTraversal traversal,
            RelPropGroup relPropGroup,
            Ontology.Accessor ont) {

        for(RelProp relProp : relPropGroup.getProps()) {
            Optional<Property> property = ont.$property(relProp.getpType());
            if (property.isPresent()) {
                if (relProp.getClass().equals(RelProp.class)) {
                    traversal.has(property.get().getName(), ConversionUtil.convertConstraint(relProp.getCon()));
                } else if (relProp.getClass().equals(RedundantRelProp.class)) {
                    traversal.has(((RedundantRelProp)relProp).getRedundantPropName(), ConversionUtil.convertConstraint(relProp.getCon()));
                } else if (relProp.getClass().equals(RedundantSelectionRelProp.class)) {
                    traversal.has(((RedundantSelectionRelProp)relProp).getRedundantPropName(),
                            SelectP.raw(((RedundantSelectionRelProp)relProp).getRedundantPropName()));
                }
            }
        }

        return traversal;
    }
    //endregion
}
