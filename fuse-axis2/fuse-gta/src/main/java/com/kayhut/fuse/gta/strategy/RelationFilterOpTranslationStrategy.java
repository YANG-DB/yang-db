package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.utils.ConverstionUtil;
import com.kayhut.fuse.gta.strategy.utils.TraversalUtil;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.PushdownRelProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Arrays;
import java.util.List;
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

        TraversalUtil.remove(traversal, TraversalUtil.lastConsecutiveSteps(traversal, HasStep.class));

        traversal = appendRelationAndPropertyGroup(
                traversal,
                relationOp.get().getAsgEBase().geteBase(),
                relationFilterOp.getAsgEBase().geteBase(),
                context.getOnt());

        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal appendRelationAndPropertyGroup(
            GraphTraversal traversal,
            Rel rel,
            RelPropGroup relPropGroup,
            Ontology.Accessor ont) {

        String relationTypeName = ont.$relation$(rel.getrType()).getName();
        List<Traversal> traversals = Stream.ofAll(relPropGroup.getProps())
                .map(relProp -> convertRelPropToTraversal(relProp, ont))
                .toJavaList();

        traversals.addAll(0, Arrays.asList(
                __.has(T.label, P.eq(relationTypeName)),
                __.has(GlobalConstants.HasKeys.DIRECTION, P.eq(ConverstionUtil.convertDirection(rel.getDir())))
        ));

        return traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                Constraint.by(__.and(Stream.ofAll(traversals).toJavaArray(Traversal.class))));
    }

    private Traversal convertRelPropToTraversal(RelProp relProp, Ontology.Accessor ont) {
        Optional<Property> property = ont.$property(relProp.getpType());
        return property.<Traversal>map(property1 ->
                __.has(relProp instanceof PushdownRelProp ?
                        ((PushdownRelProp)relProp).getPushdownPropName() :
                        property1.getName()
                , ConverstionUtil.convertConstraint(relProp.getCon())))
                .orElseGet(__::start);

    }
    //endregion
}
