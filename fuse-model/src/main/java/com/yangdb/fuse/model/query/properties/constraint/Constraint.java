package com.yangdb.fuse.model.query.properties.constraint;

/*-
 * #%L
 * Constraint.java - fuse-model - yangdb - 2,016
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
        @JsonSubTypes.Type(name = "InnerQueryConstraint", value = InnerQueryConstraint.class),
        @JsonSubTypes.Type(name = "WhereByConstraint", value = WhereByConstraint.class),
        @JsonSubTypes.Type(name = "ParameterizedConstraint", value = ParameterizedConstraint.class)})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Constraint {

    //region Ctrs
    public Constraint() {
    }

    public Constraint(ConstraintOp op, Object expr) {
        this.op = op;
        this.expr = expr;
    }

    public Constraint(ConstraintOp op, Object expr, String iType) {
        this.op = op;
        this.expr = expr;
        this.iType = iType;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        Constraint other = (Constraint) o;

        if (this.op == null) {
            if (other.op != null) {
                return false;
            }
        } else {
            if (!this.op.equals(other.op)) {
                return false;
            }
        }

        if (this.expr == null) {
            if (other.expr != null) {
                return false;
            }
        } else {
            if (!this.expr.equals(other.expr)) {
                return false;
            }
        }

        if (this.iType == null) {
            if (other.iType != null) {
                return false;
            }
        } else {
            if (!this.iType.equals(other.iType)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Constraint clone() {
        return new Constraint(op,expr,iType);
    }

    //endregion

    //region Properties
    public ConstraintOp getOp() {
        return op;
    }

    public void setOp(ConstraintOp op) {
        this.op = op;
    }

    public Object getExpr() {
        return expr;
    }

    public void setExpr(Object expr) {
        this.expr = expr;
    }

    public String getiType() {
        return iType;
    }

    public void setiType(String iType) {
        this.iType = iType;
    }
    //endregion

    //region Fields
    private ConstraintOp op;
    private Object expr;
    //default - inclusive
    private String iType = "[]";
    //endregion

    public static Constraint of(ConstraintOp op) {
        return of(op, null, "[]");
    }

    public static Constraint of(ConstraintOp op, Object exp) {
        return of(op, exp, "[]");
    }

    public static Constraint of(ConstraintOp op, Object exp, String iType) {
        Constraint constraint = new Constraint();
        constraint.setExpr(exp);
        constraint.setOp(op);
        constraint.setiType(iType);
        return constraint;
    }

}
