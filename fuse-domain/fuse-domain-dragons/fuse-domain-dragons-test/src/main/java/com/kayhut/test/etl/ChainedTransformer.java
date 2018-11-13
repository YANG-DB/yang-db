package com.kayhut.test.etl;

/*-
 * #%L
 * fuse-domain-dragons-test
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

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by moti on 06/06/2017.
 */
public class ChainedTransformer implements Transformer{
    private List<Transformer> transformers;

    public ChainedTransformer(Transformer... transformers) {
        this.transformers = Arrays.stream(transformers).collect(Collectors.toList());
    }


    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<Map<String, String>> docs = documents;
        for (Transformer transformer : transformers) {
            docs = transformer.transform(docs);
        }
        return docs;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        CsvSchema schema = oldSchema;
        for (Transformer transformer : transformers) {
            schema = transformer.getNewSchema(schema);
        }
        return schema;
    }
}
