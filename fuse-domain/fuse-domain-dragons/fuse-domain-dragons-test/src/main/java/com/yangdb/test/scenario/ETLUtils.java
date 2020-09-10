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
import com.jayway.jsonpath.JsonPath;
import com.yangdb.fuse.model.Utils;
import com.yangdb.fuse.model.execution.plan.Direction;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.yangdb.test.etl.DateFieldPartitioner;
import com.yangdb.test.etl.Partitioner;
import net.minidev.json.JSONArray;
import org.apache.commons.io.FilenameUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.yangdb.fuse.model.Utils.readJsonFile;
import static com.yangdb.test.scenario.DragonsOntologyPhysicalIndexProviderFactory.*;

/**
 * Created by moti on 6/7/2017.
 */
public abstract class ETLUtils {
    public static final String ENTITY_B_ID = "EntityB.Id";
    public static final String ENTITY_B_TYPE = "EntityB.type";
    public static final String ENTITY_A_TYPE = "EntityA.type";
    public static final String ENTITY_A_ID = "EntityA.Id";
    public static final String DURATION = "duration";
    public static final String START_DATE = "startDate";
    public static final String SINCE = "since";
    public static final String END_DATE = "endDate";
    public static final String ID = "id";
    public static final String ESTABLISH_DATE = "establishDate";

    public static final String PERSON = "Person";
    public static final String KINGDOM = "Kingdom";
    public static final String MEMBER_OF_GUILD = "MemberOf";
    public static final String OWN = "Own";
    public static final String ORIGINATED = "Origin";
    public static final String REGISTERED = "Registered";
    public static final String SUBJECT_OF_KINGDOM = "Subject";
    public static final String KNOWS = "Know";
    public static final String GUILD = "Guild";
    public static final String HORSE = "Horse";
    public static final String FIRE = "Fire";
    public static final String FREEZE = "Freez";
    public static final String DRAGON = "Dragon";

    public static SimpleDateFormat sdf;
    public static final String DIRECTION_FIELD = "dir";

    static String confGraphLayoutProviderFactory = readJsonFile("schema/" + "ETLGraphLayoutFactory.conf");
    static String confDragonsIndexProvider = readJsonFile("schema/" + "DragonsIndexProvider.conf");

    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    static SimpleDateFormat dateFormat() {
        return sdf;
    }

    public static String id(String type, String id) {
        return type+"_"+id;
    }

    public static void createIndices(String fileName,String type, String index, Client client) throws IOException, ExecutionException, InterruptedException {
        String s = Utils.readJsonFile(fileName);
        List<String> indices = IntStream.range(1, 13).mapToObj(i -> index + String.format("%02d", i)).collect(Collectors.toList());
        indices.forEach(idx -> {
                    try {
                        CreateIndexResponse own = client.admin().indices().prepareCreate(idx).addMapping(type, s).execute().get();
                        System.out.println(own);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
        );
        //PutMappingResponse putMappingResponse = client.admin().getIndices().preparePutMapping(getIndices.toArray(new String[getIndices.size()])).setType("own").setSource(s).execute().get();
        //System.out.println(putMappingResponse);
    }

    static BulkProcessor getBulkProcessor(Client client) {
        return BulkProcessor.builder(client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long l, BulkRequest bulkRequest) {}
                    @Override
                    public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {}
                    @Override
                    public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {}
                })
                .setBulkActions(1000)
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .build();
    }

    public static TransportClient getClient() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", "fuse-test").build();
        return new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("13.81.12.209"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("13.73.165.97"), 9300))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("52.166.57.208"), 9300));
    }

    public static Map<String, String> redundant(String edgeType, Direction direction, String side) {
        JSONArray array = JsonPath.read(confGraphLayoutProviderFactory, "$['entities'][?(@.type == '" + edgeType + "')]['redundant']['" + direction.name() + "']['fields" + side + "']");
        Map<String, String> redundantFields = new HashMap<>();
        array.stream().flatMap(v -> ((JSONArray) v).stream()).forEach(m -> {
            Map<String, String> map = (Map<String, String>) m;
            redundantFields.put(map.get("name"), map.get("redundant_name"));
        });
        return redundantFields;

    }

    public static IndexPartitions indexPartition(String label) {
        JSONArray entity = JsonPath.read(confDragonsIndexProvider, "$['entities'][?(@.type =='" + label + "')]");
        Optional<IndexPartitions> partition = entity.stream().filter(p -> ((Map) p).containsKey(PARTITION)).map(v -> {
            if (((Map) v).get(PARTITION).equals(TIME)) {
                return new TimeBasedIndexPartitions((Map) v);
            } else {
                return new StaticIndexPartitions(indices((Map) v));
            }
        }).findFirst();
        return partition.orElse(new StaticIndexPartitions(Collections.emptyList()));
    }

    public static Iterable<String> indices(Map map) {
        JSONArray array = (JSONArray) ((Map) (map).get(PROPS)).get(VALUES);
        return Arrays.asList(array.toArray(new String[array.size()]));
    }

    public static void splitFileToChunks(String filePath , String destFolder, CsvSchema schema, String partitionField) {
        new File(destFolder).mkdirs();

        ObjectReader reader = new CsvMapper().reader(schema).forType(new TypeReference<Map<String, String>>() {
        });

        Partitioner partitioner = new DateFieldPartitioner(partitionField, "%s", "yyyy-MM-dd HH:mm:ss.SSS", "yyyyMM");

        Map<String, List<String>> bufferedPartitions = new HashMap<>();
        int maxBufferedLines = 100000;

        int numLines = 0;
        int totalNumLinesScanned = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, String> fire = reader.readValue(line);
                String partitionKey = partitioner.getPartition(fire);

                List<String> bufferedPartition = bufferedPartitions.get(partitionKey);
                if (bufferedPartition == null) {
                    bufferedPartition = new ArrayList<>();
                    bufferedPartitions.put(partitionKey, bufferedPartition);
                }

                bufferedPartition.add(line);
                numLines++;

                if (numLines == maxBufferedLines) {
                    flushBufferedPartitions(bufferedPartitions, filePath, destFolder);
                    totalNumLinesScanned += numLines;
                    numLines = 0;
                    bufferedPartitions.clear();

                    System.out.println("total # lines: " + totalNumLinesScanned);
                }
            }

            flushBufferedPartitions(bufferedPartitions, filePath, destFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void flushBufferedPartitions(Map<String, List<String>> bufferedPartitions, String filePath, String destFolder) throws IOException {
        String fileName = FilenameUtils.removeExtension(FilenameUtils.getName(filePath));
        String fileExtension = FilenameUtils.getExtension(filePath);

        for(Map.Entry<String, List<String>> entry : bufferedPartitions.entrySet()) {
            String partitionFileName = getPartitionFileName(destFolder, fileName, entry.getKey(), fileExtension);
            try (BufferedWriter wr = new BufferedWriter(new FileWriter(partitionFileName, true))) {
                for (String line : entry.getValue()) {
                    wr.write(line + System.lineSeparator());
                }
            }
        }
    }

    private static String getPartitionFileName(String destFolder, String fileName, String partitionKey, String fileExtension) {
        return Paths.get(destFolder, fileName + "." + partitionKey + "." + fileExtension).toString();
    }

}
