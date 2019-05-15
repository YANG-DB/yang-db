package com.kayhut.fuse.assembly.knowledge.parser.model;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;
import javaslang.control.Option;

public enum TypeConstraint {
    EQ("=",ConstraintOp.eq),
    EXACT("=",ConstraintOp.eq),
    IN("IN",ConstraintOp.inSet),
    RANGE("",ConstraintOp.inRange),
    LT("<",ConstraintOp.lt),
    GT(">",ConstraintOp.gt),
    PLAIN("CONTAINS",ConstraintOp.like);

    private String val;
    private ConstraintOp op;

    TypeConstraint(String val, ConstraintOp op){
        this.val = val;
        this.op = op;
    }

    public static Constraint asConstraint(String key,Object value) {
        final Option<TypeConstraint> constraints = Stream.of(TypeConstraint.values()).find(v -> v.name().equals(key));
        if(constraints.isEmpty())
            return Constraint.of(ConstraintOp.like,value);

        return Constraint.of(constraints.get().op,value);
    }
}
