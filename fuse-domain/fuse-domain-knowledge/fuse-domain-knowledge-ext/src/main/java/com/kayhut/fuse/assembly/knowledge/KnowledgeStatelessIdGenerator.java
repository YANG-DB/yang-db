package com.kayhut.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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
import com.kayhut.fuse.assembly.knowledge.load.builder.EntityBuilder;
import com.kayhut.fuse.assembly.knowledge.load.builder.RelationBuilder;
import com.kayhut.fuse.assembly.knowledge.load.builder.RvalueBuilder;
import com.kayhut.fuse.assembly.knowledge.load.builder.ValueBuilder;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.model.Range;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.engine.VersionConflictEngineException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kayhut.fuse.executor.ExecutorModule.globalClient;

public class KnowledgeStatelessIdGenerator implements IdGeneratorDriver<Range> {

    private AtomicInteger eCounter = new AtomicInteger(0);
    private AtomicInteger evCounter = new AtomicInteger(0);
    private AtomicInteger fCounter = new AtomicInteger(0);
    private AtomicInteger refCounter = new AtomicInteger(0);
    private AtomicInteger relCounter = new AtomicInteger(0);
    private AtomicInteger iCounter = new AtomicInteger(0);

    //region Constructors
    @Inject
    public KnowledgeStatelessIdGenerator() {}
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

}
