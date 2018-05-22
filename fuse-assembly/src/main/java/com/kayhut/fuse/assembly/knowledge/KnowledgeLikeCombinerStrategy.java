package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.asg.strategy.AsgElementStrategy;
import com.kayhut.fuse.asg.strategy.AsgPredicateRoutingStrategy;
import com.kayhut.fuse.asg.strategy.RuleBoostProvider;
import com.kayhut.fuse.asg.strategy.schema.LikeAnyConstraintTransformationAsgStrategy;
import com.kayhut.fuse.asg.strategy.schema.LikeConstraintTransformationAsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import javaslang.Tuple2;
import javaslang.collection.HashSet;
import javaslang.collection.List;
import javaslang.collection.Stream;

import java.util.function.Predicate;

public class KnowledgeLikeCombinerStrategy extends AsgPredicateRoutingStrategy<EPropGroup> {
    public static HashSet<String> fieldNames = HashSet.of("title").add("nicknames");

    public KnowledgeLikeCombinerStrategy(RuleBoostProvider boostProvider, OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        super(List.of(
                new Tuple2<Predicate<EPropGroup>, AsgElementStrategy<EPropGroup>>(KnowledgeLikeCombinerStrategy::ePropGroupContainsLikeField, new LikeConstraintTransformationAsgStrategy(ontologyProvider, schemaProviderFactory)),
                new Tuple2<Predicate<EPropGroup>, AsgElementStrategy<EPropGroup>>(KnowledgeLikeCombinerStrategy::ePropGroupContainsLikeFieldOnSpecialFields, new KnowledgeRankingAsgStrategy(boostProvider, ontologyProvider, schemaProviderFactory)),
                new Tuple2<Predicate<EPropGroup>, AsgElementStrategy<EPropGroup>>(KnowledgeLikeCombinerStrategy::ePropGroupContainsLikeAnyField, new LikeAnyConstraintTransformationAsgStrategy(ontologyProvider, schemaProviderFactory))
                ).toJavaList(),
                (query) -> com.kayhut.fuse.dispatcher.utils.AsgQueryUtil.elements(query, EPropGroup.class));
    }


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
                .find(eProp -> eProp.getCon().getOp() == ConstraintOp.like || eProp.getCon().getOp() == ConstraintOp.eq)
                .isEmpty()) {
            return true;
        }

        if (ePropGroup.getGroups().size() > 0) {
            return Stream.ofAll(ePropGroup.getGroups()).map(g -> ePropGroupContainsLikeField(g)).reduce((a, b) -> a || b);
        }

        return false;
    }

}
