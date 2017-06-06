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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by Roman on 06/06/2017.
 */
public class IngestHorsesToES {
    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = getClientDataOnly();
        BulkProcessor processor = getBulkProcessor(client);

        String filePath = "E:\\fuse_data\\demo_data_6June2017\\horses.csv";
        ObjectReader reader = new CsvMapper().reader(
                CsvSchema.builder().setColumnSeparator(',')
                        .addColumn("id", CsvSchema.ColumnType.NUMBER)
                        .addColumn("name", CsvSchema.ColumnType.STRING)
                        .addColumn("weight", CsvSchema.ColumnType.NUMBER)
                        .addColumn("maxSpeed", CsvSchema.ColumnType.NUMBER)
                        .addColumn("distance", CsvSchema.ColumnType.NUMBER)
                        .build()
        ).forType(new TypeReference<Map<String, Object>>() {
        });

        String index = "horses";
        String type = "Horse";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, Object> horse = reader.readValue(line);
                String id = "Horse_" + horse.remove("id");

                processor.add(new IndexRequest(index, type, id).source(horse));
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
}
