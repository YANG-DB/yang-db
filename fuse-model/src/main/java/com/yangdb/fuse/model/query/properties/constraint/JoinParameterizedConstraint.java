package com.yangdb.fuse.model.query.properties.constraint;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JoinParameterizedConstraint extends ParameterizedConstraint{
    private WhereByFacet.JoinType joinType;

    public JoinParameterizedConstraint() {
    }

    public JoinParameterizedConstraint(ConstraintOp op, Object expression, NamedParameter parameter, WhereByFacet.JoinType joinType) {
        super(op, expression, parameter);
        this.joinType = joinType;
    }

    public WhereByFacet.JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(WhereByFacet.JoinType joinType) {
        this.joinType = joinType;
    }

    @Override
    public JoinParameterizedConstraint clone() {
        return new JoinParameterizedConstraint(getOp(),getExpr(),getParameter(),getJoinType());
    }

    public static JoinParameterizedConstraint of(ConstraintOp op, NamedParameter exp, WhereByFacet.JoinType joinType) {
        return of(op,exp,"[]",joinType);
    }

    public static JoinParameterizedConstraint of(ConstraintOp op, NamedParameter exp, String iType,WhereByFacet.JoinType joinType) {
        JoinParameterizedConstraint constraint = new JoinParameterizedConstraint();
        constraint.setExpr(exp);
        constraint.setOp(op);
        constraint.setiType(iType);
        constraint.setJoinType(joinType);
        return constraint;
    }

}
