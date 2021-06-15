package com.yangdb.fuse.model.query.properties.constraint;


/*-
 *
 * ConstraintOp.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.Set;

/**
 * Created by lior.perry on 23/02/2017.
 */
public enum CountConstraintOp {

    @JsonProperty("eq")
    eq,

    @JsonProperty("ne")
    ne,

    @JsonProperty("gt")
    gt,

    @JsonProperty("ge")
    ge,

    @JsonProperty("lt")
    lt,

    @JsonProperty("le")
    le,

    @JsonProperty("within")
    between,

    @JsonProperty("within")
    within;

 public static Set<Class<? extends Constraint>> ignorableConstraints;
    public static Set<CountConstraintOp> noValueOps;
    public static Set<CountConstraintOp> singleValueOps;
    public static Set<CountConstraintOp> multiValueOps;
    public static Set<CountConstraintOp> exactlyTwoValueOps;

    static {
        ignorableConstraints = Stream.of(ParameterizedConstraint.class,
                JoinParameterizedConstraint.class,
                InnerQueryConstraint.class).toJavaSet();
        singleValueOps = Stream.of(eq, ne, gt, ge, lt, le).toJavaSet();
        multiValueOps = Collections.emptySet();
        exactlyTwoValueOps = Stream.of(between,within).toJavaSet();
    }

}
