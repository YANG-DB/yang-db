package com.kayhut.fuse.assembly.knowledge.domain;

/*-
 * #%L
 * fuse-domain-knowledge-test
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

import com.kayhut.fuse.assembly.knowledge.KnowledgeRawSchemaShort;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;

/**
 * Created by lior.perry pc on 5/12/2018.
 */
public class KnowledgeRawSchemaSingleton {

    public static final String cIndexType = "pge";
    private static KnowledgeRawSchemaSingleton myObj;
    private KnowledgeRawSchemaShort _schema;
    /**
     * Create private constructor
     */
    private KnowledgeRawSchemaSingleton(){
        _schema = new KnowledgeRawSchemaShort();
    }
    /**
     * Create a static method to get instance.
     */
    public static KnowledgeRawSchemaSingleton getInstance(){
        if(myObj == null){
            myObj = new KnowledgeRawSchemaSingleton();
        }
        return myObj;
    }

    public RawSchema getSchema(){
        return _schema;
    }
}
