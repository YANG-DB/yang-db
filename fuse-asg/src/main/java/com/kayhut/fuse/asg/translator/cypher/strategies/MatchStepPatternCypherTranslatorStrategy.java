package com.kayhut.fuse.asg.translator.cypher.strategies;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2018 The Fuse Graph Database Project
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

import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import org.opencypher.v9_0.expressions.*;
import scala.Option;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class MatchStepPatternCypherTranslatorStrategy extends MatchCypherTranslatorStrategy {


    public MatchStepPatternCypherTranslatorStrategy(MatchNodePatternCypherTranslatorStrategy nodePattern) {
        this.nodePattern = nodePattern;
    }

    @Override
    void applyPattern(PatternElement element, CypherStrategyContext context, Query query) {
        if(element instanceof RelationshipChain) {
            final PatternElement left = ((RelationshipChain) element).element();
            nodePattern.applyPattern(left,context,query);
            final RelationshipPattern relationship = ((RelationshipChain) element).relationship();
            applyPattern(relationship,context,query);
            final NodePattern right = ((RelationshipChain) element).rightNode();
            this.nodePattern.applyPattern(right,context,query);
        }
    }


    void applyPattern(RelationshipPattern element, CypherStrategyContext context, Query query) {
        final Option<LogicalVariable> variable = element.variable();
        final int current = context.getScope().getNext();
        String name = "Rel_#" + current;

        if (!variable.isEmpty()) {
            //get scope and calculate next enum
            final LogicalVariable logicalVariable = variable.get();
            name = logicalVariable.name();
        }

        final SemanticDirection direction = element.direction();
        //build node and update query, mutate new current scope
        //labels
//        final Collection<LabelName> labels = asJavaCollectionConverter(((NodePattern) element).labels()).asJavaCollection();
//        final List<String> collect = labels.stream().map(l -> l.name()).collect(Collectors.toList());
        Rel rel = new Rel(current, null, resolve(direction), name, current + 1, 0);
//        if(!collect.isEmpty()) {
//            rel = new Rel(current, null, resolve(direction), name, current + 1, 0);
//        }
        query.getElements().add(rel);
        context.scope(rel);
    }

    private Rel.Direction resolve(SemanticDirection direction) {
        if(direction instanceof SemanticDirection.INCOMING$)
            return Rel.Direction.R;
        if(direction instanceof SemanticDirection.OUTGOING$)
            return Rel.Direction.L;
        return Rel.Direction.RL;
    }

    private MatchNodePatternCypherTranslatorStrategy nodePattern;

}
