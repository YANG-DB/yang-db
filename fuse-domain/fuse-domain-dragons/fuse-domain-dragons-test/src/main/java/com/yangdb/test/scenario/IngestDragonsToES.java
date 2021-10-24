package com.yangdb.test.scenario;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.opensearch.action.bulk.BulkProcessor;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.Client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yangdb.test.scenario.ETLUtils.*;

/**
 * Created by Roman on 06/06/2017.
 */
public class IngestDragonsToES {


    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = getClient();
        BulkProcessor processor = getBulkProcessor(client);

        String filePath = "E:\\fuse_data\\demo_data_6June2017\\dragons.csv";
        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.NUMBER)
                        .addColumn("name", CsvSchema.ColumnType.STRING)
                        .addColumn("power", CsvSchema.ColumnType.NUMBER)
                        .addColumn("gender", CsvSchema.ColumnType.STRING)
                        .addColumn("color", CsvSchema.ColumnType.STRING)
                        .build()
        ).forType(new TypeReference<Map<String, Object>>() {});

        String index = "dragons";
        String type = DRAGON;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> dragon = reader.readValue(line);
                String id = id(type, dragon.remove("id").toString());

                processor.add(new IndexRequest(index, type, id).source(dragon));
            }
        }

        processor.awaitClose(5, TimeUnit.MINUTES);
    }


}
