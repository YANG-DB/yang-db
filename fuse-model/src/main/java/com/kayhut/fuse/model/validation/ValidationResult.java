package com.kayhut.fuse.model.validation;

/*-
 * #%L
 * ValidationResult.java - fuse-model - kayhut - 2,016
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

import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Created by lior.perry on 5/29/2017.
 */
public class ValidationResult {
    public static ValidationResult OK = new ValidationResult(true, "none");

    public static String print(Object... elements) {
        StringJoiner joiner = new StringJoiner(":", "[", "]");
        Arrays.asList(elements).forEach(element -> joiner.add(element.toString()));
        return joiner.toString();
    }

    //region Constructors
    public ValidationResult(boolean valid, String validator, String... errors) {
        this(valid, validator, Stream.of(errors));
    }

    public ValidationResult(boolean valid, String validator, Iterable<String> errors) {
        this.valid = valid;
        this.validator = validator;
        this.errors = Stream.ofAll(errors).toJavaList();
    }
    //endregion

    //region Public Methods
    public boolean valid() {
        return valid;
    }

    public Iterable<String> errors() {
        return errors;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        if (valid())
            return "valid";
        return print(errors + ":" + validator);
    }

    public String getValidator() {
        return validator;
    }
//endregion

    //region Fields
    private final String validator;
    private boolean valid;
    private Iterable<String> errors;
    //endregion
}
