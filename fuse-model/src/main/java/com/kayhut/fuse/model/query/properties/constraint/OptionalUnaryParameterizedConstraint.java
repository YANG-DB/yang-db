package com.kayhut.fuse.model.query.properties.constraint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collections;
import java.util.Set;


@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionalUnaryParameterizedConstraint extends ParameterizedConstraint {

    public OptionalUnaryParameterizedConstraint() {}

    public OptionalUnaryParameterizedConstraint(ConstraintOp defaultValue, Set<ConstraintOp> ops, NamedParameter parameter) {
        //set defaultValue as the op field of the base class (calling OptionalUnaryParameterizedConstraint.getOps() will result with the default value)
        super(defaultValue,parameter);
        this.operations = ops;
    }

    public Set<ConstraintOp> getOperations() {
        return operations;
    }

    private Set<ConstraintOp> operations = Collections.emptySet();
}
