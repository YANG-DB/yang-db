package com.kayhut.test.scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Roman on 07/06/2017.
 */
public class IngestFireToES {
    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = getClientDataOnly();
        BulkProcessor processor = getBulkProcessor(client);

        String filePath = "E:\\fuse_data\\edgesAfterEtl\\dragonsRelations_chunks\\dragonsRelations_FIRES-out.200005.csv";
        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityA.id", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.id", CsvSchema.ColumnType.STRING)
                        .addColumn("timestamp", CsvSchema.ColumnType.NUMBER)
                        .addColumn("temperature", CsvSchema.ColumnType.NUMBER)
                        .addColumn("entityB.type", CsvSchema.ColumnType.STRING)
                        .addColumn("direction", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.color", CsvSchema.ColumnType.STRING)
                        .addColumn("entityB.name", CsvSchema.ColumnType.STRING)
                        .build()
        ).forType(new TypeReference<Map<String, Object>>() {
        });

        String index = "fire200005";
        String type = "fire";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> fire = reader.readValue(line);
                String id = fire.remove("id").toString();

                Map<String, Object> entityA = new HashMap<>();
                entityA.put("id", fire.remove("entityA.id"));

                Map<String, Object> entityB = new HashMap<>();
                entityB.put("id", fire.remove("entityB.id"));
                entityB.put("type", fire.remove("entityB.type"));
                entityB.put("color", fire.remove("entityB.color"));
                entityB.put("name", fire.remove("entityB.name"));

                fire.put("entityA", entityA);
                fire.put("entityB", entityB);

                processor.add(new IndexRequest(index, type, id).source(fire));
            }
        }

        processor.awaitClose(5, TimeUnit.MINUTES);
    }

    private static Client getClient() throws UnknownHostException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "fuse-test")
                .build();

        return TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("52.174.90.109"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("13.93.93.10"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("13.93.93.190"), 9300));
    }

    private static Client getClientDataOnly() throws UnknownHostException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "fuse-test")
                .build();

        return TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("13.81.12.209"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("13.73.165.97"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("52.166.57.208"), 9300));
    }

    private static BulkProcessor getBulkProcessor(Client client) {
        return BulkProcessor.builder(client,
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long l, BulkRequest bulkRequest) {}
                    @Override
                    public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                        int x = 5;
                    }
                    @Override
                    public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
                        int x = 5;
                    }
                })
                .setBulkActions(1000)
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.GB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .build();
    }
}
