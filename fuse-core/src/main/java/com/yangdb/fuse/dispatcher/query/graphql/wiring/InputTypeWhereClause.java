package com.yangdb.fuse.dispatcher.query.graphql.wiring;

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
