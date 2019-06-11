package com.kayhut.fuse.assembly.knowledge.domain;

/*-
 * #%L
 * fuse-domain-knowledge-test
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by lior.perry pc on 5/11/2018.
 */
public class KnowledgeEntity {
    private String _id;
    private String _logicalId;
    private Entity _entity;

    public String getId() {
        return _id;
    }

    public void setId(String value) {
        _id = value;
    }

    public String getLogicalId() {
        return _logicalId;
    }

    public void setLogicalId(String value) {
        _logicalId = value;
    }

    public Entity getEntity() {
        return _entity;
    }

    public String getElasticSearchJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(_entity);
    }
}
