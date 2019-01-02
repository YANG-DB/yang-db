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

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import org.opencypher.v9_0.expressions.*;
import scala.Option;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class StepPatternCypherTranslatorStrategy implements CypherElementTranslatorStrategy<PatternElement> {


    public StepPatternCypherTranslatorStrategy(NodePatternCypherTranslatorStrategy nodePattern) {
        this.nodePattern = nodePattern;
    }

    @Override
    public void apply(PatternElement element, AsgQuery query, CypherStrategyContext context) {
        if (element instanceof RelationshipChain) {
            final PatternElement left = ((RelationshipChain) element).element();
            if(left instanceof RelationshipChain) apply(left,query,context);
            if(left instanceof NodePattern) nodePattern.apply(left,query,context);

            final RelationshipPattern relationship = ((RelationshipChain) element).relationship();
            apply(relationship,query,context);
            final NodePattern right = ((RelationshipChain) element).rightNode();
            this.nodePattern.apply(right,query,context);
        }
    }


    public void apply(RelationshipPattern element, AsgQuery query, CypherStrategyContext context) {
        final Option<LogicalVariable> variable = element.variable();

        int current = CypherUtils.maxEntityNum(query)+1;
        String name = "Rel_#" + current;

        if (!variable.isEmpty()) {
            //get scope and calculate next enum
            final LogicalVariable logicalVariable = variable.get();
            name = logicalVariable.name();
        }

        if (!element.types().isEmpty()) {
            //todo
        }
        final SemanticDirection direction = element.direction();

        //build label and update query, mutate new current scope
        AsgEBase<EBase> quant = CypherUtils.quant(context.getScope(), Optional.empty(), query, context);
        context.scope(quant);

        //labels
        Collection<RelTypeName> labels = asJavaCollectionConverter((element).types()).asJavaCollection();
        final List<String> rTypes = labels.stream().map(l -> l.name()).collect(Collectors.toList());
        AsgEBase<Rel> rel = new AsgEBase<>(new Rel(current, null, resolve(direction), name, current + 1, 0));
        if (!rTypes.isEmpty()) {
            //todo add solution for multi-type labels
            rel = new AsgEBase<>(new Rel(current, rTypes.get(0), resolve(direction), name, current + 1, 0));
        }

        context.getScope().addNext(rel);
        context.scope(rel);
    }

    private Rel.Direction resolve(SemanticDirection direction) {
        if (direction instanceof SemanticDirection.INCOMING$)
            return Rel.Direction.L;
        if (direction instanceof SemanticDirection.OUTGOING$)
            return Rel.Direction.R;
        return Rel.Direction.RL;
    }

    private NodePatternCypherTranslatorStrategy nodePattern;

}
