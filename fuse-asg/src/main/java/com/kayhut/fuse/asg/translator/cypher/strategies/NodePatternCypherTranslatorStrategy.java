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

import com.kayhut.fuse.asg.translator.cypher.strategies.expressions.EqualityExpression;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import org.opencypher.v9_0.expressions.*;
import org.opencypher.v9_0.util.InputPosition;
import scala.Option;
import scala.Tuple2;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.maxEntityNum;
import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class NodePatternCypherTranslatorStrategy implements CypherElementTranslatorStrategy<PatternElement> {

    public NodePatternCypherTranslatorStrategy(EqualityExpression equalityExpression) {
        this.equalityExpression = equalityExpression;
    }

    @Override
    public void apply(PatternElement element, AsgQuery query, CypherStrategyContext context) {
        if (element instanceof NodePattern) {
            final Option<LogicalVariable> variable = element.variable();

            int current = maxEntityNum(query) + 1;
            String name = "Node_#" + current;

            if (!variable.isEmpty()) {
                //get scope and calculate next enum
                final LogicalVariable logicalVariable = variable.get();
                name = logicalVariable.name();
            }

            //build label and update query, mutate new current scope
            final Collection<LabelName> labels = asJavaCollectionConverter(((NodePattern) element).labels()).asJavaCollection();
            //labels
            final List<String> collect = labels.stream().map(l -> l.name()).collect(Collectors.toList());

            Optional<AsgEBase<EBase>> byTag = AsgQueryUtil.getByTag(query.getStart(), name);

            //if no node is present - create one
            AsgEBase<? extends EBase> node;

            if(!byTag.isPresent()) {
                //create label
                node = new AsgEBase<>(new EUntyped(current, name, collect, Collections.emptyList(), current + 1, 0));
                //is single label - use EType label (specific typed label)
                if (labels.size() == 1) {
                    node = new AsgEBase<>(new ETyped(current, name, labels.iterator().next().name(), current + 1, 0));
                }

                //build label and update query, mutate new current scope
/*
                AsgEBase<EBase> quant = CypherUtils.quant(context.getScope(), Optional.empty(), query, context);
                context.scope(quant);
*/

                context.getScope().addNext(node);
                context.scope(node);

                final Option<Expression> properties = ((NodePattern) element).properties();
                if (properties.nonEmpty()) {
                    addProps(query, context, variable, properties);
                }
            } else {
                //todo validate same label on existing node
                node = byTag.get();
                final Option<Expression> properties = ((NodePattern) element).properties();
                if (properties.nonEmpty()) {
                    addProps(query, context, variable, properties);
                }

            }
            //return scope to original node
            context.scope(node);
        }

    }

    protected void addProps(AsgQuery query, CypherStrategyContext context, Option<LogicalVariable> variable, Option<Expression> properties) {
        Collection<Tuple2<PropertyKeyName, Expression>> collection = asJavaCollectionConverter(((MapExpression) properties.get()).items()).asJavaCollection();
        Property property = new Property(variable.get(), collection.iterator().next()._1, InputPosition.NONE());
        Equals equals = new Equals(property, collection.iterator().next()._2, InputPosition.NONE());
        CypherUtils.Wrapper wrapper = CypherUtils.Wrapper.of(equals);
        equalityExpression.apply(Optional.empty(), com.bpodgursky.jbool_expressions.Variable.of(wrapper), query, context);
    }

    private EqualityExpression equalityExpression;
}
