package com.yangdb.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.google.inject.Inject;
import com.yangdb.fuse.assembly.knowledge.load.builder.EntityBuilder;
import com.yangdb.fuse.assembly.knowledge.load.builder.RelationBuilder;
import com.yangdb.fuse.assembly.knowledge.load.builder.RvalueBuilder;
import com.yangdb.fuse.assembly.knowledge.load.builder.ValueBuilder;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.model.Range;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class KnowledgeStatelessIdGenerator implements IdGeneratorDriver<Range> {

    private AtomicInteger eCounter;
    private AtomicInteger evCounter;
    private AtomicInteger fCounter;
    private AtomicInteger refCounter;
    private AtomicInteger relCounter;
    private AtomicInteger iCounter;

    //region Constructors
    @Inject
    public KnowledgeStatelessIdGenerator() {
        init(Collections.emptyList());
    }
    //endregion

    //region IdGenerator Implementation
    @Override
    public Range getNext(String genName, int numIds) {
        switch (genName) {
            case EntityBuilder.type:
                return new Range(eCounter.getAndAdd(numIds),eCounter.get());
            case ValueBuilder.type:
                return new Range(evCounter.getAndAdd(numIds),evCounter.get());
            case RelationBuilder.type:
                return new Range(relCounter.getAndAdd(numIds),relCounter.get());
            case RvalueBuilder.type:
                return new Range(relCounter.getAndAdd(numIds),relCounter.get());
            default:
                return new Range(0,1000);
        }
    }

    @Override
    public boolean init(List<String> names) {
        eCounter = new AtomicInteger(0);
        evCounter = new AtomicInteger(0);
        fCounter = new AtomicInteger(0);
        refCounter = new AtomicInteger(0);
        relCounter = new AtomicInteger(0);
        iCounter = new AtomicInteger(0);
        return true;
    }

}
