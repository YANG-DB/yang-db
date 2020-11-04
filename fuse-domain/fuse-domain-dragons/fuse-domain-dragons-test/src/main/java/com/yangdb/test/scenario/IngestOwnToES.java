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
import com.yangdb.fuse.model.GlobalConstants;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;

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
public class IngestOwnToES {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
//        createIndices("mapping/owns.mapping", "own","own2000", getClient());
        TransportClient client = getClient();
        loadDragons(client);
    }

    private static void loadHorses(TransportClient client) {
        IntStream.range(1,13).forEach(p -> {
            try {
                writeToIndex("C:\\demo_data_6June2017\\own_horses_chunks", "personsRelations_OWNS_HORSE-out", "2000" +String.format("%02d", p), client);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static void loadDragons(TransportClient client) {
        IntStream.range(1,13).forEach(p -> {
            try {
                writeToIndex("C:\\demo_data_6June2017\\own_dragons_chunks", "personsRelations_OWNS_DRAGON-out", "2000" +String.format("%02d", p), client);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }


    private static void writeToIndex(String folder, String filePrefix, String index, Client client) throws IOException, InterruptedException {
        String type = "own";
        BulkProcessor processor = getBulkProcessor(client);
        String filePath = Paths.get(folder,filePrefix+"."+index+".csv").toString();
        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_ID, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_ID, CsvSchema.ColumnType.STRING)
                        .addColumn("startDate", CsvSchema.ColumnType.STRING)
                        .addColumn("endDate", CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DIRECTION, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_TYPE, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.SOURCE_NAME, CsvSchema.ColumnType.STRING)
                        .addColumn(GlobalConstants.EdgeSchema.DEST_NAME, CsvSchema.ColumnType.STRING)
                        .build()
        ).forType(new TypeReference<Map<String, Object>>() {
        });


        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> fire = reader.readValue(line);
                String id = fire.remove("id").toString();

                fire.put(GlobalConstants.EdgeSchema.DIRECTION, fire.get(GlobalConstants.EdgeSchema.DIRECTION).toString().toUpperCase());

                Map<String, Object> entityA = new HashMap<>();
                entityA.put("type", fire.remove(GlobalConstants.EdgeSchema.SOURCE_TYPE));
                entityA.put("id", entityA.get("type").toString() + "_" + fire.remove(GlobalConstants.EdgeSchema.SOURCE_ID));
                entityA.put("name", fire.remove(GlobalConstants.EdgeSchema.SOURCE_NAME));

                Map<String, Object> entityB = new HashMap<>();
                entityB.put("type", fire.remove(GlobalConstants.EdgeSchema.DEST_TYPE));
                entityB.put("id", entityB.get("type").toString() + "_" + fire.remove(GlobalConstants.EdgeSchema.DEST_ID));
                entityB.put("name", fire.remove(GlobalConstants.EdgeSchema.DEST_NAME));

                fire.put(GlobalConstants.EdgeSchema.SOURCE, entityA);
                fire.put(GlobalConstants.EdgeSchema.DEST, entityB);

                processor.add(new IndexRequest(type+index, type, id)
                        .source(fire)
                        .routing(entityA.get("id").toString()));
            }
        }

        processor.awaitClose(5, TimeUnit.MINUTES);
        System.out.println("Completed loading "+index);
    }

}
