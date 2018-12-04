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

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantBase;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.opencypher.v9_0.expressions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public interface CypherUtils {
    static QuantType type(Optional<OperatorExpression> operation) {
        if (!operation.isPresent())
            return QuantType.all;

        final OperatorExpression expression = operation.get();
        if (expression instanceof Or) {
            return QuantType.some;
        }
        if (expression instanceof And) {
            return QuantType.all;
        }

        return QuantType.all;
    }

    static AsgEBase<EBase> quant(AsgEBase<? extends EBase> byTag, Optional<OperatorExpression> operation, AsgQuery query, CypherStrategyContext context) {
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

    static Expression reWrite(Expression expression) {
        List<Expression> elements = new ArrayList<>();
        final TreeNode root = new TreeNode(null, null);
        new InOrder().traverse(expression, root);
        return expression;

    }


    class InOrder {

        public void traverse(Expression expression, TreeNode root) {
            final TreeNode node = new TreeNode(expression.productPrefix(),expression.asCanonicalStringVal());
            root.child(node);
            if(expression instanceof BinaryOperatorExpression) {
                traverse(((BinaryOperatorExpression) expression).lhs(), node);
                traverse(((BinaryOperatorExpression) expression).rhs(), node);
            }
        }
    }

    class TreeNode {
        boolean visited = false;
        List<TreeNode> child = new ArrayList<>();
        String productPrefix;
        String asCanonicalStringVal;
        TreeNode parent;


        public TreeNode(String productPrefix,String asCanonicalStringVal) {
            this.productPrefix = productPrefix;
            this.asCanonicalStringVal = asCanonicalStringVal;
        }

        TreeNode visit() {
            visited = true;
            return this;
        }


        TreeNode child(TreeNode node) {
            this.child.add(node);
            node.parent = this;
            return this;
        }
    }
}
