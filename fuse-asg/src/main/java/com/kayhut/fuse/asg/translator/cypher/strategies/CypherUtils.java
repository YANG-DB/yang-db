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
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantBase;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.opencypher.v9_0.expressions.And;
import org.opencypher.v9_0.expressions.Not;
import org.opencypher.v9_0.expressions.Or;

import java.util.ArrayList;
import java.util.Optional;

//import org.opencypher.v9_0.expressions.*;


public interface CypherUtils {
    static QuantType type(Optional<org.opencypher.v9_0.expressions.OperatorExpression> operation) {
        if (!operation.isPresent())
            return QuantType.all;

        org.opencypher.v9_0.expressions.OperatorExpression expression = operation.get();
        if (expression instanceof org.opencypher.v9_0.expressions.Or) {
            return QuantType.some;
        }
        if (expression instanceof org.opencypher.v9_0.expressions.And) {
            return QuantType.all;
        }

        return QuantType.all;
    }

    static AsgEBase<EBase> quant(AsgEBase<? extends EBase> byTag, Optional<org.opencypher.v9_0.expressions.OperatorExpression> operation,
                                 AsgQuery query, CypherStrategyContext context) {
        //next find the quant associated with this element - if none found create one
        if (!AsgQueryUtil.nextDescendant(byTag, QuantBase.class).isPresent()) {
            final int current = context.getScope().geteNum();
            //quants will get enum according to the next formula = scopeElement.enum * 100
            final AsgEBase<Quant1> quantAsg = new AsgEBase<>(new Quant1(current * 100, CypherUtils.type(operation), new ArrayList<>(), 0));
            query.getElements().add(quantAsg);
            context.getScope().addNext(quantAsg);
            context.scope(quantAsg);
        }
        return AsgQueryUtil.nextDescendant(byTag, QuantBase.class).get();
    }

    static com.bpodgursky.jbool_expressions.Expression reWrite(org.opencypher.v9_0.expressions.Expression expression) {
        final com.bpodgursky.jbool_expressions.Expression traversal = Traversal.traversal(expression);
        final com.bpodgursky.jbool_expressions.Expression simplify = RuleSet.simplify(traversal);
        final com.bpodgursky.jbool_expressions.Expression dnf = RuleSet.toDNF(simplify);
        System.out.println(asCanonicalStringVal(dnf));
        return dnf;
    }


    static String asCanonicalStringVal(com.bpodgursky.jbool_expressions.Expression expression) {
        StringBuilder builder = new StringBuilder();
        if (expression instanceof NExpression) {
            builder.append(((NExpression) expression).getChildren().stream().reduce((t, u) ->
                    asCanonicalStringVal(((com.bpodgursky.jbool_expressions.Expression) t)) + " " + expression.getExprType()
                            + " " + asCanonicalStringVal((com.bpodgursky.jbool_expressions.Expression) u))
                    .get());
        } else {
            builder.append(expression.toLexicographicString());
        }
        return builder.toString();
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
            return Variable.of(expression);
        }
    }
}
