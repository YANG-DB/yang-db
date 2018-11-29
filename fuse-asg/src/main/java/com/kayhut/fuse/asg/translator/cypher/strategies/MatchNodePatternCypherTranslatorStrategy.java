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
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import org.opencypher.v9_0.expressions.LabelName;
import org.opencypher.v9_0.expressions.LogicalVariable;
import org.opencypher.v9_0.expressions.NodePattern;
import org.opencypher.v9_0.expressions.PatternElement;
import scala.Option;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class MatchNodePatternCypherTranslatorStrategy extends MatchCypherTranslatorStrategy {

    @Override
    void applyPattern(PatternElement element, CypherStrategyContext context, Query query) {
        if(element instanceof NodePattern) {
            final Option<LogicalVariable> variable = element.variable();
            final int current = context.getScope().getNext();
            String name = "Node_#"+ current;

            if (!variable.isEmpty()) {
                //get scope and calculate next enum
                final LogicalVariable logicalVariable = variable.get();
                name = logicalVariable.name();
            }

            //build node and update query, mutate new current scope
            final Collection<LabelName> labels = asJavaCollectionConverter(((NodePattern) element).labels()).asJavaCollection();
            //labels
            final List<String> collect = labels.stream().map(l -> l.name()).collect(Collectors.toList());
            EEntityBase node = new EUntyped(current, name,collect, Collections.emptyList(), current + 1, 0);
            //is single label - use EType node (specific typed node)
            if(labels.size() == 1) {
                node = new ETyped(current,name,labels.iterator().next().name(),current+1,0);
            }
            query.getElements().add(node);
            context.scope(node);
        }
    }
}
