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
import com.yangdb.fuse.model.execution.plan.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yangdb.test.scenario.ETLUtils.DIRECTION_FIELD;

/**
 * Created by moti on 6/5/2017.
 */
public class DuplicateEdgeTransformer implements Transformer {

    private String id1Field;
    private String id2Field;

    public DuplicateEdgeTransformer(String id1Field, String id2Field) {
        this.id1Field = id1Field;
        this.id2Field = id2Field;
    }
    
    @Override
    public List<Map<String, String>> transform(List<Map<String, String>> documents) {
        List<Map<String, String>> docs = new ArrayList<>();
        for (Map<String, String> document : documents) {
            Map<String, String> newDoc = new HashMap<>(document);
            newDoc.put(DIRECTION_FIELD, Direction.out.toString());
            docs.add(newDoc);

            newDoc = new HashMap<>(document);
            newDoc.put(id1Field, document.get(id2Field));
            newDoc.put(id2Field, document.get(id1Field));
            newDoc.put(DIRECTION_FIELD, Direction.in.toString());
            docs.add(newDoc);
        }

        return docs;
    }

    @Override
    public CsvSchema getNewSchema(CsvSchema oldSchema) {
        CsvSchema.Builder builder = CsvSchema.builder();
        oldSchema.forEach(c -> builder.addColumn(c));
        return builder.addColumn(DIRECTION_FIELD).build();

    }
}
