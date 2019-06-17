package com.kayhut.fuse.model.query.properties.constraint;

/*-
 * #%L
 * ParameterizedConstraint.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "OptionalUnaryParameterizedConstraint", value = OptionalUnaryParameterizedConstraint.class),
        @JsonSubTypes.Type(name = "JoinParameterizedConstraint", value = JoinParameterizedConstraint.class)})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterizedConstraint extends Constraint {
    private NamedParameter parameter;

    public ParameterizedConstraint() {}

    public ParameterizedConstraint(ConstraintOp op,Object expression, NamedParameter parameter) {
        super(op,expression);
        this.parameter = parameter;
    }

    @Override
    public ParameterizedConstraint clone() {
        return new ParameterizedConstraint(getOp(),getExpr(),getParameter());
    }

    public NamedParameter getParameter() {
        return parameter;
    }

    public static ParameterizedConstraint of(ConstraintOp op) {
        return of(op, null, "[]");
    }

    public static ParameterizedConstraint of(ConstraintOp op, NamedParameter exp) {
        return of(op, exp, "[]");
    }

    public static ParameterizedConstraint of(ConstraintOp op, NamedParameter exp, String iType) {
        ParameterizedConstraint constraint = new ParameterizedConstraint();
        constraint.setExpr(exp);
        constraint.setOp(op);
        constraint.setiType(iType);
        return constraint;
    }
}
