package com.kayhut.fuse.model.query.properties.constraint;

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
public class WhereByConstraint extends Constraint implements WhereByFacet {

    private String projectedField;

    public WhereByConstraint() {
    }

    public WhereByConstraint(ConstraintOp op, String expression, String projectedField) {
        super(op, expression);
        this.projectedField = projectedField;
    }

    public String getProjectedField() {
        return projectedField;
    }

    public String getTagEntity() {
        return getExpr().toString();
    }

    @Override
    public WhereByConstraint clone()  {
        return new WhereByConstraint(getOp(), getExpr().toString(), projectedField);
    }

    public static WhereByConstraint of(ConstraintOp op, String expression, String projectedFields) {
        return new WhereByConstraint(op, expression, projectedFields);
    }
}
