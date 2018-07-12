package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.asg.strategy.AsgElementStrategy;
import com.kayhut.fuse.asg.strategy.AsgPredicateRoutingStrategy;
import com.kayhut.fuse.asg.strategy.RuleBoostProvider;
import com.kayhut.fuse.asg.strategy.schema.LikeAnyConstraintTransformationAsgStrategy;
import com.kayhut.fuse.asg.strategy.schema.LikeConstraintTransformationAsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import javaslang.Tuple2;
import javaslang.collection.HashSet;
import javaslang.collection.List;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

public class KnowledgeLikeCombinerStrategy extends AsgPredicateRoutingStrategy<EPropGroup> {
    public static Set<String> fieldNames = Stream.of("title", "nicknames").toJavaSet();

    //region Constructors
    public KnowledgeLikeCombinerStrategy(RuleBoostProvider boostProvider, OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        super(Arrays.asList(
                new Routing<>(KnowledgeLikeCombinerStrategy::ePropGroupContainsLikeField, new LikeConstraintTransformationAsgStrategy(ontologyProvider, schemaProviderFactory)),
                new Routing<>(KnowledgeLikeCombinerStrategy::ePropGroupContainsLikeFieldOnSpecialFields, new KnowledgeRankingAsgStrategy(boostProvider, ontologyProvider, schemaProviderFactory)),
                new Routing<>(KnowledgeLikeCombinerStrategy::ePropGroupContainsLikeAnyField, new LikeAnyConstraintTransformationAsgStrategy(ontologyProvider, schemaProviderFactory))),
                (query) -> AsgQueryUtil.elements(query, EPropGroup.class));
    }
    //endregion

    //Routing Predicates
    private static boolean ePropGroupContainsLikeAnyField(EPropGroup ePropGroup) {

        if( !Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null && eProp.getpType().equals("stringValue"))
                .find(eProp -> eProp.getCon().getOp() == ConstraintOp.likeAny)
                .isEmpty()) {
            return true;
        }

        if (ePropGroup.getGroups().size() > 0) {
            return Stream.ofAll(ePropGroup.getGroups()).map(g -> ePropGroupContainsLikeAnyField(g)).reduce((a, b) -> a || b);
        }
        return false;
    }

    private static boolean ePropGroupContainsLikeFieldOnSpecialFields(EPropGroup ePropGroup) {
        boolean fieldExists = !Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null && eProp.getpType().equals("fieldId"))
                .find(eProp -> fieldNames.contains(eProp.getCon().getExpr().toString()))
                .isEmpty();

        if (fieldExists && !Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null && eProp.getpType().equals("stringValue"))
                .filter(eProp -> !eProp.getCon().getExpr().toString().isEmpty())
                .find(eProp -> eProp.getCon().getOp() == ConstraintOp.like || eProp.getCon().getOp() == ConstraintOp.eq)
                .isEmpty()) {
            return true;
        }

        if (ePropGroup.getGroups().size() > 0) {
            return Stream.ofAll(ePropGroup.getGroups()).map(g -> ePropGroupContainsLikeFieldOnSpecialFields(g)).reduce((a, b) -> a || b);
        }

        return false;
    }

    private static boolean ePropGroupContainsLikeField(EPropGroup ePropGroup) {
        boolean fieldExists = !Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null && eProp.getpType().equals("fieldId"))
                .find(eProp -> fieldNames.contains(eProp.getCon().getExpr().toString()))
                .isEmpty();

        if (!fieldExists &&!Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null && eProp.getpType().equals("stringValue"))
                .filter(eProp -> !eProp.getCon().getExpr().toString().isEmpty())
                .find(eProp -> eProp.getCon().getOp() == ConstraintOp.like || eProp.getCon().getOp() == ConstraintOp.eq)
                .isEmpty()) {
            return true;
        }

        if (ePropGroup.getGroups().size() > 0) {
            return Stream.ofAll(ePropGroup.getGroups()).map(g -> ePropGroupContainsLikeField(g)).reduce((a, b) -> a || b);
        }

        return false;
    }
    //endregion
}
