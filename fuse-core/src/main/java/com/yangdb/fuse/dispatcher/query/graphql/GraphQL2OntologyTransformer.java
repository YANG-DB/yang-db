package com.yangdb.fuse.dispatcher.query.graphql;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformer;
import com.yangdb.fuse.model.ontology.*;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.*;
import graphql.schema.idl.EchoingWiringFactory;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import javaslang.Tuple2;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * API that will transform a GraphQL schema into YangDb ontology schema
 */
public class GraphQL2OntologyTransformer implements OntologyTransformer<String,Ontology>, GraphQLSchemaUtils {
    //graph QL reader and schema parts
    private GraphQLSchema graphQLSchema;
    private SchemaParser schemaParser = new SchemaParser();
    private TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
    private SchemaGenerator schemaGenerator = new SchemaGenerator();

    private Set<String> languageTypes = new HashSet<>();
    private Set<String> objectTypes = new HashSet<>();
    private Set<Property> properties = new HashSet<>();

    @Inject
    public GraphQL2OntologyTransformer() {
        languageTypes.add("QueryType");
    }

    @Override
    public Ontology transform(String source) {
        return transform(IOUtils.toInputStream(source));
    }

    /**
     * get the graph QL schema
     *
     * @return
     */
    @Override
    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

    public TypeDefinitionRegistry getTypeRegistry() {
        return typeRegistry;
    }


    /**
     * API that will transform a GraphQL schema into YangDb ontology schema
     *
     * @param graphQL
     * @return
     */
    public Ontology transform(InputStream graphQL) {
        if (graphQLSchema == null) {
            // each registry is merged into the main registry
            TypeDefinitionRegistry parse = schemaParser.parse(new InputStreamReader(graphQL));
            typeRegistry.merge(parse);
            //create schema
            graphQLSchema = schemaGenerator.makeExecutableSchema(
                    SchemaGenerator.Options.defaultOptions().enforceSchemaDirectives(false),
                    typeRegistry,
                    EchoingWiringFactory.newEchoingWiring());
        }
        //create a curated list of names for typed schema elements
        populateObjectTypes(graphQLSchema);

        //transform
        Ontology.OntologyBuilder builder = Ontology.OntologyBuilder.anOntology();
        interfaces(graphQLSchema, builder);
        entities(graphQLSchema, builder);
        relations(graphQLSchema, builder);
        properties(graphQLSchema, builder);
        enums(graphQLSchema, builder);

        return builder.build();

    }

    private void populateObjectTypes(GraphQLSchema graphQLSchema) {
        objectTypes.addAll(Stream.concat(graphQLSchema.getAllTypesAsList().stream()
                        .filter(p -> GraphQLInterfaceType.class.isAssignableFrom(p.getClass()))
                        .map(GraphQLNamedSchemaElement::getName),
                graphQLSchema.getAllTypesAsList().stream()
                        .filter(p -> GraphQLObjectType.class.isAssignableFrom(p.getClass()))
                        .map(GraphQLNamedSchemaElement::getName)
        )
                .filter(p -> !p.startsWith("__"))
                .filter(p -> !languageTypes.contains(p))
                .collect(Collectors.toList()));
    }

