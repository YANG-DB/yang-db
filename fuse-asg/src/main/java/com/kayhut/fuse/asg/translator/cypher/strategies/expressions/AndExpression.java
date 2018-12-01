package com.kayhut.fuse.asg.translator.cypher.strategies.expressions;

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

import com.kayhut.fuse.asg.translator.cypher.strategies.CypherStrategyContext;
import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.query.Query;
import org.opencypher.v9_0.expressions.Expression;
import org.opencypher.v9_0.expressions.HasLabels;
import org.opencypher.v9_0.expressions.LabelName;
import org.opencypher.v9_0.expressions.Variable;

import java.util.Collection;

import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class AndExpression implements ExpressionStrategies {

    @Override
    public void apply(Expression expression, Query query, CypherStrategyContext context) {
        if(expression instanceof HasLabels) {
            HasLabels hasLabels = ((HasLabels) expression);
            Collection<LabelName> labelNames = asJavaCollectionConverter(hasLabels.labels()).asJavaCollection();
            Variable variable = (Variable) hasLabels.expression();

            Next<Integer> scope = context.getScope();
        }
    }

}
