package com.kayhut.fuse.generator.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.generator.knowledge.dataSuppliers.*;
import com.kayhut.fuse.generator.knowledge.idSuppliers.PrefixIdSupplier;
import com.kayhut.fuse.generator.knowledge.model.Entity;
import com.kayhut.fuse.generator.knowledge.model.KnowledgeEntityBase;
import com.kayhut.fuse.generator.knowledge.model.Reference;
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
import java.util.*;
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

        GenerationContext generationContext = new GenerationContext(elasticConfiguration, contextGenerationConfiguration, contextStatistics);

        ElasticWriter elasticWriter = new ElasticWriter(client, elasticConfiguration, 1000);

        // generate references
        KnowledgeGraphGenerator<Object> generator = getReferenceGenerator(client, elasticConfiguration, generationContext);
        Iterable<ElasticDocument<KnowledgeEntityBase>> knowledgeDocuments = generator.generate(null);
        List<String> referenceIds = new ArrayList<>();
        while (!Stream.ofAll(knowledgeDocuments).isEmpty()) {
            referenceIds.addAll(Stream.ofAll(knowledgeDocuments)
                    .filter(doc -> doc.getSource() instanceof Reference)
                    .map(ElasticDocument::getId)
                    .toJavaList());

            elasticWriter.write(knowledgeDocuments);
            knowledgeDocuments = generator.generate(null);
        }

        // generate entities
        generator = getContextEntityGenerator(client, elasticConfiguration, generationContext, referenceIds);
        List<String> logicalIdsInvolved = new ArrayList<>();
        knowledgeDocuments = generator.generate(null);
        while (!Stream.ofAll(knowledgeDocuments).isEmpty()) {
            logicalIdsInvolved.addAll(Stream.ofAll(knowledgeDocuments)
                    .filter(doc -> doc.getSource() instanceof Entity)
                    .map(doc -> ((Entity) doc.getSource()).getLogicalId()).toJavaList());

            elasticWriter.write(knowledgeDocuments);
            knowledgeDocuments = generator.generate(null);
        }
    }

    private static KnowledgeGraphGenerator<Object> getReferenceGenerator(Client client, ElasticConfiguration elasticConfiguration, GenerationContext generationContext) {
        Supplier<String> referenceIdSupplier = new PrefixIdSupplier("ref", 1000000, 2000000, elasticConfiguration.getWriteSchema());
        Supplier<Date> metadataDateSupplier = new UnifromBoundedDateSupplier(
                System.currentTimeMillis() - (long) (1000L * 60L * 60L * 24L * 30L),
                System.currentTimeMillis());
        Supplier<String> nameSupplier = new UnifromCachedSupplier<>(new NameSupplier(4, 12), 1000);
        Supplier<String> titleSupplier = new FunctionChainSupplier<>(
                new IterableSupplier<>(
                        new UniformDataTextSupplier(
                                client,
                                "reference",
                                "title",
                                generationContext.getElasticConfiguration().getReadSchema().getReferenceIndex(),
                                100000),
                        new UnifromBoundedIntSupplier(3, 10)),
                words -> String.join(" ", Stream.ofAll(words).toArray()));

        Supplier<String> contentSupplier = new FunctionChainSupplier<>(
                new IterableSupplier<>(
                        new UniformDataTextSupplier(
                                client,
                                "reference",
                                "content",
                                generationContext.getElasticConfiguration().getReadSchema().getReferenceIndex(),
                                100000),
                        new UnifromBoundedIntSupplier(8, 20)),
                words -> String.join(" ", Stream.ofAll(words).toArray()));

        Supplier<String> urlSupplier = new UrlSupplier();
        Supplier<String> systemSupplier = new UnifromCachedSupplier<>(Stream.of("system1", "system2", "system3", "eyeOfGod",
                "inspector", "insDocs", "facebook", "twitter", "insta", "whats", "guardian", "cnn", "bbc", "sky", "fox"));

        Supplier<KnowledgeEntityBase.Metadata> metadataSupplier = new MetadataSupplier(metadataDateSupplier, nameSupplier);

        return new KnowledgeContextReferenceDataGenerator(
                client,
                generationContext,
                referenceIdSupplier,
                metadataSupplier,
                titleSupplier,
                contentSupplier,
                urlSupplier,
                systemSupplier
        );
    }

    private static KnowledgeGraphGenerator<Object> getContextEntityGenerator(
            Client client,
            ElasticConfiguration elasticConfiguration,
            GenerationContext generationContext,
            List<String> referenceIds) {
        Supplier<String> logicalIdSupplier = new PrefixIdSupplier("e", 1000000, 2000000, elasticConfiguration.getWriteSchema());

        Supplier<String> categorySupplier = new UnifromCachedSupplier<>(
                Stream.ofAll(generationContext.getContextStatistics().getEntityCategories().entrySet())
                        .sortBy(Map.Entry::getValue)
                        .flatMap(entry -> Stream.fill(entry.getValue(), entry::getKey))
                        .toJavaList());

        Supplier<String> nameSupplier = new UnifromCachedSupplier<>(new NameSupplier(4, 12), 1000);
        Supplier<Date> metadataDateSupplier = new UnifromBoundedDateSupplier(
                System.currentTimeMillis() - (long) (1000L * 60L * 60L * 24L * 30L),
                System.currentTimeMillis());
        Supplier<Iterable<String>> refsSupplier = new IterableSupplier<>(
                new UnifromCachedSupplier<>(referenceIds),
                new UnifromCachedSupplier<>(
                        Stream.ofAll(generationContext.getContextStatistics().getEntityReferenceCounts().entrySet())
                                .sortBy(Map.Entry::getValue)
                                .flatMap(entry -> Stream.fill(entry.getValue(), entry::getKey))
                                .toJavaList()));

        Supplier<KnowledgeEntityBase.Metadata> metadataSupplier = new MetadataSupplier(
                metadataDateSupplier,
                nameSupplier,
                refsSupplier);

        return new KnowledgeContextEntityDataGenerator(
                client,
                generationContext,
                logicalIdSupplier,
                categorySupplier,
                metadataSupplier);
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
