package com.yangdb.fuse.assembly.knowledge.domain;

/*-
 *
 * fuse-domain-knowledge-test
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

/**
 * Created by lior.perry pc on 5/11/2018.
 */
public class KnowledgeRelation {
    private String _id;
    private KnowledgeEntity _aEntity, _bEntity;
    private Relation _rel;

    public void setId(String value) {
        _id = value;
    }

    public String getId() {
        return _id;
    }

    public void setEntity(KnowledgeEntity entity, boolean isA) {
        if (isA) {
            _aEntity = entity;
        } else {
            _bEntity = entity;
        }
    }

    public Relation getRelation() {
        if (_rel == null)
            _rel = new Relation();

        return _rel;
    }
}
