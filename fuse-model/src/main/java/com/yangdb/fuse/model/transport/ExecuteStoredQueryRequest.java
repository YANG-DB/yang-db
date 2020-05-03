package com.yangdb.fuse.model.transport;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

/*-
 *
 * ExecuteStoredQueryRequest.java - fuse-model - yangdb - 2,016
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.yangdb.fuse.model.query.QueryRef;
import com.yangdb.fuse.model.query.properties.constraint.NamedParameter;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;

import java.util.Collection;
import java.util.Collections;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecuteStoredQueryRequest extends CreateQueryRequest {

    private final Collection<NamedParameter> parameters;
    private final Collection<NamedParameter> executionParams;

    public ExecuteStoredQueryRequest() {
        this.parameters = Collections.EMPTY_LIST;
        this.executionParams = Collections.EMPTY_LIST;
    }

    public ExecuteStoredQueryRequest(String id, String name, Collection<NamedParameter> parameters,Collection<NamedParameter> executionParams) {
        super(id, name, new QueryRef(name));
        this.parameters = parameters;
        this.executionParams = executionParams;
    }

    public ExecuteStoredQueryRequest(String id, String name, CreateCursorRequest createCursorRequest, Collection<NamedParameter> parameters,Collection<NamedParameter> executionParams) {
        super(id, name, new QueryRef(name), new PlanTraceOptions(), createCursorRequest);
        this.parameters = parameters;
        this.executionParams = executionParams;
    }

    public Collection<NamedParameter> getExecutionParams() {
        return executionParams;
    }

    public Collection<NamedParameter> getParameters() {
        return parameters;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public CreatePageRequest getPageCursorRequest() {
        return (getCreateCursorRequest() != null ? getCreateCursorRequest().getCreatePageRequest() : null);
    }

    @Override
    public String toString() {
        return "ExecuteStoredQueryRequest{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", query=" + QueryDescriptor.toString(getQuery()) + "\n"+
                ", createCursorRequest=" + (getCreateCursorRequest()!=null ? getCreateCursorRequest().toString() : "None" )+
                ", parameters=" + parameters +
                ", executionParams=" + executionParams +
                '}';
    }
}
