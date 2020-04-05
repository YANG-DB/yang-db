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

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InputTypeConstraint {
    private String operand;
    private String operator;
    private String expression;

    public InputTypeConstraint() {
    }

    public InputTypeConstraint(String operand, String operator) {
        this.operand = operand;
        this.operator = operator;
    }

    public InputTypeConstraint(String operand, String operator, String expression) {
        this(operand, operator);
        this.expression = expression;
    }

    public String getOperand() {
        return operand;
    }

    public String getOperator() {
        return operator;
    }

    public String getExpression() {
        return expression;
    }
}
