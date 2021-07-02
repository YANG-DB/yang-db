package com.yangdb.fuse.asg.translator.cypher.strategies.expressions;

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



import com.bpodgursky.jbool_expressions.Expression;
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.FunctionEProp;
import com.yangdb.fuse.model.query.properties.FunctionRelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.CountConstraintOp;
import org.opencypher.v9_0.expressions.FunctionInvocation;
import org.opencypher.v9_0.expressions.Variable;

import java.util.Optional;

import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.*;

public class CountExpression extends BaseEqualityExpression<org.opencypher.v9_0.expressions.InequalityExpression> {

    public static final String COUNT = "count";

    @Override
    protected org.opencypher.v9_0.expressions.InequalityExpression get(org.opencypher.v9_0.expressions.Expression expression) {
        return (org.opencypher.v9_0.expressions.InequalityExpression) expression;
    }

    @Override
    protected Optional<String> getKeyName(org.opencypher.v9_0.expressions.Expression prop) {
        return Optional.of(((FunctionInvocation)prop.findAggregate().get()).name());
    }

    @Override
    protected String getTagName(org.opencypher.v9_0.expressions.Expression prop) {
        return ((Variable)prop.inputs().seq().last()._1).name();
    }

    protected Constraint constraint(String operator, org.opencypher.v9_0.expressions.Expression literal) {
        switch (operator) {
            case "<": return of(CountConstraintOp.lt,literal.asCanonicalStringVal());
            case "<=":return of(CountConstraintOp.le,literal.asCanonicalStringVal());
            case ">": return of(CountConstraintOp.gt,literal.asCanonicalStringVal());
            case ">=":return of(CountConstraintOp.ge,literal.asCanonicalStringVal());
        }
        throw new IllegalArgumentException("condition "+literal.asCanonicalStringVal()+" doesn't match any supported V1 constraints");
    }

    @Override
    protected FunctionEProp addPredicate(int current, String propery, Constraint constraint) {
        return new FunctionEProp(current,propery,constraint);
    }
    @Override
    protected FunctionRelProp addRelPredicate(int current, String propery, Constraint constraint) {
        return new FunctionRelProp(current,propery,constraint);
    }

    @Override
    public boolean isApply(Expression expression) {
        return (expression instanceof com.bpodgursky.jbool_expressions.Variable) &&
                (((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression() instanceof org.opencypher.v9_0.expressions.InequalityExpression) &&
                !(((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression()).findAggregate().isEmpty() &&
                ((org.opencypher.v9_0.expressions.FunctionInvocation)(((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression()).findAggregate().get()).name().equals(COUNT);
    }
}
