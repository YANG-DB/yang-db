package com.yangdb.fuse.dispatcher.query.graphql.wiring;

/*-
 * #%L
 * fuse-core
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InputTypeWhereClause {
    private WhereOperator operator;
    private List<InputTypeConstraint> constraints;

    public InputTypeWhereClause() {
    }

    public InputTypeWhereClause(WhereOperator operator, List<InputTypeConstraint> constraints) {
        this.operator = operator;
        this.constraints = constraints;
    }

    public WhereOperator getOperator() {
        return operator;
    }

    public List<InputTypeConstraint> getConstraints() {
        return constraints;
    }

    enum WhereOperator {
        AND,
        OR
    }
}
