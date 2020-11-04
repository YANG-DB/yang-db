package com.yangdb.fuse.assembly.knowledge.load;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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

import com.google.inject.Inject;
import com.yangdb.fuse.assembly.knowledge.load.builder.*;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.DataTransformer;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.executor.ontology.schema.load.CSVTransformer;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.ontology.transformer.OntologyTransformer;
import com.yangdb.fuse.model.ontology.transformer.TransformerEntityType;
import com.yangdb.fuse.model.ontology.transformer.TransformerRelationType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.yangdb.fuse.assembly.knowledge.load.KnowledgeLoaderUtils.*;
import static java.util.regex.Pattern.matches;

public class KnowledgeCSVTransformer implements DataTransformer<KnowledgeContext, CSVTransformer.CsvElement> {
    public static final String ID = "id";
    public static final String SOURCE = "source";
    public static final String TARGET = "target";

    private static Map<String, Range.StatefulRange> ranges = new HashMap<>();

    private OntologyTransformer transformer;
    private IdGeneratorDriver<Range> idGenerator;
    private StoreAccessor accessor;
    private OntologyProvider ontologyProvider;
    private RawSchema schema;
    private KnowledgeWriterContext writerContext;

    @Inject
    public KnowledgeCSVTransformer(OntologyProvider ontologyProvider,RawSchema schema, OntologyTransformer transformer, IdGeneratorDriver<Range> idGenerator, StoreAccessor client) {
        this.ontologyProvider = ontologyProvider;
        this.schema = schema;
        this.transformer = transformer;
        this.idGenerator = idGenerator;
        this.accessor = client;
    }

    @Override
    public KnowledgeContext transform(CSVTransformer.CsvElement data, GraphDataLoader.Directive directive) {
        KnowledgeContext context = new KnowledgeContext();
        this.writerContext = new KnowledgeWriterContext(context);
        try (CSVParser csvRecords = new CSVParser(data.content(), CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim())) {

            List<CSVRecord> dataRecords = csvRecords.getRecords();

            switch (data.type()) {
                case "Entity":
                    Optional<TransformerEntityType> entityType = transformer.getEntityTypes().stream()
                            .filter(e -> matches(e.getPattern(), data.label())).findFirst();
                    if (!entityType.isPresent()) {
                        context.failed("Entity type not matched", data.label());
                        return context;
                    }

                    dataRecords.forEach(rec -> {
                        try {
                            context.add(createEntity(accessor, schema,
                                            context, writerContext,
                                            //sequence types
                                            getRange(ranges, idGenerator, EntityBuilder.type),
                                            getRange(ranges, idGenerator, ValueBuilder.type),
                                            //schematic type
                                            entityType.get(),
                                            //record params
                                            rec.get(ID), data.label(), rec.toMap(), rec.toMap(),
                                            directive));
                        } catch (Throwable err) {
                            //error while creating edge
                            context.failed("Vertex creation failed", err.getMessage());
                        }
                    });
                    break;
                case "Relation":
                    Optional<TransformerRelationType> relationType = transformer.getRelationTypes().stream()
                            .filter(e -> matches(e.getPattern(), data.label())).findFirst();
                    if (!relationType.isPresent()) {
                        context.failed("Relationship type not matched", data.label());
                        return context;
                    }

                    dataRecords.forEach(rec -> {
                        try {
                            //id generation for edges without ids: source_label_target
                            String id = rec.isSet(ID) ? rec.get(ID) : String.format("%s_%s_%s",rec.get(SOURCE),data.label(),rec.get(TARGET)) ;
                            context.add(createEdge(accessor,schema,
                                    context,writerContext,
                                    getRange(ranges,idGenerator, RelationBuilder.type),
                                    getRange(ranges,idGenerator,RvalueBuilder.type),
                                    relationType.get(),
                                    id, data.label(),rec.get(SOURCE),rec.get(TARGET), rec.toMap(), rec.toMap(),
                                    directive));
                        } catch (Throwable err) {
                            //error while creating edge
                            context.failed("Edge creation failed", err.getMessage());
                        }
                    });
                    break;
                case FileBuilder.type:
                    break;
                case InsightBuilder.type:
                    break;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return context;
    }
}