package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.asg.strategy.AsgPredicateRoutingStrategy;
import com.kayhut.fuse.asg.strategy.RuleBoostProvider;
import com.kayhut.fuse.asg.strategy.schema.LikeConstraintTransformationAsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import javaslang.collection.HashSet;
import javaslang.collection.Stream;

public class KnowledgeLikeCombinerStrategy extends AsgPredicateRoutingStrategy<EPropGroup> {
    public static HashSet<String> fieldNames = HashSet.of("title").add("nicknames");

    public KnowledgeLikeCombinerStrategy(RuleBoostProvider boostProvider, OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        super(ePropGroup -> !Stream.ofAll(ePropGroup.getProps()).find(eProp -> fieldNames.contains(eProp.getCon().getExpr().toString())).isEmpty()
                , query -> com.kayhut.fuse.dispatcher.utils.AsgQueryUtil.elements(query, EPropGroup.class)
                , new KnowledgeRankingAsgStrategy(boostProvider, ontologyProvider, schemaProviderFactory),
                new LikeConstraintTransformationAsgStrategy(ontologyProvider, schemaProviderFactory));
    }
}
