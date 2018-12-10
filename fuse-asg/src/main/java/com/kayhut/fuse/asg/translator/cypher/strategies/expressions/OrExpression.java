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
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantBase;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;
import org.opencypher.v9_0.expressions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.asg.translator.cypher.strategies.CypherUtils.maxEntityNum;
import static com.kayhut.fuse.asg.translator.cypher.strategies.CypherUtils.reverse;

public class OrExpression implements ExpressionStrategies {

    public OrExpression(Iterable<ExpressionStrategies> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void apply(Optional<com.bpodgursky.jbool_expressions.Expression> parent, com.bpodgursky.jbool_expressions.Expression expression, AsgQuery query, CypherStrategyContext context) {
        //filter only AND expressions
        if ((expression instanceof com.bpodgursky.jbool_expressions.Or)) {
            //todo parent is empty - create a 'all'-quant as query start
            //rechain elements after start for new root quant
            List<AsgEBase<? extends EBase>> chain = context.getScope().getNext();
            int maxEnum = maxEntityNum(query);

            if(!parent.isPresent()) {
                context.scope(query.getStart());
                chain = context.getScope().getNext();
                //next find the quant associated with this element - if none found create one
                if (!AsgQueryUtil.nextAdjacentDescendant(context.getScope(), QuantBase.class).isPresent()) {
                    final int current = Math.max(context.getScope().geteNum(), maxEntityNum(query));
                    //quants will get enum according to the next formula = scopeElement.enum * 100
                    final AsgEBase<Quant1> quantAsg = new AsgEBase<>(new Quant1(current * 100, QuantType.some, new ArrayList<>(), 0));
                    context.getScope().setNext(Arrays.asList(quantAsg));
                }

                //set root quant at scope
                context.scope(AsgQueryUtil.nextAdjacentDescendant(context.getScope(), QuantBase.class).get());
            }

            com.bpodgursky.jbool_expressions.Or or = (com.bpodgursky.jbool_expressions.Or) expression;
            //max enum


            List<AsgEBase<? extends EBase>> finalChain = chain;
            reverse(((List<Expression>) or.getChildren()))
                    .forEach(c -> {
                        //todo count distinct variables
                        int newMaxEnum = Math.max(maxEnum,maxEntityNum(query));

                        //base quant to add onto
                        final AsgEBase<? extends EBase> base = context.getScope();
                        //duplicate query from scope to end
                        final AsgEBase<? extends EBase> clone = AsgQueryUtil.deepCloneWithEnums(newMaxEnum,finalChain.get(0), e -> true, e -> true);
                        //add duplication to scope
                        context.getScope().addNext(clone);
                        //run strategies on current scope
                        context.scope(clone);
                        strategies.forEach(s -> s.apply(Optional.of(or), c, query, context));
                        context.scope(base);

                    });
        }
    }

    private Iterable<ExpressionStrategies> strategies;

}
