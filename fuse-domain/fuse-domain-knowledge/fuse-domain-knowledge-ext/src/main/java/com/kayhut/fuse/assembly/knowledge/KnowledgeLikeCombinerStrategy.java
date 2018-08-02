package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.asg.strategy.AsgPredicateRoutingStrategy;
import com.kayhut.fuse.asg.strategy.RuleBoostProvider;
import com.kayhut.fuse.asg.strategy.schema.LikeAnyConstraintTransformationAsgStrategy;
import com.kayhut.fuse.asg.strategy.schema.LikeConstraintTransformationAsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Set;

public class KnowledgeLikeCombinerStrategy extends AsgPredicateRoutingStrategy<EPropGroup> {
    public static Set<String> applicableFieldNames = Stream.of("title", "nicknames").toJavaSet();

    //region Constructors
    public KnowledgeLikeCombinerStrategy(RuleBoostProvider boostProvider, OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        super(Arrays.asList(
                new Routing<>(KnowledgeLikeCombinerStrategy::ePropGroupContainsLikeField, new LikeConstraintTransformationAsgStrategy(ontologyProvider, schemaProviderFactory)),
                new Routing<>(KnowledgeLikeCombinerStrategy::ePropGroupContainsLikeFieldOnRankableFields, new KnowledgeRankingAsgStrategy(boostProvider, ontologyProvider, schemaProviderFactory)),
                new Routing<>(KnowledgeLikeCombinerStrategy::ePropGroupContainsLikeAnyField, new LikeAnyConstraintTransformationAsgStrategy(ontologyProvider, schemaProviderFactory))),
                (query) -> AsgQueryUtil.elements(query, EPropGroup.class));
    }
    //endregion

    //Routing Predicates
    private static boolean ePropGroupContainsLikeAnyField(EPropGroup ePropGroup) {
        //exclusive or for e.value with fieldId[title/nickname] and condition on stringValue
        if(ePropGroupContainsLikeFieldOnRankableFields(ePropGroup))
            return false;

        if (!Stream.ofAll(ePropGroup.getProps())
                .find(eProp -> eProp.getCon().getOp() == ConstraintOp.likeAny)
                .isEmpty()) {
            return true;
        }

        if (ePropGroup.getGroups().size() > 0) {
            return Stream.ofAll(ePropGroup.getGroups()).map(g -> ePropGroupContainsLikeAnyField(g)).reduce((a, b) -> a || b);
        }
        return false;
    }

    private static boolean ePropGroupContainsLikeFieldOnRankableFields(EPropGroup ePropGroup) {
        boolean fieldExists = !Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null && eProp.getpType().equals("fieldId"))
                .find(eProp -> applicableFieldNames.contains(eProp.getCon().getExpr().toString()))
                .isEmpty();

        if (fieldExists && !Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null && eProp.getpType().equals("stringValue"))
                .filter(eProp -> !eProp.getCon().getExpr().toString().isEmpty())
                .find(eProp -> eProp.getCon().getOp() == ConstraintOp.like || eProp.getCon().getOp() == ConstraintOp.eq)
                .isEmpty()) {
            return true;
        }

        if (ePropGroup.getGroups().size() > 0) {
            return Stream.ofAll(ePropGroup.getGroups()).map(g -> ePropGroupContainsLikeFieldOnRankableFields(g)).reduce((a, b) -> a || b);
        }

        return false;
    }

    private static boolean ePropGroupContainsLikeField(EPropGroup ePropGroup) {
        //exclusive or for e.value with fieldId[title/nickname] and condition on stringValue
        if(ePropGroupContainsLikeFieldOnRankableFields(ePropGroup))
            return false;

         if (!Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null)
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
