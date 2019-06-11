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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by rani on 5/17/2018.
 */
public class KnowledgeJSONMapperSingleton {
    private static KnowledgeJSONMapperSingleton myObj;

    private SimpleDateFormat sdf;
    private ObjectMapper _mapper;

    /**
     * Create private constructor
     */
    private KnowledgeJSONMapperSingleton(){
        _mapper = new ObjectMapper();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        _mapper.setDateFormat(sdf);
    }

    /**
     * Create a static method to get instance.
     */
    public static KnowledgeJSONMapperSingleton getInstance(){
        if(myObj == null){
            myObj = new KnowledgeJSONMapperSingleton();
        }
        return myObj;
    }

    public ObjectMapper getMapper() {
        return _mapper;
    }
}
