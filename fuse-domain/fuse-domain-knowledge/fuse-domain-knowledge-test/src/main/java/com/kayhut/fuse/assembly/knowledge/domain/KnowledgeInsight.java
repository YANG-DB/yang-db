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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lior.perry pc on 5/12/2018.
 */
public class KnowledgeInsight {
    private String _id;
    private Insight _insight;
    private List<KnowledgeEntity> _entitiesList;
    private List<KnowledgeReference> _refsList;

    public void setInsight(Insight i) {
        _insight = i;
    }

    public void setId(int i) {
        _id = "i" + String.format(KnowledgeRawSchemaSingleton.getInstance().getSchema().getIdFormat("insight"), i);
    }

    public String getId() {
        return _id;
    }

    public void addEntity(KnowledgeEntity e) {
        if (_entitiesList == null) {
            _entitiesList = new ArrayList<KnowledgeEntity>();
        }

        _entitiesList.add(e);
    }

    public void addRef(KnowledgeReference r) {
        if (_refsList == null) {
            _refsList = new ArrayList<KnowledgeReference>();
        }

        _refsList.add(r);
    }
}
