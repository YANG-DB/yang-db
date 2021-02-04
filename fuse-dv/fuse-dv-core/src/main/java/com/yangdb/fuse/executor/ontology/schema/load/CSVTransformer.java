package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 * #%L
 * fuse-dv-core
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderFactory;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.executor.ontology.DataTransformer;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.Entity;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.Redundant;
import com.yangdb.fuse.model.schema.Relation;
import javaslang.Tuple2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.elasticsearch.client.Client;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static com.yangdb.fuse.executor.elasticsearch.ElasticIndexProviderMappingFactory.*;
import static com.yangdb.fuse.executor.ontology.DataTransformer.Utils.TYPE;
import static com.yangdb.fuse.executor.ontology.DataTransformer.Utils.sdf;
import static com.yangdb.fuse.executor.ontology.schema.load.DataLoaderUtils.*;

public class CSVTransformer implements DataTransformer<DataTransformerContext, CSVTransformer.CsvElement> {
    private final Ontology.Accessor accessor;
    private final IndexProviderFactory indexProviderFactory;
    private final OntologyProvider ontologyProvider;
    private IndexProvider indexProvider;
    private final RawSchema schema;
    private final IdGeneratorDriver<Range> idGenerator;
    private final Client client;
    private final ObjectMapper mapper;

    @Inject
    public CSVTransformer(Config config, OntologyProvider ontologyProvider, IndexProviderFactory indexProviderFactory, RawSchema schema, IdGeneratorDriver<Range> idGenerator, Client client) {
        String assembly = config.getString("assembly");
        this.ontologyProvider = ontologyProvider;
        this.indexProviderFactory = indexProviderFactory;
        this.accessor = new Ontology.Accessor(ontologyProvider.get(assembly).orElseThrow(
                () -> new FuseError.FuseErrorException(new FuseError("No Ontology present for assembly", "No Ontology present for assembly" + assembly))));

        //if no index provider found with assembly name - generate default one accoring to ontology and simple Static Index Partitioning strategy
        this.indexProvider = indexProviderFactory.get(assembly).orElseGet(() -> IndexProvider.Builder.generate(accessor.get()));

        this.schema = schema;
        this.idGenerator = idGenerator;
        this.client = client;
        this.mapper = new ObjectMapper();

    }

    @Override
    public DataTransformerContext transform(String ontology, CsvElement data, GraphDataLoader.Directive directive) {
        //match requested ontology
        Ontology onto = ontologyProvider.get(ontology).orElseThrow(
                () -> new FuseError.FuseErrorException(new FuseError("No Ontology present for assembly", "No Ontology present for assembly" + ontology)));
        //user specifically requested ontology
        IndexProvider indexProvider = indexProviderFactory.get(onto.getOnt())
                .orElseGet(() ->  IndexProvider.Builder.generate(onto));

        return _transform(new Ontology.Accessor(onto),indexProvider, data);
    }

    @Override
    public DataTransformerContext transform(CSVTransformer.CsvElement data, GraphDataLoader.Directive directive) {
        return _transform(this.accessor, this.indexProvider, data);
    }

    private DataTransformerContext _transform(Ontology.Accessor accessor, IndexProvider indexProvider, CsvElement data) {
        DataTransformerContext context = new DataTransformerContext(mapper);
        try (CSVParser csvRecords = new CSVParser(data.content(), CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim())) {

            List<CSVRecord> dataRecords = csvRecords.getRecords();
            if (accessor.entity(data.label()).isPresent()) {
                EntityType entityType = accessor.entity$(data.label());
                dataRecords.forEach(r -> context.withEntity(translate(accessor,indexProvider,context, entityType, r.toMap())));
            } else if (accessor.relation(data.label()).isPresent()) {
                RelationshipType relType = accessor.relation$(data.label());
                //store both sides
                dataRecords.forEach(r -> context.withRelation(translate(accessor,indexProvider,context, relType, r.toMap(), "in")));
                dataRecords.forEach(r -> context.withRelation(translate(accessor,indexProvider,context, relType, r.toMap(), "out")));
            }
        } catch (IOException e) {
            throw new FuseError.FuseErrorException("Error while building graph Element from csv row ", e);
        }
        return context;
    }

    /**
     * transform single entity-row into document
     *
     * @param context
     * @param entityType
     * @param node
     * @return
     */
    private DocumentBuilder translate(Ontology.Accessor accessor,IndexProvider indexProvider,DataTransformerContext context, EntityType entityType, Map<String, String> node) {
        try {
            ObjectNode element = mapper.createObjectNode();
            Entity entity = indexProvider.getEntity(entityType.geteType())
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("CSV Transformation Error", "No matching node found with label " + entityType.geteType())));
            //put id (take according to ontology id field mapping or generate UUID of none found)
            StringJoiner joiner = new StringJoiner(".");
            entityType.getIdField().forEach(field->joiner.add(node.getOrDefault(field, UUID.randomUUID().toString())));
            //put classifiers
            element.put(entityType.idFieldName(), joiner.toString());
            element.put(TYPE, entity.getType());

