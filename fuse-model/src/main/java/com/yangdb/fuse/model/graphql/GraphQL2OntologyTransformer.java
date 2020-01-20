package com.yangdb.fuse.model.graphql;

import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.Property;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.*;
import graphql.schema.idl.EchoingWiringFactory;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yangdb.fuse.model.ontology.Property.MandatoryProperty.of;

/**
 * API that will transform a GraphQL schema into YangDb ontology schema
 */
public abstract class GraphQL2OntologyTransformer {
    //graph QL reader and schema parts
    static SchemaParser schemaParser = new SchemaParser();
    static TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
    static SchemaGenerator schemaGenerator = new SchemaGenerator();

    static Set<String> languageTypes = new HashSet<>();
    static Set<String> objectTypes = new HashSet<>();
    static Set<Property> properties = new HashSet<>();

    static {
        //exclusion of build-in graphQL types
        languageTypes.add("QueryType");
    }

    /**
     * API that will transform a GraphQL schema into YangDb ontology schema
     * @param graphQL
     * @return
     */
    public static Ontology transform(InputStream graphQL) {
        // each registry is merged into the main registry
        TypeDefinitionRegistry parse = schemaParser.parse(graphQL);
        typeRegistry.merge(parse);
        //create schema
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(
                SchemaGenerator.Options.defaultOptions().enforceSchemaDirectives(false),
                typeRegistry,
                EchoingWiringFactory.newEchoingWiring());

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

    private static void populateObjectTypes(GraphQLSchema graphQLSchema) {
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

    private static List<Property> populateProperties(List<GraphQLFieldDefinition> fieldDefinitions) {
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
     * populate property type according to entities
     *
     * @param type
     * @param fieldName
     * @return
     */
    private static Optional<Property> createProperty(Type type, String fieldName) {
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
                return of(Optional.of(new Property(fieldName, fieldName, ((TypeName) rawType).getName())));
            }

            if (rawType instanceof ListType) {
                return of(createProperty(((ListType) rawType).getType(), fieldName));
            }
        }

        return Optional.empty();
    }

    /**
     * generate interface entity types
     * @param graphQLSchema
     * @param context
     * @return
     */
    static Ontology.OntologyBuilder interfaces(GraphQLSchema graphQLSchema, Ontology.OntologyBuilder context) {
        List<EntityType> collect = graphQLSchema.getAllTypesAsList().stream()
                .filter(p -> GraphQLInterfaceType.class.isAssignableFrom(p.getClass()))
                .map(ifc -> createEntity(ifc.getName(),((GraphQLInterfaceType) ifc).getFieldDefinitions()))
                .collect(Collectors.toList());
        return context.addEntityTypes(collect);
    }

    /**
     * generate concrete entity types
     * @param graphQLSchema
     * @param context
     * @return
     */
    static Ontology.OntologyBuilder entities(GraphQLSchema graphQLSchema, Ontology.OntologyBuilder context) {
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
     * @return
     */
    private static EntityType createEntity(String name, List<GraphQLFieldDefinition> fields) {
        List<Property> properties = populateProperties(fields);

        EntityType.Builder builder = EntityType.Builder.get();
        builder.withName(name).withEType(name);
        builder.withProperties(properties.stream().map(Property::getName).collect(Collectors.toList()));
        builder.withMandatory(properties.stream().filter(p -> p instanceof Property.MandatoryProperty).map(Property::getName).collect(Collectors.toList()));

        return builder.build();
    }

    static Ontology.OntologyBuilder relations(GraphQLSchema graphQLSchema, Ontology.OntologyBuilder context) {
        return context;
    }

    static Ontology.OntologyBuilder properties(GraphQLSchema graphQLSchema, Ontology.OntologyBuilder context) {
        context.withProperties(new ArrayList<>(properties));
        return context;
    }

    static Ontology.OntologyBuilder enums(GraphQLSchema graphQLSchema, Ontology.OntologyBuilder context) {
        return context;
    }

}
