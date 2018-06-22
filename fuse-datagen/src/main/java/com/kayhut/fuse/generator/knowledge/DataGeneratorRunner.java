package com.kayhut.fuse.generator.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.generator.knowledge.idSuppliers.PrefixIdSupplier;
import com.kayhut.fuse.generator.knowledge.model.Entity;
import com.kayhut.fuse.generator.knowledge.model.KnowledgeEntityBase;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DataGeneratorRunner {
    public static void main(String[] args) throws IOException {
        String elasticConfigurationFile = args[0];
        String dataGenConfigurstionFile = args[1];

        ElasticConfiguration elasticConfiguration = new ObjectMapper().readValue(
                new File(elasticConfigurationFile),
                ElasticConfiguration.class);

        ContextGenerationConfiguration contextGenerationConfiguration = new ObjectMapper().readValue(
                new File(dataGenConfigurstionFile),
                ContextGenerationConfiguration.class);

        run(elasticConfiguration, contextGenerationConfiguration);
    }

    public static void run(
            ElasticConfiguration elasticConfiguration,
            ContextGenerationConfiguration contextGenerationConfiguration) throws UnknownHostException {
        Client client = getClient(elasticConfiguration);

        ContextStatistics contextStatistics =
                new SchemaKnowledgeGraphContextStatisticsProvider(client, elasticConfiguration.getReadSchema())
                .getContextStatistics(contextGenerationConfiguration.getFromContext());

        ElasticWriter elasticWriter = new ElasticWriter(client, elasticConfiguration, 1000);

        Supplier<String> logicalIdSupplier = new PrefixIdSupplier("e", 1000000, 2000000, elasticConfiguration.getWriteSchema());
        Supplier<String> entityValueIdSupplier = new PrefixIdSupplier("ev", 1000000, 2000000, elasticConfiguration.getWriteSchema());
        Supplier<String> referenceIdSupplier = new PrefixIdSupplier("ref", 1000000, 2000000, elasticConfiguration.getWriteSchema());


        KnowledgeContextEntityDataGenerator contextEntityDataGenerator = new KnowledgeContextEntityDataGenerator(
                client,
                new GenerationContext(contextGenerationConfiguration, contextStatistics),
                logicalIdSupplier,
                entityValueIdSupplier,
                referenceIdSupplier);

        List<String> logicalIdsInvolved = new ArrayList<>();

        Iterable<ElasticDocument<KnowledgeEntityBase>> knowledgeDocuments = contextEntityDataGenerator.generate(null);
        while(!Stream.ofAll(knowledgeDocuments).isEmpty()) {
            logicalIdsInvolved.addAll(Stream.ofAll(knowledgeDocuments)
                    .filter(doc -> doc.getSource() instanceof Entity)
                    .map(doc -> ((Entity)doc.getSource()).getLogicalId()).toJavaList());

            elasticWriter.write(knowledgeDocuments);
            knowledgeDocuments = contextEntityDataGenerator.generate(null);
        }

    }

    private static Client getClient(ElasticConfiguration elasticConfiguration) throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", "whatever")
                .put("client.transport.ignore_cluster_name", true)
                .build();

        TransportClient client = new PreBuiltTransportClient(settings);
        elasticConfiguration.getHosts().forEach(host -> {
            try {
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), 9300));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        return client;
    }
}
