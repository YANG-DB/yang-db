package com.kayhut.fuse.model.query.properties.constraint;

/*-
 * #%L
 * OptionalUnaryParameterizedConstraint.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