    private List<Property> populateProperties(List<GraphQLFieldDefinition> fieldDefinitions) {
        Set<Property> collect = fieldDefinitions.stream()
                .filter(p -> Type.class.isAssignableFrom(p.getDefinition().getType().getClass()))
                .map(p -> createProperty(p.getDefinition().getType(), p.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        //add properties to context properties set
        properties.addAll(collect);
        return new ArrayList<>(collect);
    }


    /**
     * filter entity type according to predicate
     *
     * @param type
     * @param predicate
     * @return
     */
    private Optional<Tuple2<String, TypeName>> filter(Type type, String field, Predicate<TypeName> predicate) {
        //scalar type property
        if ((type instanceof TypeName) && (predicate.test((TypeName) type)))
            return Optional.of(new Tuple2(field, type));

        //list type
        if (type instanceof ListType) {
            return filter(((ListType) type).getType(), field, predicate);
        }
        //non null type - may contain all sub-types (wrapper)
        if (type instanceof NonNullType) {
            Type rawType = ((NonNullType) type).getType();

            //validate only scalars are registered as properties
            if ((rawType instanceof TypeName) && predicate.test((TypeName) rawType)) {
                return Optional.of(new Tuple2(field, rawType));
            }

            if (rawType instanceof ListType) {
                return filter(((ListType) rawType).getType(), field, predicate);
            }
        }

        return Optional.empty();
    }

    /**
     * populate property type according to entities
     *
     * @param type
     * @param fieldName
     * @return
     */
    private Optional<Property> createProperty(Type type, String fieldName) {
        //scalar type property
        if ((type instanceof TypeName) &&
                (!objectTypes.contains(((TypeName) type).getName()))) {
            return Optional.of(new Property(fieldName, fieldName, ((TypeName) type).getName()));
        }

        //list type
        if (type instanceof ListType) {
            return createProperty(((ListType) type).getType(), fieldName);
        }
        //non null type - may contain all sub-types (wrapper)
        if (type instanceof NonNullType) {
            Type rawType = ((NonNullType) type).getType();

            //validate only scalars are registered as properties
            if ((rawType instanceof TypeName) &&
                    (!objectTypes.contains(((TypeName) rawType).getName()))) {
                return Property.MandatoryProperty.of(Optional.of(new Property(fieldName, fieldName, ((TypeName) rawType).getName())));
            }

            if (rawType instanceof ListType) {
                return Property.MandatoryProperty.of(createProperty(((ListType) rawType).getType(), fieldName));
            }
        }

        return Optional.empty();
    }

    /**
     * generate interface entity types
     *
     * @param graphQLSchema
     * @param context
     * @return
     */
    private Ontology.OntologyBuilder interfaces(GraphQLSchema graphQLSchema, Ontology.OntologyBuilder context) {
        List<EntityType> collect = graphQLSchema.getAllTypesAsList().stream()
                .filter(p -> GraphQLInterfaceType.class.isAssignableFrom(p.getClass()))
                .map(ifc -> createEntity(ifc.getName(), ((GraphQLInterfaceType) ifc).getFieldDefinitions()))
                .collect(Collectors.toList());
        return context.addEntityTypes(collect);
    }

    /**
     * generate concrete entity types
     *
     * @param graphQLSchema
     * @param context
     * @return
     */
    private Ontology.OntologyBuilder entities(GraphQLSchema graphQLSchema, Ontology.OntologyBuilder context) {
        List<EntityType> collect = graphQLSchema.getAllTypesAsList().stream()
                .filter(p -> GraphQLObjectType.class.isAssignableFrom(p.getClass()))
                .filter(p -> !languageTypes.contains(p.getName()))
                .filter(p -> !p.getName().startsWith("__"))
                .map(ifc -> createEntity(ifc.getName(), ((GraphQLObjectType) ifc).getFieldDefinitions()))
                .collect(Collectors.toList());
        return context.addEntityTypes(collect);
    }

    /**
     * generate entity (interface) type
     *
     * @return
     */
    private EntityType createEntity(String name, List<GraphQLFieldDefinition> fields) {
        List<Property> properties = populateProperties(fields);

        EntityType.Builder builder = EntityType.Builder.get();
        builder.withName(name).withEType(name);
        builder.withProperties(properties.stream().map(Property::getName).collect(Collectors.toList()));
        builder.withMandatory(properties.stream().filter(p -> p instanceof Property.MandatoryProperty).map(Property::getName).collect(Collectors.toList()));

        return builder.build();
    }

    private Ontology.OntologyBuilder relations(GraphQLSchema graphQLSchema, Ontology.OntologyBuilder context) {
        Map<String, List<RelationshipType>> collect = graphQLSchema.getAllTypesAsList().stream()
                .filter(p -> GraphQLObjectType.class.isAssignableFrom(p.getClass()))
                .filter(p -> !languageTypes.contains(p.getName()))
                .filter(p -> !p.getName().startsWith("__"))
                .map(ifc -> createRelation(ifc.getName(), ((GraphQLObjectType) ifc).getFieldDefinitions()))
                .flatMap(p -> p.stream())
                .collect(Collectors.groupingBy(RelationshipType::getrType));

        //merge e-pairs
        collect.forEach((key, value) -> {
            List<EPair> pairs = value.stream()
                    .flatMap(ep -> ep.getePairs().stream())
                    .collect(Collectors.toList());
            //replace multi relationships with one containing all epairs
            context.addRelationshipType(value.get(0).withEPairs(pairs.toArray(new EPair[0])));
        });
        return context;
    }

    /**
     * @param name
     * @param fieldDefinitions
     * @return
     */
    private List<RelationshipType> createRelation(String name, List<GraphQLFieldDefinition> fieldDefinitions) {
        Set<Tuple2<String, TypeName>> typeNames = fieldDefinitions.stream()
                .filter(p -> Type.class.isAssignableFrom(p.getDefinition().getType().getClass()))
                .map(p -> filter(p.getDefinition().getType(), p.getName(), type -> objectTypes.contains(type.getName())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        //relationships for each entity
        List<RelationshipType> collect = typeNames.stream()
                .map(type -> RelationshipType.Builder.get()
                        //nested objects are directional by nature (nesting dictates the direction)
                        .withDirectional(true)
                        .withName(type._1())
                        .withRType(type._1())
                        .withEPairs(Collections.singletonList(new EPair(name, type._2().getName())))
                        .build())
                .collect(Collectors.toList());

        return collect;
    }

    private Ontology.OntologyBuilder properties(GraphQLSchema graphQLSchema, Ontology.OntologyBuilder context) {
        context.withProperties(new ArrayList<>(properties));
        return context;
    }

    /**
     * @param graphQLSchema
     * @param context
     * @return
     */
    private Ontology.OntologyBuilder enums(GraphQLSchema graphQLSchema, Ontology.OntologyBuilder context) {
        List<EnumeratedType> collect = graphQLSchema.getAllTypesAsList().stream()
                .filter(p -> GraphQLEnumType.class.isAssignableFrom(p.getClass()))
                .filter(p -> !languageTypes.contains(p.getName()))
                .filter(p -> !p.getName().startsWith("__"))
                .map(ifc -> createEnum((GraphQLEnumType) ifc))
                .collect(Collectors.toList());

        context.withEnumeratedTypes(collect);
        return context;
    }

    private EnumeratedType createEnum(GraphQLEnumType ifc) {
        AtomicInteger counter = new AtomicInteger(0);
        EnumeratedType.EnumeratedTypeBuilder builder = EnumeratedType.EnumeratedTypeBuilder.anEnumeratedType();
        builder.withEType(ifc.getName());
        builder.withValues(ifc.getValues().stream()
                .map(v -> new Value(counter.getAndIncrement(), v.getName()))
                .collect(Collectors.toList()));
        return builder.build();
    }

}
