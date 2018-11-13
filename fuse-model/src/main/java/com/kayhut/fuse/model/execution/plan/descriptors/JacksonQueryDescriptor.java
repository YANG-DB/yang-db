package com.kayhut.fuse.model.execution.plan.descriptors;

/*-
 * #%L
 * JacksonQueryDescriptor.java - fuse-model - kayhut - 2,016
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.descriptors.Descriptor;
import com.kayhut.fuse.model.query.Query;

public class JacksonQueryDescriptor implements Descriptor<Query> {
    //region Constructors
    public JacksonQueryDescriptor() {
        this.mapper = new ObjectMapper();
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(Query item) {
        try {
            return this.mapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion

    //region Fields
    private ObjectMapper mapper;
    //endregion
}
