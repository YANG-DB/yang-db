package com.yangdb.fuse.asg.translator.cypher.strategies.expressions;

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

import com.bpodgursky.jbool_expressions.Expression;
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import org.opencypher.v9_0.expressions.In;
import org.opencypher.v9_0.expressions.ListLiteral;

import java.util.stream.Collectors;

import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.inSet;
import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class InExpression extends BaseEqualityExpression<In> {

    @Override
    protected In get(org.opencypher.v9_0.expressions.Expression expression) {
        return (In) expression;
    }

    protected Constraint constraint(String operator, org.opencypher.v9_0.expressions.Expression literal) {
        switch (operator) {
            case "IN": return of(inSet,asJavaCollectionConverter(literal.arguments()).asJavaCollection().stream().map(p->p.asCanonicalStringVal()).collect(Collectors.toList()));
        }
        throw new IllegalArgumentException("condition "+literal.asCanonicalStringVal()+" doesn't match any supported V1 constraints");
    }

    @Override
    protected org.opencypher.v9_0.expressions.Expression literal(org.opencypher.v9_0.expressions.Expression lhs, org.opencypher.v9_0.expressions.Expression rhs) {
        return ListLiteral.class.isAssignableFrom(lhs.getClass()) ? ((ListLiteral) lhs) :
                ListLiteral.class.isAssignableFrom(rhs.getClass()) ? ((ListLiteral) rhs) : null;
    }

    @Override
    public boolean isApply(Expression expression) {
        return ((expression instanceof com.bpodgursky.jbool_expressions.Variable) &&
                ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression() instanceof In);
    }
}
