package com.yangdb.test.scenario;

/*-
 * #%L
 * fuse-domain-dragons-test
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.yangdb.fuse.client.elastic.BaseFuseElasticClient;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.yangdb.test.scenario.ETLUtils.getBulkProcessor;
import static com.yangdb.test.scenario.ETLUtils.getClient;

/**
 * Created by Roman on 07/06/2017.
 */
public class IngestKnowsToES {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
//        createIndices("mapping/owns.mapping", "own","own2000", getClient());
        BaseFuseElasticClient client = getClient();

        IntStream.range(1,13).forEach(p -> {
            try {
                writeToIndex("C:\\demo_data_6June2017\\knows_chunks", "personsRelations_KNOWS-out", "2000" +String.format("%02d", p), client);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }


    private static void writeToIndex(String folder, String filePrefix, String index, Client client) throws IOException, InterruptedException {
        String type = "know";
        BulkProcessor processor = getBulkProcessor(client);
        String filePath = Paths.get(folder,filePrefix+"."+index+".csv").toString();
        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.id", CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.type", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.type", CsvSchema.ColumnType.STRING)
                        .addColumn("direction", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.firstName", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.firstName", CsvSchema.ColumnType.STRING)
                        .build()
        ).forType(new TypeReference<Map<String, Object>>() {
        });


        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> fire = reader.readValue(line);
                String id = fire.remove("id").toString();

                fire.put("direction", fire.get("direction").toString().toUpperCase());

                Map<String, Object> entityA = new HashMap<>();
                entityA.put("type", fire.remove("entityA.type"));
                entityA.put("id", entityA.get("type").toString() + "_" + fire.remove("entityA.id"));
                entityA.put("firstName", fire.remove("entityA.firstName"));

                Map<String, Object> entityB = new HashMap<>();
                entityB.put("type", fire.remove("entityB.type"));
                entityB.put("id", entityB.get("type").toString() + "_" + fire.remove("entityB.id"));
                entityB.put("firstName", fire.remove("entityB.firstName"));

                fire.put("entityA", entityA);
                fire.put("entityB", entityB);

                processor.add(new IndexRequest("pr"+index, type, id)
                        .source(fire)
                        .routing(entityA.get("id").toString()));
            }
        }

        processor.awaitClose(5, TimeUnit.MINUTES);
        System.out.println("Completed loading "+index);
    }

}
