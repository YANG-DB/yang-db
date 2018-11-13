package com.kayhut.fuse.test.framework.providers;

/*-
 * #%L
 * fuse-test-framework
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

import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by moti on 4/12/2017.
 */
public class DotSeparatorDataProvider implements GenericDataProvider {
    private GenericDataProvider innerProvider;

    public DotSeparatorDataProvider(GenericDataProvider innerProvider) {
        this.innerProvider = innerProvider;
    }

    @Override
    public Iterable<Map<String, Object>> getDocuments() throws Exception {
        return Stream.ofAll(innerProvider.getDocuments()).map(doc -> {
            List<String> compositeKeys = Stream.ofAll(doc.keySet()).filter(key -> key.contains(".")).toJavaList();

            Map<String, Object> newValues = new HashMap<>();
            for(String key : compositeKeys){
                Object value = doc.remove(key);
                String[] parts = key.split("\\.");
                if(!newValues.containsKey(parts[0])){
                    newValues.put(parts[0], new HashMap<String, Object>());
                }
                ((HashMap<String, Object>)newValues.get(parts[0])).put(parts[1], value);
            }

            for (Map.Entry<String, Object> entry : newValues.entrySet()){
                doc.put(entry.getKey(), entry.getValue());
            }

            return doc;
        });
    }
}
