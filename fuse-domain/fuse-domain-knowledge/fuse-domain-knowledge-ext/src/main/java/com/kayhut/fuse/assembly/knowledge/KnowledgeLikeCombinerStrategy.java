package com.kayhut.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.kayhut.fuse.asg.strategy.AsgPredicateRoutingStrategy;
import com.kayhut.fuse.asg.strategy.RuleBoostProvider;
import com.kayhut.fuse.asg.strategy.schema.LikeAnyConstraintTransformationAsgStrategy;
import com.kayhut.fuse.asg.strategy.schema.LikeConstraintTransformationAsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.EUntyped;
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
    private static boolean ePropGroupContainsLikeAnyField(AsgEBase<EPropGroup> asgEBase) {
        return ePropGroupContainsLikeAnyField(asgEBase.geteBase());
    }

    private static boolean ePropGroupContainsLikeAnyField(EPropGroup ePropGroup) {
        //exclusive or for e.value with fieldId[title/nickname] and condition on stringValue
        if (ePropGroupContainsLikeFieldOnRankableFields(ePropGroup))
            return false;

        if (!Stream.ofAll(ePropGroup.getProps())
                .filter(prop -> prop.getCon() != null)
                .find(eProp -> eProp.getCon().getOp() == ConstraintOp.likeAny)
                .isEmpty()) {
            return true;
        }

        if (ePropGroup.getGroups().size() > 0) {
            return Stream.ofAll(ePropGroup.getGroups()).map(g -> ePropGroupContainsLikeAnyField(g)).reduce((a, b) -> a || b);
        }
        return false;
    }

    private static boolean ePropGroupContainsLikeFieldOnRankableFields(AsgEBase<EPropGroup> asgEBase) {
        boolean isSearchableKnowledgeEntity = AsgQueryUtil.<EPropGroup, EEntityBase>ancestor(asgEBase, EEntityBase.class)
                .orElseGet(() -> new AsgEBase<>(new EUntyped(asgEBase.geteNum(), "", 0, 0)))
                .geteBase().geteTag().endsWith("globalEntityValue");
        if (!isSearchableKnowledgeEntity) {
            return false;
        }

        return ePropGroupContainsLikeFieldOnRankableFields(asgEBase.geteBase());
    }

    private static boolean ePropGroupContainsLikeFieldOnRankableFields(EPropGroup ePropGroup) {
        boolean fieldExists = !Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null && eProp.getpType().equals("fieldId"))
                .filter(eProp -> eProp.getCon().getExpr() != null )
                .find(eProp -> applicableFieldNames.contains(eProp.getCon().getExpr().toString()))
                .isEmpty();

        if (fieldExists && !Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null && eProp.getpType().equals("stringValue"))
                .filter(eProp -> eProp.getCon().getExpr() != null )
                .filter(eProp -> !eProp.getCon().getExpr().toString().isEmpty())
                .find(eProp -> eProp.getCon().getOp() == ConstraintOp.like || eProp.getCon().getOp() == ConstraintOp.eq)
                .isEmpty()) {
            return true;
        }

        if (ePropGroup.getGroups().size() > 0) {
            return Stream.ofAll(ePropGroup.getGroups())
                    .map(KnowledgeLikeCombinerStrategy::ePropGroupContainsLikeFieldOnRankableFields)
                    .reduce((a, b) -> a || b);
        }

        return false;
    }

    private static boolean ePropGroupContainsLikeField(AsgEBase<EPropGroup> asgEBase) {
        //exclusive or for e.value with fieldId[title/nickname] and condition on stringValue
        if (ePropGroupContainsLikeFieldOnRankableFields(asgEBase)) {
            return false;
        }

        return ePropGroupContainsLikeField(asgEBase.geteBase());
    }

    private static boolean ePropGroupContainsLikeField(EPropGroup ePropGroup) {
        if (!Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null)
                .filter(eProp -> !(eProp.getCon().getExpr() == null) && !eProp.getCon().getExpr().toString().isEmpty())
                .find(eProp -> eProp.getCon().getOp() == ConstraintOp.like)
                .isEmpty()) {
            return true;
        }

        if (ePropGroup.getGroups().size() > 0) {
            return Stream.ofAll(ePropGroup.getGroups())
                    .map(KnowledgeLikeCombinerStrategy::ePropGroupContainsLikeField)
                    .reduce((a, b) -> a || b);
        }

        return false;
    }
    //endregion
}
