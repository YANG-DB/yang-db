package com.yangdb.fuse.dispatcher.query.graphql.wiring;

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
