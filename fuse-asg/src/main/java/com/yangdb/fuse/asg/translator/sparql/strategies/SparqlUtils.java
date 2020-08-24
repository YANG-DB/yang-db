package com.yangdb.fuse.asg.translator.sparql.strategies;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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
import com.bpodgursky.jbool_expressions.NExpression;
import com.bpodgursky.jbool_expressions.Variable;
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantBase;

import java.util.*;

import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.*;

public interface SparqlUtils {

    static AsgEBase<? extends EBase> quant(AsgEBase<? extends EBase> byTag,
                                           Optional<Expression> operation,
                                           AsgQuery query, SparqlStrategyContext context) {
        //next find the quant associated with this element - if none found create one
        if (!AsgQueryUtil.nextAdjacentDescendant(byTag, QuantBase.class).isPresent()) {
            final int current = max(query);

            final Set<Variable> distinct = distinct(operation);
            //quants will get enum according to the next formula = scopeElement.enum * 100
            final AsgEBase<Quant1> quantAsg = new AsgEBase<>(new Quant1(current+1, CypherUtils.type(operation, distinct), new ArrayList<>(), 0));
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

    static Set<Variable> distinct(Optional<com.bpodgursky.jbool_expressions.Expression> operation) {
        Set<Variable> vars = new HashSet<>();
        if (!operation.isPresent()) return Collections.emptySet();

        if (NExpression.class.isAssignableFrom(operation.get().getClass())) {
            final List<com.bpodgursky.jbool_expressions.Expression> children = ((NExpression) operation.get()).getChildren();
            children.forEach(c -> vars.addAll(distinct(Optional.of(c))));
        } else if (com.bpodgursky.jbool_expressions.Variable.class.isAssignableFrom(operation.get().getClass())) {
            return Collections.singleton(((Variable) operation.get()));
        }

        return vars;
    }

}
