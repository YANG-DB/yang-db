package com.yangdb.fuse.asg.translator.sparql.strategies.expressions;

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

import com.yangdb.fuse.asg.translator.sparql.strategies.SparqlStrategyContext;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.Compare;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.helpers.VarNameCollector;

import java.util.Optional;

/**
 * assuming the compare strategy works as follows;
 *  - lhe (left hand expression) is a var
 *  - rhe (right hand expression) is a val
 */
public class CompareExpressionStrategy implements ExpressionStrategies {

    @Override
    public void apply(ValueExpr expression, AsgQuery query, SparqlStrategyContext context) {
        if(Compare.class.isAssignableFrom(expression.getClass())) {
            ConstraintOp operator = operator(((Compare) expression).getOperator());
            //collect left side operand
            ValueExpr leftArg = ((Compare) expression).getLeftArg();

            //is lhe is value
            if(ValueConstant.class.isAssignableFrom(leftArg.getClass())) {
                Value value = ((ValueConstant) leftArg).getValue();

            } else {
            //lhe is a variable
                // find the node element by its var name in the query
                VarNameCollector leftVarName = new VarNameCollector();
                leftArg.visit(leftVarName);

                Optional<AsgEBase<EBase>> byTag = AsgQueryUtil.getByTag(context.getScope(), leftVarName.getVarNames().iterator().next());
                if (!byTag.isPresent())
                    byTag = AsgQueryUtil.getByTag(query.getStart(), leftVarName.getVarNames().iterator().next());

                //todo uncomment this line after query is build
//                if (!byTag.isPresent()) return;

            }

            //collect right side operand
            ValueExpr rightArg = ((Compare) expression).getRightArg();
            //is rhe is value
            if(ValueConstant.class.isAssignableFrom(rightArg.getClass())) {
                Value value = ((ValueConstant) rightArg).getValue();
            } else {
                VarNameCollector rightVarName = new VarNameCollector();
                leftArg.visit(rightVarName);
                //rhe is a variable
                // find the node element by its var name in the query
                Optional<AsgEBase<EBase>> byTag = AsgQueryUtil.getByTag(context.getScope(), rightVarName.getVarNames().iterator().next());
                if (!byTag.isPresent())
                    byTag = AsgQueryUtil.getByTag(query.getStart(), rightVarName.getVarNames().iterator().next());

                //todo uncomment this line after query is build
//                if (!byTag.isPresent()) return;

            }
            //todo add the constraint to the correct place in the asgQuery
        }
    }

    public static ConstraintOp operator(Compare.CompareOp op) {
        switch (op) {
            case EQ:return ConstraintOp.eq;
            case LE:return ConstraintOp.le;
            case GE:return ConstraintOp.ge;
            case GT:return ConstraintOp.gt;
            case NE:return ConstraintOp.ne;
            case LT:return ConstraintOp.lt;
            default:
                return ConstraintOp.notEmpty;
        }
    }
}
