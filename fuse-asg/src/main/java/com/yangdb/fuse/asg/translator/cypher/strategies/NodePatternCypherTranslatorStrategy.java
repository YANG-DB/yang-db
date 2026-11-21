package com.yangdb.fuse.asg.translator.cypher.strategies;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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


import com.yangdb.fuse.asg.translator.cypher.strategies.expressions.EqualityExpression;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import org.opencypher.v9_0.expressions.*;
import org.opencypher.v9_0.util.InputPosition;
import scala.Option;
import scala.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.maxEntityNum;
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

            if (!byTag.isPresent()) {
                //create label
                node = new AsgEBase<>(new EUntyped(current, name, new HashSet<>(collect), Collections.emptySet(), current + 1, 0));
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
                    addProps(query, context, name, variable, properties);
                }
            } else {
                //todo validate same label on existing node
                node = byTag.get();
                final Option<Expression> properties = ((NodePattern) element).properties();
                if (properties.nonEmpty()) {
                    addProps(query, context, name, variable, properties);
                }

            }
            //return scope to original node
            context.scope(node);
        }

    }

    /**
     * Each {@code {k: v}} entry of the pattern becomes {@code node.k = v}, translated exactly like the same equality in
     * a WHERE clause: one EProp per entry on the node. An unnamed node is addressed by its generated tag.
     */
    protected void addProps(AsgQuery query, CypherStrategyContext context, String name, Option<LogicalVariable> variable, Option<Expression> properties) {
        final LogicalVariable nodeVariable = variable.isEmpty() ? new Variable(name, InputPosition.NONE()) : variable.get();
        Collection<Tuple2<PropertyKeyName, Expression>> items = asJavaCollectionConverter(((MapExpression) properties.get()).items()).asJavaCollection();
        for (Tuple2<PropertyKeyName, Expression> item : items) {
            Property property = new Property(nodeVariable, item._1, InputPosition.NONE());
            Equals equals = new Equals(property, item._2, InputPosition.NONE());
            equalityExpression.apply(Optional.empty(),
                    com.bpodgursky.jbool_expressions.Variable.of(CypherUtils.Wrapper.of(equals)), query, context);
        }
    }

    private EqualityExpression equalityExpression;
}
