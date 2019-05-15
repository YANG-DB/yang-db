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
import com.kayhut.fuse.model.query.Query;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InnerQueryConstraint extends Constraint {

    private Query innerQuery;
    private String tagEntity;
    private String projectedField;

    public InnerQueryConstraint() {
    }

    public InnerQueryConstraint(ConstraintOp op, Query innerQuery, String tagEntity, String projectedField) {
        super(op, null);
        this.innerQuery = innerQuery;
        this.tagEntity = tagEntity;
        this.projectedField = projectedField;
    }

    public Query getInnerQuery() {
        return innerQuery;
    }

    public String getProjectedField() {
        return projectedField;
    }

    public String getTagEntity() {
        return tagEntity;
    }

    @Override
    public Object getExpr() {
        return projectedField;
    }

    public static InnerQueryConstraint of(ConstraintOp op, Query innerQuery, String tagEntity, String projectedFields) {
        return new InnerQueryConstraint(op, innerQuery, tagEntity, projectedFields);
    }

    public static InnerQueryConstraint of(ConstraintOp op, Query innerQuery) {
        return of(op, innerQuery, "", "");
    }

    public static InnerQueryConstraint of(ConstraintOp op, Query innerQuery, String iType) {
        return new InnerQueryConstraint(op, innerQuery, iType, "");
    }
}
