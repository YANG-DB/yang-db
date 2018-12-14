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
import com.kayhut.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import org.opencypher.v9_0.expressions.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.inSet;
import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class HasLabelExpression extends BaseExpressionStrategy {

    @Override
    public void apply(Optional<com.bpodgursky.jbool_expressions.Expression> parent, com.bpodgursky.jbool_expressions.Expression expression, AsgQuery query, CypherStrategyContext context) {
        HasLabels hasLabels = ((HasLabels) ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression());
        Collection<LabelName> labelNames = asJavaCollectionConverter(hasLabels.labels()).asJavaCollection();
        Variable variable = (Variable) hasLabels.expression();

        //first find the node element by its var name in the query
        final Optional<AsgEBase<EBase>> byTag = AsgQueryUtil.getByTag(context.getScope(), variable.name());
        if(!byTag.isPresent()) return;

        //update the scope
        context.scope(byTag.get());
        //change scope to quant
        final AsgEBase<EBase> quantAsg = CypherUtils.quant(byTag.get(),parent,query,context);
        //add the label eProp constraint
        final int current = Math.max(quantAsg.getNext().stream().mapToInt(p->p.geteNum()).max().orElse(0),quantAsg.geteNum());
        final List<String> labels = labelNames.stream().map(l -> l.name()).collect(Collectors.toList());

        if(!AsgQueryUtil.nextAdjacentDescendant(quantAsg, EPropGroup.class).isPresent()) {
            quantAsg.addNext(new AsgEBase<>(new EPropGroup(current + 1 ,CypherUtils.type(parent, Collections.EMPTY_SET))));
        }

        ((EPropGroup) AsgQueryUtil.nextAdjacentDescendant(quantAsg, EPropGroup.class).get().geteBase())
                .getProps().add(addPredicate(current,"type", of(inSet, labels)));
    }

    @Override
    public boolean isApply(com.bpodgursky.jbool_expressions.Expression expression) {
        return (expression instanceof com.bpodgursky.jbool_expressions.Variable) &&
                ((CypherUtils.Wrapper) ((com.bpodgursky.jbool_expressions.Variable) expression).getValue()).getExpression() instanceof HasLabels;
    }


}
