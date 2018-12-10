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

import com.bpodgursky.jbool_expressions.NExpression;
import com.bpodgursky.jbool_expressions.Variable;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.BaseProp;
import com.kayhut.fuse.model.query.properties.BasePropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantBase;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;
import org.opencypher.v9_0.expressions.*;

import java.util.*;

//import org.opencypher.v9_0.expressions.*;


public interface CypherUtils {

    static <T> List<T> reverse(List<T> list) {
        List<T> target = new ArrayList<>(list);
        Collections.reverse(target);
        return target;

    }

    static int maxEntityNum(AsgQuery query) {
        return Stream.ofAll(AsgQueryUtil.eNums(query,
                asgEBase -> !QuantBase.class.isAssignableFrom(asgEBase.geteBase().getClass())
                        && !BaseProp.class.isAssignableFrom(asgEBase.geteBase().getClass())
                        && !BasePropGroup.class.isAssignableFrom(asgEBase.geteBase().getClass())))
                .max().get();
    }

    static QuantType type(Optional<com.bpodgursky.jbool_expressions.Expression> operation) {
        if (!operation.isPresent())
            return QuantType.all;

        if (operation.get() instanceof com.bpodgursky.jbool_expressions.Or) {
            return QuantType.some;
        }
        if (operation.get() instanceof com.bpodgursky.jbool_expressions.And) {
            return QuantType.all;
        }

        return QuantType.all;
    }

    static AsgEBase<EBase> quant(AsgEBase<? extends EBase> byTag,
                                 Optional<com.bpodgursky.jbool_expressions.Expression> operation,
                                 AsgQuery query, CypherStrategyContext context) {
        //next find the quant associated with this element - if none found create one
        if (!AsgQueryUtil.nextAdjacentDescendant(byTag, QuantBase.class).isPresent()) {
            final int current = Math.max(context.getScope().geteNum(), Stream.ofAll(AsgQueryUtil.eNums(query,
                    asgEBase -> !QuantBase.class.isAssignableFrom(asgEBase.geteBase().getClass())))
                    .max().get());

            //quants will get enum according to the next formula = scopeElement.enum * 100
            final AsgEBase<Quant1> quantAsg = new AsgEBase<>(new Quant1(current * 100, CypherUtils.type(operation), new ArrayList<>(), 0));
            //is scope already has next - add them to the newly added quant
            if (context.getScope().hasNext()) {
                final List<AsgEBase<? extends EBase>> next = context.getScope().getNext();
                quantAsg.setNext(new ArrayList<>(next));
                context.getScope().setNext(new ArrayList<>());
            }
            context.getScope().addNext(quantAsg);
            context.scope(quantAsg);
        }
        return AsgQueryUtil.nextAdjacentDescendant(byTag, QuantBase.class).get();
    }

    static com.bpodgursky.jbool_expressions.Expression reWrite(org.opencypher.v9_0.expressions.Expression expression) {
        final com.bpodgursky.jbool_expressions.Expression traversal = Traversal.traversal(expression);
        final com.bpodgursky.jbool_expressions.Expression simplify = RuleSet.simplify(traversal);
        final com.bpodgursky.jbool_expressions.Expression dnf = RuleSet.toDNF(simplify);
        final com.bpodgursky.jbool_expressions.Expression simplifyDnf = RuleSet.simplify(dnf);
        return simplifyDnf;
    }


    static String asCanonicalStringVal(com.bpodgursky.jbool_expressions.Expression expression) {
        if (expression instanceof NExpression) {
            StringJoiner joiner = new StringJoiner(" " + expression.getExprType() + " ");
            ((NExpression) expression).getChildren().forEach(e ->
                    joiner.add(asCanonicalStringVal(((com.bpodgursky.jbool_expressions.Expression) e))));
            return joiner.toString();
        } else if (expression instanceof Variable) {
            final Expression value = (Expression) ((Variable) expression).getValue();
            if (value instanceof HasLabels) {
                return ((HasLabels) value).expression().asCanonicalStringVal();
            }
        }
        return "";
    }


    class Traversal {

        public static com.bpodgursky.jbool_expressions.Expression traversal(org.opencypher.v9_0.expressions.Expression expression) {
            if (expression instanceof Not) {
                return com.bpodgursky.jbool_expressions.Not.of(traversal(((Not) expression).rhs()));
            }
            if (expression instanceof Or) {
                return com.bpodgursky.jbool_expressions.Or.of(
                        traversal(((Or) expression).lhs()),
                        traversal(((Or) expression).rhs()));
            }

            if (expression instanceof And) {
                return com.bpodgursky.jbool_expressions.And.of(
                        traversal(((And) expression).lhs()),
                        traversal(((And) expression).rhs()));
            }
            return Variable.of(Wrapper.of(expression));
        }
    }


    class Wrapper {
        private org.opencypher.v9_0.expressions.Expression expression;

        private Wrapper(Expression expression) {
            this.expression = expression;
        }

        public static Wrapper of(Expression expression) {
            return new Wrapper(expression);
        }

        public Expression getExpression() {
            return expression;
        }

        @Override
        public String toString() {
            return expression.asCanonicalStringVal();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Wrapper wrapper = (Wrapper) o;
            return Objects.equals(expression, wrapper.expression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expression);
        }
    }
}
