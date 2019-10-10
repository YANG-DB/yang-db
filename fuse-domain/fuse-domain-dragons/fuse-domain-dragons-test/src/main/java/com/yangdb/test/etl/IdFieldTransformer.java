package com.yangdb.test.etl;

/*-
 *
 * fuse-domain-dragons-test
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

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by moti on 6/7/2017.
 */
public class IdFieldTransformer implements Transformer {
    private String idField;
    private String directionField;
    private String type;

    public IdFieldTransformer(String idField, String directionField, String type) {
        this.idField = idField;
        this.directionField = directionField;
        this.type = type;
    }

    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<Map<String, String>> newDocs = documents.stream().map(HashMap::new).collect(Collectors.toList());
        newDocs.forEach(doc -> doc.put(idField, type +"_" + doc.get(idField) + "_" + doc.get(directionField)));
        return newDocs;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        return oldSchema;
    }
}
