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

import com.bpodgursky.jbool_expressions.Expression;
import com.kayhut.fuse.asg.translator.cypher.strategies.CypherStrategyContext;
import com.kayhut.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import org.opencypher.v9_0.expressions.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.*;
import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class InequalityExpression extends BaseExpressionStrategy {

    @Override
    public void apply(Optional<com.bpodgursky.jbool_expressions.Expression> parent, com.bpodgursky.jbool_expressions.Expression expression, AsgQuery query, CypherStrategyContext context) {
        org.opencypher.v9_0.expressions.InequalityExpression inequality = ((org.opencypher.v9_0.expressions.InequalityExpression) ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression());
        org.opencypher.v9_0.expressions.Expression lhs = inequality.lhs();
        org.opencypher.v9_0.expressions.Expression rhs = inequality.rhs();

        Property property = Property.class.isAssignableFrom(lhs.getClass()) ? ((Property) lhs) :
                Property.class.isAssignableFrom(rhs.getClass()) ? ((Property) rhs) : null;

        Literal literal = Literal.class.isAssignableFrom(lhs.getClass()) ? ((Literal) lhs) :
                Literal.class.isAssignableFrom(rhs.getClass()) ? ((Literal) rhs) : null;

        if (CypherUtils.var(property).isEmpty()) return;

        Variable variable = CypherUtils.var(property).get(0);
        //first find the node element by its var name in the query
        Optional<AsgEBase<EBase>> byTag = AsgQueryUtil.getByTag(context.getScope(), variable.name());
        if (!byTag.isPresent()) return;

        //update the scope
        context.scope(byTag.get());
        //change scope to quant
        final AsgEBase<EBase> quantAsg = CypherUtils.quant(byTag.get(), parent, query, context);
        //add the label eProp constraint
        final int current = Math.max(quantAsg.getNext().stream().mapToInt(p -> p.geteNum()).max().orElse(0), quantAsg.geteNum());

        if (!AsgQueryUtil.nextAdjacentDescendant(quantAsg, EPropGroup.class).isPresent()) {
            quantAsg.addNext(new AsgEBase<>(new EPropGroup(current + 1, CypherUtils.type(parent, Collections.EMPTY_SET))));
        }

        ((EPropGroup) AsgQueryUtil.nextAdjacentDescendant(quantAsg, EPropGroup.class).get().geteBase())
                .getProps().add(addPredicate(current, property.propertyKey().name(), constraint(inequality.canonicalOperatorSymbol(),literal)));
    }

    private Constraint constraint(String operator, Literal literal) {
        switch (operator) {
            case "<": return of(lt,literal.asCanonicalStringVal());
            case "<=":return of(le,literal.asCanonicalStringVal());
            case ">": return of(gt,literal.asCanonicalStringVal());
            case ">=":return of(ge,literal.asCanonicalStringVal());
        }
        throw new IllegalArgumentException("condition "+literal.asCanonicalStringVal()+" doesn't match any supported V1 constraints");
    }


    @Override
    public boolean isApply(Expression expression) {
        return (expression instanceof com.bpodgursky.jbool_expressions.Variable) &&
                (((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression() instanceof org.opencypher.v9_0.expressions.InequalityExpression ||
                        ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression() instanceof Equals);
    }
}
