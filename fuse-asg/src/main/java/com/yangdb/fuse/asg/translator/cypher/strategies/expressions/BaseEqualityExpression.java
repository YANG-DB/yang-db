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
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherStrategyContext;
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import org.opencypher.v9_0.expressions.BinaryOperatorExpression;
import org.opencypher.v9_0.expressions.Literal;
import org.opencypher.v9_0.expressions.Property;
import org.opencypher.v9_0.expressions.Variable;

import java.util.Collections;
import java.util.Optional;

public abstract class BaseEqualityExpression<T extends BinaryOperatorExpression> extends BaseExpressionStrategy {

    @Override
    public void apply(Optional<Expression> parent, Expression expression, AsgQuery query, CypherStrategyContext context) {
        T exp = get(( ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression()));
        org.opencypher.v9_0.expressions.Expression lhs = exp.lhs();
        org.opencypher.v9_0.expressions.Expression rhs = exp.rhs();

        Property property = null;
        if(Property.class.isAssignableFrom(lhs.getClass())) {
            property = ((Property) lhs);
        }

        if (CypherUtils.var(property).isEmpty()) return;

        Variable variable = CypherUtils.var(property).get(0);
        //first find the node element by its var name in the query
        Optional<AsgEBase<EBase>> byTag = AsgQueryUtil.getByTag(context.getScope(), variable.name());
        if (!byTag.isPresent())
            byTag = AsgQueryUtil.getByTag(query.getStart(), variable.name());

        if (!byTag.isPresent()) return;

        //when tag is of entity type
        if(EEntityBase.class.isAssignableFrom(byTag.get().geteBase().getClass())) {

            //update the scope
            context.scope(byTag.get());
            //change scope to quant
            final AsgEBase<? extends EBase> quantAsg = CypherUtils.quant(byTag.get(), parent, query, context);
            //add the label eProp constraint
            final int current = Math.max(quantAsg.getNext().stream().mapToInt(p -> p.geteNum()).max().orElse(0), quantAsg.geteNum());

            if (!AsgQueryUtil.nextAdjacentDescendant(quantAsg, EPropGroup.class).isPresent()) {
                quantAsg.addNext(new AsgEBase<>(new EPropGroup(current + 1, CypherUtils.type(parent, Collections.EMPTY_SET))));
            }

            ((EPropGroup) AsgQueryUtil.nextAdjacentDescendant(quantAsg, EPropGroup.class).get().geteBase())
                    .getProps().add(addPredicate(current, property.propertyKey().name(), constraint(exp.canonicalOperatorSymbol(), rhs)));
        }

        //when tag is of entity type
        if(Rel.class.isAssignableFrom(byTag.get().geteBase().getClass())) {
            //update the scope
            context.scope(byTag.get());

            if(!AsgQueryUtil.bAdjacentDescendant(byTag.get(), RelPropGroup.class).isPresent()) {
                final int current = Math.max(byTag.get().getB().stream().mapToInt(p->p.geteNum()).max().orElse(0),byTag.get().geteNum());
                byTag.get().addBChild(new AsgEBase<>(new RelPropGroup(100*current,CypherUtils.type(parent, Collections.EMPTY_SET))));
            }

            final int current = Math.max(byTag.get().getB().stream().mapToInt(p->p.geteNum()).max().orElse(0),byTag.get().geteNum());
            ((RelPropGroup) AsgQueryUtil.bAdjacentDescendant(byTag.get(), RelPropGroup.class).get().geteBase())
                    .getProps().add(new RelProp(current + 1, property.propertyKey().name(), constraint(exp.canonicalOperatorSymbol(), (org.opencypher.v9_0.expressions.Expression) literal(lhs, rhs)),0));
        }
    }

    protected Object literal(org.opencypher.v9_0.expressions.Expression lhs,
                             org.opencypher.v9_0.expressions.Expression rhs) {
        return Literal.class.isAssignableFrom(lhs.getClass()) ? ((Literal) lhs) :
                Literal.class.isAssignableFrom(rhs.getClass()) ? ((Literal) rhs) : null;
    }

    protected abstract T get(org.opencypher.v9_0.expressions.Expression expression);

    protected abstract Constraint constraint(String operator, org.opencypher.v9_0.expressions.Expression literal);

    @Override
    public abstract boolean isApply(Expression expression) ;


}
