package com.kayhut.fuse.model.transport;

/*-
 * #%L
 * ExecutionScope.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ExecutionScope {
    public static final String clientParameter = "ExecutionScope.@maxExecutionTimeout";

    public ExecutionScope() {}

    @Inject
    public ExecutionScope(@Named(clientParameter) long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    private long timeout = 60 * 1000 * 3;

}
