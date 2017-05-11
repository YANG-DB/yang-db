package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.strategy.utils.ConverstionUtil;
import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
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
public class RelationFilterOpTranslationStrategy implements TranslationStrategy {
    //region TranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanOpBase planOp, TranslationStrategyContext context) {
        if (!(planOp instanceof RelationFilterOp)) {
            return traversal;
        }

        RelationFilterOp relationFilterOp = (RelationFilterOp)planOp;
        Optional<RelationOp> relationOp = PlanUtil.getAdjacentPrev(context.getPlan(), relationFilterOp);
        if (!relationOp.isPresent()) {
            return traversal;
        }

        if (HasStep.class.isAssignableFrom(traversal.asAdmin().getEndStep().getClass())) {
            traversal.asAdmin().removeStep(traversal.asAdmin().getSteps().indexOf(traversal.asAdmin().getEndStep()));
        }

        traversal = appendRelationAndPropertyGroup(
                traversal,
                relationOp.get().getAsgEBase().geteBase(),
                relationFilterOp.getAsgEBase().geteBase(),
                context.getOntology());

        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal appendRelationAndPropertyGroup(
            GraphTraversal traversal,
            Rel rel,
            RelPropGroup relPropGroup,
            Ontology ontology) {

        String relationTypeName = OntologyUtil.getRelationTypeNameById(ontology, rel.getrType());
        List<Traversal> traversals = Stream.ofAll(relPropGroup.getrProps())
                .map(relProp -> convertRelPropToTraversal(relProp, ontology))
                .toJavaList();

        traversals.addAll(0, Arrays.asList(
                __.has(T.label, P.eq(relationTypeName)),
                __.has(GlobalConstants.HasKeys.DIRECTION, P.eq(ConverstionUtil.convertDirection(rel.getDir())))
        ));

        return traversal.has(GlobalConstants.HasKeys.CONSTRAINT,
                Constraint.by(__.and(Stream.ofAll(traversals).toJavaArray(Traversal.class))));
    }

    private Traversal convertRelPropToTraversal(RelProp relProp, Ontology ontology) {
        Optional<Property> property = OntologyUtil.getProperty(ontology, Integer.parseInt(relProp.getpType()));
        return property.<Traversal>map(property1 -> __.has(property1.getName(), ConverstionUtil.convertConstraint(relProp.getCon())))
                .orElseGet(__::start);

    }
    //endregion
}