            //populate fields
            populateMetadataFields(accessor,context, node, entity, element);
            populatePropertyFields(accessor,context, node, entity, element);
            return new DocumentBuilder(element, joiner.toString(), entity.getType(), Optional.empty());
        } catch (FuseError.FuseErrorException e) {
            return new DocumentBuilder(e.getError());
        }
    }

    /**
     * transform single relation-row into documents (including sideA / sideB proxies)
     *
     * @param context
     * @param relType
     * @param node
     * @return
     */
    private DocumentBuilder translate(Ontology.Accessor accessor,IndexProvider indexProvider,DataTransformerContext context, RelationshipType relType, Map<String, String> node, String direction) {
        try {
            ObjectNode element = mapper.createObjectNode();
            Relation relation = indexProvider.getRelation(relType.getrType())
                    .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("CSV Transformation Error", "No matching node found with label " + relType.getrType())));
            //put id (take according to ontology id field mapping or generate UUID of none found)
            StringJoiner joiner = new StringJoiner(".");
            relType.getIdField().forEach(field->joiner.add(node.getOrDefault(field, UUID.randomUUID().toString())));
            //put classifiers
            String id = String.format("%s.%s", joiner.toString(), direction);
            //put classifiers
            element.put(relType.idFieldName(), id);
            element.put(TYPE, relation.getType());
            element.put(DIRECTION, direction);

            //populate fields
            populateMetadataFields(accessor,context, node, relation, element);

            populatePropertyFields(accessor,indexProvider,context, node, relation, element, direction);

            //partition field in case of none static partitioning index
            Optional<Tuple2<String, String>> partition = Optional.empty();

            //in case of a partition field - set in the document builder
            String field = relation.getProps().getPartitionField();
            if (field != null)
                partition = Optional.of(new Tuple2<>(field, parseValue(accessor.property$(field).getType(), node.get(field), sdf).toString()));

            return new DocumentBuilder(element, id, relation.getType(), Optional.empty(), partition);
        } catch (FuseError.FuseErrorException e) {
            return new DocumentBuilder(e.getError());
        }
    }


    /**
     * Relation metadata populator
     *
     * @param context
     * @param element
     */
    private void populateMetadataFields(Ontology.Accessor accessor,DataTransformerContext context, Map<String, String> node, Relation relation, ObjectNode element) {
        node.entrySet()
                .stream()
                .filter(m -> accessor.$relation$(relation.getType()).containsMetadata(m.getKey()))
                .filter(m -> validateValue(accessor.property$(m.getKey()).getType(), m.getValue(), sdf))
                .forEach(m -> parseAndSetValue(m.getKey(),element,accessor.property$(m.getKey()).getType(), m.getValue(),sdf));
    }


    /**
     * Relation PropertyFields populator
     *
     * @param context
     * @param element
     * @param direction
     */
    private void populatePropertyFields(Ontology.Accessor accessor,IndexProvider indexProvider,DataTransformerContext context, Map<String, String> node, Relation relation, ObjectNode element, String direction) {
        populateRelationFields(accessor,node, relation, element);

        RelationshipType relationshipType = accessor.$relation$(relation.getType());
        //populate each pair
        switch (direction) {
            case "out":
                //for each pair do:
                relationshipType.getePairs().forEach(pair -> {
                    element.put(ENTITY_A, populateSide(indexProvider,ENTITY_A, context, node.get(pair.getSideAIdField()), pair.geteTypeA(), relation, node));
                    //populate redundant fields B
                    element.put(ENTITY_B, populateSide(indexProvider,ENTITY_B, context, node.get(pair.getSideBIdField()), pair.geteTypeB(), relation, node));
                });
                break;
            case "in":
                //for each pair do:
                relationshipType.getePairs().forEach(pair -> {
                    element.put(ENTITY_B, populateSide(indexProvider,ENTITY_A, context, node.get(pair.getSideAIdField()), pair.geteTypeA(), relation, node));
                    //populate redundant fields B
                    element.put(ENTITY_A, populateSide(indexProvider,ENTITY_B, context, node.get(pair.getSideBIdField()), pair.geteTypeB(), relation, node));
                });
                break;
        }
    }

    /**
     * populate edge redundant side - as a json object
     *
     * @param side
     * @param context
     * @param sideId
     * @param relation
     * @return
     */
    private ObjectNode populateSide(IndexProvider indexProvider,String side, DataTransformerContext context, String sideId, String sideType, Relation relation, Map<String, String> node) {
        ObjectNode entitySide = mapper.createObjectNode();

        //get type (label) of the side node
        Entity entity = indexProvider.getEntity(sideType)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("Logical Graph Transformation Error", "No matching node found with label " + sideType)));

        //put classifiers
        entitySide.put(ID, sideId);
        entitySide.put(TYPE, entity.getType());

        List<Redundant> redundant = relation.getRedundant(side);
        redundant.forEach(r -> populateRedundantField(r, node, side, entitySide));
        return entitySide;
    }

    private void populateRedundantField(Redundant redundant, Map<String, String> node, String sideName, ObjectNode map) {
        String key = String.format("%s.%s", sideName, redundant.getRedundantName());
        if (node.containsKey(key))
            map.put(redundant.getRedundantName(), parseValue(redundant.getType(), node.get(key), sdf).toString());
    }


    /**
     * Entity metadata populator
     *
     * @param context
     * @param element
     */
    private void populateMetadataFields(Ontology.Accessor accessor,DataTransformerContext context, Map<String, String> node, Entity entity, ObjectNode element) {
        //manage regular metadata properties
        node.entrySet()
                .stream()
                .filter(m -> accessor.$entity$(entity.getType()).containsMetadata(m.getKey()))
                .filter(m -> !accessor.enumeratedType(accessor.property$(m.getKey()).getType()).isPresent())
                .filter(m -> validateValue(accessor.property$(m.getKey()).getType(), m.getValue(), sdf))
                    .forEach(m -> element.put(accessor.property$(m.getKey()).getpType(),
                            parseValue(accessor.property$(m.getKey()).getType(), m.getValue(), sdf).toString()));
        //manage enums
        node.entrySet()
                .stream()
                .filter(m -> accessor.$entity$(entity.getType()).containsMetadata(m.getKey()))
                .filter(m -> accessor.enumeratedType(accessor.property$(m.getKey()).getType()).isPresent())
                .filter(m -> validateValue("int", m.getValue(), sdf))
                .forEach(m -> element.put(accessor.property$(m.getKey()).getpType(),
                        parseValue("int", m.getValue(), sdf).toString()));
    }


    /**
     * Entity PropertyFields populator
     *
     * @param context
     * @param element
     */
    private void populatePropertyFields(Ontology.Accessor accessor,DataTransformerContext context, Map<String, String> node, Entity entity, ObjectNode element) {
        //manage regular properties
        node.entrySet()
                .stream()
                .filter(m -> accessor.$entity$(entity.getType()).containsProperty(m.getKey()))
                .filter(m -> !accessor.enumeratedType(accessor.property$(m.getKey()).getType()).isPresent())
                .filter(m -> validateValue(accessor.property$(m.getKey()).getType(), m.getValue(), sdf))
                    .forEach(m -> element.put(accessor.property$(m.getKey()).getpType(),
                            parseValue(accessor.property$(m.getKey()).getType(), m.getValue(), sdf).toString()));
        //manage enums
        node.entrySet()
                .stream()
                .filter(m -> accessor.$entity$(entity.getType()).containsProperty(m.getKey()))
                .filter(m -> accessor.enumeratedType(accessor.property$(m.getKey()).getType()).isPresent())
                .filter(m -> validateValue("int", m.getValue(), sdf))
                .forEach(m -> element.put(accessor.property$(m.getKey()).getpType(),
                        parseValue("int", m.getValue(), sdf).toString()));
    }

    /**
     * relation fields populator
     * @param node
     * @param relation
     * @param element
     */
    private void populateRelationFields(Ontology.Accessor accessor,Map<String, String> node, Relation relation, ObjectNode element) {
        //manage regular properties
        node.entrySet()
                .stream()
                .filter(m -> accessor.$relation$(relation.getType()).containsProperty(m.getKey()))
                .filter(m -> !accessor.enumeratedType(accessor.property$(m.getKey()).getType()).isPresent())
                .filter(m -> validateValue(accessor.property$(m.getKey()).getType(), m.getValue(), sdf))
                .forEach(m -> parseAndSetValue(m.getKey(), element,accessor.property$(m.getKey()).getType(), m.getValue(),sdf));

        //manage enums
        node.entrySet()
                .stream()
                .filter(m -> accessor.$relation$(relation.getType()).containsProperty(m.getKey()))
                .filter(m -> accessor.enumeratedType(accessor.property$(m.getKey()).getType()).isPresent())
                .filter(m -> validateValue("int", m.getValue(), sdf))
                .forEach(m -> parseAndSetValue(m.getKey(), element,"int", m.getValue(),sdf));
    }

    /**
     * Csv Graph element Container
     */
    public interface CsvElement {
        String label();

        /**
         * todo - calculate the type according to the ontology
         * @return
         */
        String type();

        Reader content();
    }


}
