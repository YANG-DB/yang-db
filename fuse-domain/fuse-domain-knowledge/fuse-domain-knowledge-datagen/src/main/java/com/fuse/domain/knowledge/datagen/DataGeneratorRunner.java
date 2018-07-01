package com.fuse.domain.knowledge.datagen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.domain.knowledge.datagen.dataSuppliers.*;
import com.fuse.domain.knowledge.datagen.idSuppliers.FormatIdSupplier;
import com.fuse.domain.knowledge.datagen.model.Entity;
import com.fuse.domain.knowledge.datagen.model.KnowledgeEntityBase;
import com.fuse.domain.knowledge.datagen.model.Reference;
import com.fuse.domain.knowledge.datagen.model.Relation;
import com.kayhut.fuse.unipop.controller.search.DefaultSearchOrderProvider;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
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

import static org.elasticsearch.index.query.QueryBuilders.*;

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

        Supplier<KnowledgeEntityBase.Metadata> commonMetadataSupplier = getCommonMetadataSupplier(
                generationContext.getContextStatistics().getEntityReferenceCounts(),
                referenceIds);

        // generate entities
        generator = getContextEntityGenerator(client, elasticConfiguration, generationContext, commonMetadataSupplier);
        List<String> logicalIdsInvolved = new ArrayList<>();
        knowledgeDocuments = generator.generate(null);
        while (!Stream.ofAll(knowledgeDocuments).isEmpty()) {
            logicalIdsInvolved.addAll(Stream.ofAll(knowledgeDocuments)
                    .filter(doc -> doc.getSource() instanceof Entity)
                    .map(doc -> ((Entity) doc.getSource()).getLogicalId()).toJavaList());

            elasticWriter.write(knowledgeDocuments);
            knowledgeDocuments = generator.generate(null);
        }

        Supplier<KnowledgeEntityBase.Metadata> commonEntityValueMetadataSupplier = getCommonMetadataSupplier(
                generationContext.getContextStatistics().getEntityValueReferenceCounts(),
                referenceIds);

        //generate entity global values
        Supplier<String> entityValueIdSupplier = new FormatIdSupplier("ev" + elasticConfiguration.getWriteSchema().getIdFormat(), 1000000, 2000000);

        Set<String> fromContextLogicalIds =
                Stream.ofAll(new SearchHitScrollIterable(
                        client,
                        client.prepareSearch().setIndices(elasticConfiguration.getReadSchema().getEntityIndex())
                                .setQuery(boolQuery().filter(boolQuery()
                                        .must(termQuery("type", "entity"))
                                        .must(termQuery("context", generationContext.getContextGenerationConfiguration().getFromContext()))
                                        .mustNot(existsQuery("deleteTime"))))
                                .setFetchSource(new String[]{"logicalId"}, null),
                        new DefaultSearchOrderProvider().build(null),
                        1000000, 1000, 60000)).map(hit -> (String) hit.sourceAsMap().get("logicalId")).distinct().toJavaSet();
        List<String> newLogicalIds = Stream.ofAll(logicalIdsInvolved).filter(id -> !fromContextLogicalIds.contains(id)).toJavaList();

        Stream.ofAll(generationContext.getContextStatistics().getEntityFieldTypes().keySet())
                .filter(field -> globalFieldNames.contains(field))
                .forEach(fieldId -> {
                    KnowledgeGraphGenerator<Object> generator1 = null;
                    try {
                        generator1 = getGlobalEntityValueGenerator(
                                client,
                                elasticConfiguration,
                                fieldId,
                                generationContext,
                                entityValueIdSupplier,
                                commonEntityValueMetadataSupplier,
                                newLogicalIds,
                                fromContextLogicalIds);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Iterable<ElasticDocument<KnowledgeEntityBase>> knowledgeDocuments1 = generator1.generate(null);
                    while (!Stream.ofAll(knowledgeDocuments1).isEmpty()) {
                        elasticWriter.write(knowledgeDocuments1);
                        knowledgeDocuments1 = generator1.generate(null);
                    }
                });

        // generate entity context values
        Stream.ofAll(generationContext.getContextStatistics().getEntityFieldTypes().keySet())
                .filter(fieldId -> !globalFieldNames.contains(fieldId))
                .forEach(fieldId -> {
                    KnowledgeGraphGenerator<Object> generator1 = null;
                    try {
                        generator1 = getContextEntityValueGenerator(
                                client,
                                elasticConfiguration,
                                fieldId,
                                generationContext,
                                entityValueIdSupplier,
                                commonEntityValueMetadataSupplier,
                                logicalIdsInvolved);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Iterable<ElasticDocument<KnowledgeEntityBase>> knowledgeDocuments1 = generator1.generate(null);
                    while (!Stream.ofAll(knowledgeDocuments1).isEmpty()) {
                        elasticWriter.write(knowledgeDocuments1);
                        knowledgeDocuments1 = generator1.generate(null);
                    }
                });

        commonMetadataSupplier = getCommonMetadataSupplier(
                generationContext.getContextStatistics().getRelationReferenceCounts(),
                referenceIds);

        // generate context relations
        generator = getContextRelationGenerator(client, elasticConfiguration, generationContext, commonMetadataSupplier, logicalIdsInvolved);
        List<String> relationIds = new ArrayList<>();
        knowledgeDocuments = generator.generate(null);
        while (!Stream.ofAll(knowledgeDocuments).isEmpty()) {
            relationIds.addAll(Stream.ofAll(knowledgeDocuments)
                    .filter(doc -> doc.getSource() instanceof Relation)
                    .map(ElasticDocument::getId)
                    .toJavaList());

            elasticWriter.write(knowledgeDocuments);
            knowledgeDocuments = generator.generate(null);
        }

        Supplier<KnowledgeEntityBase.Metadata> commonRelationValueMetadataSupplier = getCommonMetadataSupplier(
                generationContext.getContextStatistics().getRelationValueReferenceCounts(),
                referenceIds);
        Supplier<String> relationValueIdSupplier = new FormatIdSupplier("ev" + elasticConfiguration.getWriteSchema().getIdFormat(), 1000000, 2000000);

        // generate relation context values
        Stream.ofAll(generationContext.getContextStatistics().getRelationFieldTypes().keySet())
                .forEach(fieldId -> {
                    KnowledgeGraphGenerator<Object> generator1 = null;
                    try {
                        generator1 = getContextRelationValueGenerator(
                                client,
                                elasticConfiguration,
                                fieldId,
                                generationContext,
                                relationValueIdSupplier,
                                commonRelationValueMetadataSupplier,
                                relationIds);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Iterable<ElasticDocument<KnowledgeEntityBase>> knowledgeDocuments1 = generator1.generate(null);
                    while (!Stream.ofAll(knowledgeDocuments1).isEmpty()) {
                        elasticWriter.write(knowledgeDocuments1);
                        knowledgeDocuments1 = generator1.generate(null);
                    }
                });

        Supplier<KnowledgeEntityBase.Metadata> commonInsightMetadataSupplier = getCommonMetadataSupplier(
                generationContext.getContextStatistics().getInsightReferenceCounts(),
                referenceIds);

        // generate insights
        generator = getContextInsightGenerator(client, elasticConfiguration, generationContext, commonInsightMetadataSupplier, logicalIdsInvolved);
        knowledgeDocuments = generator.generate(null);
        while (!Stream.ofAll(knowledgeDocuments).isEmpty()) {
            elasticWriter.write(knowledgeDocuments);
            knowledgeDocuments = generator.generate(null);
        }
    }

    private static KnowledgeGraphGenerator<Object> getReferenceGenerator(Client client, ElasticConfiguration elasticConfiguration, GenerationContext generationContext) {
        Supplier<String> referenceIdSupplier = new FormatIdSupplier("ref" + elasticConfiguration.getWriteSchema().getIdFormat(), 1000000, 2000000);
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
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier) {
        String fromContext = generationContext.getContextGenerationConfiguration().getFromContext();

        Supplier<String> logicalIdSupplier = new ProbabilisticCompositeSupplier<>(
                new UniformDataTextSupplier(
                        client,
                        "entity",
                        "logicalId",
                        fromContext,
                        generationContext.getElasticConfiguration().getReadSchema().getEntityIndex(),
                        100000),
                generationContext.getContextGenerationConfiguration().getEntityOverlapFactor(),
                new FormatIdSupplier("e" + elasticConfiguration.getWriteSchema().getIdFormat(), 1000000, 2000000));

        Supplier<String> categorySupplier = new UnifromCachedSupplier<>(
                Stream.ofAll(generationContext.getContextStatistics().getEntityCategories().entrySet())
                        .sortBy(Map.Entry::getValue)
                        .flatMap(entry -> Stream.fill(entry.getValue(), entry::getKey))
                        .toJavaList());

        return new KnowledgeContextEntityDataGenerator(
                client,
                generationContext,
                logicalIdSupplier,
                categorySupplier,
                metadataSupplier);
    }

    private static KnowledgeGraphGenerator<Object> getContextRelationGenerator(
            Client client,
            ElasticConfiguration elasticConfiguration,
            GenerationContext generationContext,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier,
            List<String> logicalIds) {

        Supplier<String> logicalIdSupplier = new UnifromCachedSupplier<String>(logicalIds);
        Supplier<String> relationIdSupplier = new FormatIdSupplier("r" + elasticConfiguration.getWriteSchema().getIdFormat(), 1000000, 2000000);

        Supplier<String> categorySupplier = new UnifromCachedSupplier<>(
                Stream.ofAll(generationContext.getContextStatistics().getRelationCategories().entrySet())
                        .sortBy(Map.Entry::getValue)
                        .flatMap(entry -> Stream.fill(entry.getValue(), entry::getKey))
                        .toJavaList());

        Supplier<Integer> numOutRelationsSupplier =
                new UnifromCachedSupplier<>(
                Stream.ofAll(generationContext.getContextStatistics().getEntityRelationCounts().get("out").entrySet())
                        .sortBy(Map.Entry::getValue)
                        .flatMap(entry1 -> Stream.fill(entry1.getValue(), entry1::getKey))
                        .toJavaList());

        Supplier<Integer> numInRelationsSupplier =
                new UnifromCachedSupplier<>(
                        Stream.ofAll(generationContext.getContextStatistics().getEntityRelationCounts().get("in").entrySet())
                                .sortBy(Map.Entry::getValue)
                                .flatMap(entry1 -> Stream.fill(entry1.getValue(), entry1::getKey))
                                .toJavaList());

        return new KnowledgeContextRelationDataGenerator(
                client,
                generationContext,
                relationIdSupplier,
                categorySupplier,
                logicalIdSupplier,
                numOutRelationsSupplier,
                numInRelationsSupplier,
                metadataSupplier);
    }

    private static KnowledgeGraphGenerator<Object> getContextEntityValueGenerator(
            Client client,
            ElasticConfiguration elasticConfiguration,
            String fieldId,
            GenerationContext generationContext,
            Supplier<String> entityValueIdSupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier,
            List<String> logicalIds) throws IOException {

        String fieldType = generationContext.getContextStatistics().getEntityFieldTypes().get(fieldId);

        String fromContext = generationContext.getContextGenerationConfiguration().getFromContext();

        Supplier<String> logicalIdSupplier = new OrderedSupplier<>(logicalIds);

        Supplier<Integer> fieldNumValuesSupplier =
                new UnifromCachedSupplier<>(
                Stream.ofAll(generationContext.getContextStatistics().getEntityValueCounts().get(fieldId).entrySet())
                        .sortBy(Map.Entry::getValue)
                        .flatMap(entry1 -> Stream.fill(entry1.getValue(), entry1::getKey))
                        .toJavaList());

        Supplier fieldValuesSupplier =
                new ProbabilisticCompositeSupplier<>(
                        new UniformFieldDataSupplier<>(
                                client,
                                "e.value",
                                fieldId,
                                fromContext,
                                elasticConfiguration.getReadSchema().getEntityIndex(),
                                100000),
                        generationContext.getContextGenerationConfiguration().getEntityValueOverlapFactor(),
                        fieldType.equals("stringValue") ?
                                    new FunctionChainSupplier<>(
                                        new UniformFileLinesCachedSupplier(String.format("%s.generation.values.txt", fieldId)), value -> value) :
                                fieldType.equals("intValue") ?
                                    new FunctionChainSupplier<String, Object>(
                                                new UniformFileLinesCachedSupplier(String.format("%s.generation.values.txt", fieldId)),
                                                Integer::parseInt) :
                                        () -> null);


        return new KnowledgeContextEntityValueDataGenerator(
                client,
                generationContext,
                fieldId,
                generationContext.getContextGenerationConfiguration().getToContext(),
                entityValueIdSupplier,
                logicalIdSupplier,
                metadataSupplier,
                fieldNumValuesSupplier,
                fieldValuesSupplier);
    }

    private static KnowledgeGraphGenerator<Object> getGlobalEntityValueGenerator(
            Client client,
            ElasticConfiguration elasticConfiguration,
            String fieldId,
            GenerationContext generationContext,
            Supplier<String> entityValueIdSupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier,
            List<String> logicalIds,
            Set<String> fromContextLogicalIds) throws IOException {

        Supplier<String> logicalIdSupplier = new OrderedSupplier<>(logicalIds);

        Supplier<Integer> fieldNumValuesSupplier =
                new UnifromCachedSupplier<>(
                        Stream.ofAll(generationContext.getContextStatistics().getEntityGlobalValueCounts().get(fieldId).entrySet())
                                .sortBy(Map.Entry::getValue)
                                .flatMap(entry1 -> Stream.fill(entry1.getValue(), entry1::getKey))
                                .toJavaList());

        Supplier fieldValuesSupplier =
                new ProbabilisticCompositeSupplier<>(
                        new UniformCompositeSupplier<>(
                                Stream.ofAll(fromContextLogicalIds).grouped(1000)
                                        .map(ids -> (Supplier<String>) new UniformGlobalFieldDataSupplier<String>(
                                                client,
                                                "e.value",
                                                fieldId,
                                                ids.toJavaList(),
                                                elasticConfiguration.getReadSchema().getEntityIndex(),
                                                100000))
                                        .toJavaList()),
                        generationContext.getContextGenerationConfiguration().getEntityValueOverlapFactor(),
                        new UniformFileLinesCachedSupplier(String.format("%s.generation.values.txt", fieldId))); //TODO

        return new KnowledgeContextEntityValueDataGenerator(
                client,
                generationContext,
                fieldId,
                generationContext.getContextGenerationConfiguration().getToContext(),
                entityValueIdSupplier,
                logicalIdSupplier,
                metadataSupplier,
                fieldNumValuesSupplier,
                fieldValuesSupplier);
    }

    private static KnowledgeGraphGenerator<Object> getContextRelationValueGenerator(
            Client client,
            ElasticConfiguration elasticConfiguration,
            String fieldId,
            GenerationContext generationContext,
            Supplier<String> relationValueIdSupplier,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier,
            List<String> relationIds) throws IOException {

        String fieldType = generationContext.getContextStatistics().getEntityFieldTypes().get(fieldId);

        String fromContext = generationContext.getContextGenerationConfiguration().getFromContext();

        Supplier<String> relationIdSupplier = new OrderedSupplier<>(relationIds);

        Supplier<Integer> fieldNumValuesSupplier =
                new UnifromCachedSupplier<>(
                        Stream.ofAll(generationContext.getContextStatistics().getRelationValueCounts().get(fieldId).entrySet())
                                .sortBy(Map.Entry::getValue)
                                .flatMap(entry1 -> Stream.fill(entry1.getValue(), entry1::getKey))
                                .toJavaList());

        Supplier fieldValuesSupplier =
                new ProbabilisticCompositeSupplier<>(
                        new UniformFieldDataSupplier<>(
                                client,
                                "r.value",
                                fieldId,
                                fromContext,
                                elasticConfiguration.getReadSchema().getRelationIndex(),
                                100000),
                        generationContext.getContextGenerationConfiguration().getEntityValueOverlapFactor(),
                        fieldType.equals("stringValue") ?
                                new FunctionChainSupplier<>(
                                        new UniformFileLinesCachedSupplier(String.format("%s.generation.values.txt", fieldId)), value -> value) :
                                fieldType.equals("intValue") ?
                                        new FunctionChainSupplier<String, Object>(
                                                new UniformFileLinesCachedSupplier(String.format("%s.generation.values.txt", fieldId)),
                                                Integer::parseInt) :
                                        () -> null);


        return new KnowledgeContextRelationValueDataGenerator(
                client,
                generationContext,
                fieldId,
                generationContext.getContextGenerationConfiguration().getToContext(),
                relationValueIdSupplier,
                relationIdSupplier,
                metadataSupplier,
                fieldNumValuesSupplier,
                fieldValuesSupplier);
    }

    private static KnowledgeGraphGenerator<Object> getContextInsightGenerator(
            Client client,
            ElasticConfiguration elasticConfiguration,
            GenerationContext generationContext,
            Supplier<KnowledgeEntityBase.Metadata> metadataSupplier,
            List<String> logicalIds) {

        Supplier<String> logicalIdSupplier = new UnifromCachedSupplier<String>(logicalIds);
        Supplier<String> insightIdSupplier = new FormatIdSupplier("i" + elasticConfiguration.getWriteSchema().getIdFormat(), 1000000, 2000000);

        Supplier<String> contentSupplier = new FunctionChainSupplier<>(
                new IterableSupplier<>(
                        new UniformDataTextSupplier(
                                client,
                                "insight",
                                "content",
                                generationContext.getElasticConfiguration().getReadSchema().getInsightIndex(),
                                100000),
                        new UnifromBoundedIntSupplier(8, 20)),
                words -> String.join(" ", Stream.ofAll(words).toArray()));

        Supplier<Integer> numEntitiesSupplier =
                new UnifromCachedSupplier<>(
                        Stream.ofAll(generationContext.getContextStatistics().getInsightEntityCounts().entrySet())
                                .sortBy(Map.Entry::getValue)
                                .flatMap(entry1 -> Stream.fill(entry1.getValue(), entry1::getKey))
                                .toJavaList());

        return new KnowledgeContextInsightDataGenerator(
                client,
                generationContext,
                insightIdSupplier,
                contentSupplier,
                logicalIdSupplier,
                numEntitiesSupplier,
                metadataSupplier);
    }


    private static Supplier<KnowledgeEntityBase.Metadata> getCommonMetadataSupplier(Map<Integer, Integer> refCounts, Iterable<String> referenceIds) {
        Supplier<String> nameSupplier = new UnifromCachedSupplier<>(new NameSupplier(4, 12), 1000);
        Supplier<Date> metadataDateSupplier = new UnifromBoundedDateSupplier(
                System.currentTimeMillis() - (long) (1000L * 60L * 60L * 24L * 30L),
                System.currentTimeMillis());
        Supplier<Iterable<String>> refsSupplier = new IterableSupplier<>(
                new UnifromCachedSupplier<>(referenceIds),
                new UnifromCachedSupplier<>(
                        Stream.ofAll(refCounts.entrySet())
                                .sortBy(Map.Entry::getValue)
                                .flatMap(entry -> Stream.fill(entry.getValue(), entry::getKey))
                                .toJavaList()));

        return new MetadataSupplier(
                metadataDateSupplier,
                nameSupplier,
                refsSupplier);
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

    private static Set<String> globalFieldNames = Stream.of("title", "nicknames").toJavaSet();
}
