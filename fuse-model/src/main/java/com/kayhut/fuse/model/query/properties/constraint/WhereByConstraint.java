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

    private String tagEntity;
    private String projectedField;
    private JoinType joinType;

    public WhereByConstraint() {
    }

    public WhereByConstraint(ConstraintOp op,String tagEntity, String projectedField) {
        super(op, null);
        this.tagEntity = tagEntity;
        this.projectedField = projectedField;
        this.joinType = ConstraintOp.singleValueOps.contains(op) ? JoinType.FOR_EACH : JoinType.FULL;
    }

    public WhereByConstraint(ConstraintOp op, Object expression,String tagEntity, String projectedField) {
        super(op, expression);
        this.tagEntity = tagEntity;
        this.projectedField = projectedField;
        this.joinType = ConstraintOp.singleValueOps.contains(op) ? JoinType.FOR_EACH : JoinType.FULL;
    }

    public WhereByConstraint(ConstraintOp op, Object expression, String tagEntity,JoinType joinType, String projectedField) {
        super(op, expression);
        this.tagEntity = tagEntity;
        this.joinType = joinType;
        this.projectedField = projectedField;
    }

    public String getProjectedField() {
        return projectedField;
    }

    @Override
    public JoinType getJoinType() {
        return joinType;
    }

    public String getTagEntity() {
        return tagEntity;
    }

    @Override
    public WhereByConstraint clone()  {
        return new WhereByConstraint(getOp(), getExpr(),getTagEntity(), projectedField);
    }

    public static WhereByConstraint of(ConstraintOp op,String tagEntity, String projectedFields) {
        return new WhereByConstraint(op, tagEntity,projectedFields);
    }

    public static WhereByConstraint of(ConstraintOp op, Object expression,String tagEntity, String projectedFields) {
        return new WhereByConstraint(op, expression, tagEntity,projectedFields);
    }

    public static WhereByConstraint of(ConstraintOp op, Object expression,JoinType joinType, String tagEntity, String projectedFields) {
        return new WhereByConstraint(op, expression,tagEntity,joinType, projectedFields);
    }

}
