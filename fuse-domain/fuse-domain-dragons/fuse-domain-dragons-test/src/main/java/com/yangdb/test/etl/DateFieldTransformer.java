package com.yangdb.test.etl;

/*-
 * #%L
 * fuse-domain-dragons-test
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

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.test.scenario.ETLUtils.sdf;

/**
 * Created by moti on 6/7/2017.
 */
public class DateFieldTransformer implements Transformer {
    private String[] fields;

    public DateFieldTransformer(String... fields) {
        this.fields = fields;
    }

    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<Map<String, String>> newDocs = documents.stream().map(HashMap::new).collect(Collectors.toList());
        newDocs.forEach(doc -> Arrays.asList(fields).forEach(
                field -> doc.put(field, sdf.format(new Date(Long.parseLong(doc.get(field)) * 1000)))));
        return newDocs;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        return oldSchema;
    }
}
