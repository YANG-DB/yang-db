package com.yangdb.fuse.graphql.translator.wiring;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.Property;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.query.quant.QuantType;
import graphql.Internal;
import graphql.Scalars;
import graphql.TypeResolutionEnvironment;
import graphql.execution.ResultPath;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.*;
import graphql.schema.idl.*;
import javaslang.Tuple2;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * A wiring factory that will echo back the objects defined.  That is if you have a field called
 * "name" of type String, it will echo back the value "name".  This is ONLY useful for mocking out a
 * schema that do don't want to actually execute properly.
 */
@Internal
public class TraversalWiringFactory implements WiringFactory {

    public static final String WHERE = "where";
    public static final String QUERY = "query";
    //wiring jackson mapper
    public static final ObjectMapper mapper = new ObjectMapper();

    private Query.Builder builder;
    private Ontology.Accessor accessor;
    private Map<String, Integer> pathContext;
    /** the value of each field visited (by path without list indexes) */
    private final Map<String, Object> fetched = new HashMap<>();

    /** a value of an abstract field: an object of the given type */
    private static final class Typed {
        private final String type;

        private Typed(String type) {
            this.type = type;
        }
    }

    public static RuntimeWiring newEchoingWiring(Ontology ontology, Query.Builder queryBuilder) {
        return newEchoingWiring(x -> {
        }, ontology, queryBuilder);
    }

    public static RuntimeWiring newEchoingWiring(Consumer<RuntimeWiring.Builder> builderConsumer, Ontology ontology, Query.Builder queryBuilder) {
        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();
        builderConsumer.accept(builder);
        return builder
                .wiringFactory(new TraversalWiringFactory(ontology, queryBuilder))
                .build();
    }

    /**
     * @param ontology the ontology the query is translated against
     * @param builder  collects the translated query
     */
    public TraversalWiringFactory(Ontology ontology, Query.Builder builder) {
        this.builder = builder;
        this.accessor = new Ontology.Accessor(ontology);
        this.pathContext = new HashMap<>();
    }

    public Query.Builder getBuilder() {
        return builder;
    }

    @Override
    public boolean providesTypeResolver(InterfaceWiringEnvironment environment) {
        return true;
    }

    @Override
    public TypeResolver getTypeResolver(InterfaceWiringEnvironment environment) {
        return TraversalWiringFactory::resolveType;
    }

    @Override
    public boolean providesTypeResolver(UnionWiringEnvironment environment) {
        return true;
    }

    @Override
    public TypeResolver getTypeResolver(UnionWiringEnvironment environment) {
        return TraversalWiringFactory::resolveType;
    }

    /**
     * an abstract (interface / union) field returns one {@link Typed} value per type it was queried for, so the
     * fields of each type's fragment are visited
     */
    private static GraphQLObjectType resolveType(TypeResolutionEnvironment env) {
        // the schema being executed: it is generated per query, so it is not known when the wiring is built
        return env.getSchema().getObjectType(((Typed) env.getObject()).type);
    }

    @Override
    public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
        return env -> {
            // an abstract field returns one value per type, the fields they share are visited once
            String key = key(env.getExecutionStepInfo().getPath());
            if (!fetched.containsKey(key)) {
                fetched.put(key, getObject(env, env.getFieldType()));
            }
            Object value = fetched.get(key);
            if (env.getFieldType() instanceof GraphQLList) {
                return value instanceof List ? value : Arrays.asList(value);
            }
            return value;
        };
    }

    /** a path without its list indexes: the elements of a list field are all the same query element */
    private static String key(ResultPath path) {
        return path.toString().replaceAll("\\[\\d+]", "");
    }

    private Object getObject(DataFetchingEnvironment env, GraphQLType fieldType) throws IOException {
        fieldType = extractConcreteFieldType(fieldType);
        // in parent is of type vertex and current query element not quant -> add quant
        ExecutionStepInfo parent = env.getExecutionStepInfo().getParent();
        if (parent.getFieldDefinition() != null) {
            String parentPath = key(parent.getPath());
            if (isParentObjectType(parent.getFieldDefinition()) &&
                    //assume a path was already entered
                    pathContext.containsKey(parentPath) &&
                    //validate no quant related to the
                    !(builder.current(pathContext.get(parentPath)) instanceof QuantBase)) {
                //todo add quant
                if(!(builder.current() instanceof QuantBase)) {
                    builder.quant(QuantType.all);
                }
                pathContext.put(parentPath, builder.currentIndex());
            }
        }

        if (fieldType instanceof GraphQLObjectType) {
            GraphQLObjectType type = (GraphQLObjectType) fieldType;
            GraphQLObjectType parentType = (GraphQLObjectType) parent.getType();
            //add the start query element
            if (parentType.getName().toLowerCase().equals(QUERY)) {
                builder.start();
            }
            //populate vertex or relation
            List<EntityType> realType = populateGraphObject(env, Collections.singletonList(type.getName()), type.getName());
            addWhereClause(env,realType);
//            return fakeObjectValue(accessor, builder, (GraphQLObjectType) fieldType);
            return new Object();
        } else if (fieldType instanceof GraphQLInterfaceType || fieldType instanceof GraphQLUnionType) {
            String name = ((GraphQLNamedOutputType) fieldType).getName();
            List<String> types = queriedTypes(env, (GraphQLNamedOutputType) fieldType);
            // an interface that is an entity type itself (imported SDL: Character) is the side B as is; otherwise the
            // relationship leads to several types: the side B is an entity of any of the types queried
            List<EntityType> realType = populateGraphObject(env,
                    accessor.entity(name).isPresent() ? Collections.singletonList(name) : types, name);
            addWhereClause(env, realType);
            return types.stream().map(Typed::new).collect(Collectors.toList());
        }
        //populate values
        if (fieldType instanceof GraphQLScalarType) {
            String name = populateGraphValue(env);
            return fakeScalarValue(name, (GraphQLScalarType) fieldType);
        } else if (fieldType instanceof GraphQLEnumType) {
            String name = populateGraphEnum(env);
            return fakeEnumValue(name, (GraphQLEnumType) fieldType);
        }

        return new Object();
    }

    /**
     *
     * @param env
     * @param realType
     * @throws IOException
     */
    private void addWhereClause(DataFetchingEnvironment env, List<EntityType> realType) throws IOException {
        //arguments
        if(!realType.isEmpty() && env.getArguments().containsKey(WHERE)) {
            Object argument = env.getArguments().get(WHERE);
            InputTypeWhereClause whereClause = mapper.readValue(mapper.writeValueAsString(argument), InputTypeWhereClause.class);
            //verify fields exist within (one of) the entity types
            List<InputTypeConstraint> nonFoundFields = whereClause.getConstraints().stream()
                    .filter(c -> realType.stream().noneMatch(type -> type.containsProperty(c.getOperand())))
                    .collect(Collectors.toList());

            if(!nonFoundFields.isEmpty())
                throw new IllegalArgumentException("Fields "+nonFoundFields +" are not a part of the queried entity "
                        +realType.stream().map(EntityType::getName).collect(Collectors.joining(" | ")));

            //build the where clause query
            List<Tuple2<String, Optional<Constraint>>> constraints = whereClause.getConstraints().stream()
                    .map(c -> new Tuple2<>(c.getOperand(),
                            Optional.of(new Constraint(ConstraintOp.valueOf(c.getOperator()), c.getExpression()))))
                    .collect(Collectors.toList());


            //if no quant exists - add one
            if(!getBuilder().pop(eBase -> eBase instanceof QuantBase).isPresent()) {
                builder.quant(QuantType.all);
            }

            //add to group
            builder.ePropGroup(constraints,asQuantType(whereClause.getOperator()));

        }
    }

    private QuantType asQuantType(InputTypeWhereClause.WhereOperator operator) {
        switch (operator) {
            case AND: return QuantType.all;
            case OR: return QuantType.some;
            default:return QuantType.all;
        }
    }

    private boolean isParentObjectType(GraphQLFieldDefinition parentField) {
        return (extractConcreteFieldType(parentField.getType()) instanceof GraphQLInterfaceType) ||
                (extractConcreteFieldType(parentField.getType()) instanceof GraphQLUnionType) ||
                (extractConcreteFieldType(parentField.getType()) instanceof GraphQLObjectType);
    }

    /**
     * @param env
     * @return
     */
    private String populateGraphValue(DataFetchingEnvironment env) {
        //pop to the correct index according to path
        if (pathContext.containsKey(key(env.getExecutionStepInfo().getParent().getPath()))) {
            builder.currentIndex(pathContext.get(key(env.getExecutionStepInfo().getParent().getPath())));
        }
        String name = env.getField().getName();
        Property property = accessor.property$(name);
        builder.eProp(property.getpType());
        return name;
    }

    /**
     * @param env
     * @return
     */
    private String populateGraphEnum(DataFetchingEnvironment env) {
        //pop to the correct index according to path
        if (pathContext.containsKey(key(env.getExecutionStepInfo().getParent().getPath()))) {
            builder.currentIndex(pathContext.get(key(env.getExecutionStepInfo().getParent().getPath())));
        }
        Property enumProp = accessor.property$(env.getField().getName());
        builder.eProp(env.getField().getName());
        //select first value since no matter which value selected for mock data
        return accessor.enumeratedType$(enumProp.getType()).getValues().get(0).getName();
    }

    /**
     * get concrete friend type
     *
     * @param fieldType
     * @return
     */
    private GraphQLType extractConcreteFieldType(GraphQLType fieldType) {
        //list to wrapping type
        if (fieldType instanceof GraphQLList) {
            //inner field type
            fieldType = ((GraphQLList) fieldType).getWrappedType();
        }
        //non-null to wrapping type
        if (fieldType instanceof GraphQLNonNull) {
            //inner field type
            fieldType = ((GraphQLNonNull) fieldType).getWrappedType();
            //list to wrapping type
            if (fieldType instanceof GraphQLList) {
                //inner field type
                fieldType = ((GraphQLList) fieldType).getWrappedType();
            }
        }
        return fieldType;
    }

    /**
     * the types an abstract (interface / union) field is queried for: those its selected fields belong to (an inline
     * fragment narrows them to its type), or all of its types
     */
    private static List<String> queriedTypes(DataFetchingEnvironment env, GraphQLNamedOutputType abstractType) {
        List<String> all = (abstractType instanceof GraphQLUnionType
                ? ((GraphQLUnionType) abstractType).getTypes().stream().map(GraphQLNamedType::getName)
                : env.getGraphQLSchema().getImplementations((GraphQLInterfaceType) abstractType).stream().map(GraphQLNamedType::getName))
                .collect(Collectors.toList());
        Set<String> selected = env.getSelectionSet().getImmediateFields().stream()
                .filter(field -> !field.getName().startsWith("__"))
                .flatMap(field -> field.getObjectTypeNames().stream())
                .collect(Collectors.toSet());
        List<String> queried = all.stream().filter(selected::contains).collect(Collectors.toList());
        return queried.isEmpty() ? all : queried;
    }

    /**
     * @param types    the entity types of the element: one, or several for the side B of a relationship
     * @param tag      the element's tag
     */
    private List<EntityType> populateGraphObject(DataFetchingEnvironment env, List<String> types, String tag) {
        //pop to the correct index according to path
        if (pathContext.containsKey(key(env.getExecutionStepInfo().getParent().getPath()))) {
            builder.currentIndex(pathContext.get(key(env.getExecutionStepInfo().getParent().getPath())));
        }

        if (accessor.relation(env.getField().getName()).isPresent()) {
            //relation
            RelationshipType relationshipType = accessor.relation$(env.getField().getName());
            builder.rel(relationshipType.getrType(), Rel.Direction.R, env.getField().getName());
            //right after will be the vertex
            List<EntityType> entityTypes = types.stream().map(accessor::entity$).collect(Collectors.toList());
            if (entityTypes.size() == 1) {
                builder.eType(entityTypes.get(0).geteType(), tag);
            } else {
                builder.unTyped(entityTypes.stream().map(EntityType::geteType)
                        .collect(Collectors.toCollection(LinkedHashSet::new)), tag);
            }
            pathContext.put(key(env.getExecutionStepInfo().getPath()), builder.currentIndex());
            return entityTypes;
        } else if (types.size() == 1 && accessor.entity(types.get(0)).isPresent()) {
            EntityType entityType = accessor.entity$(types.get(0));
            builder.eType(entityType.geteType(), tag);
            pathContext.put(key(env.getExecutionStepInfo().getPath()), builder.currentIndex());
            return Collections.singletonList(entityType);
        }
        return Collections.emptyList();
    }


    @Deprecated
    private  Object fakeObjectValue(Ontology.Accessor accessor, Query.Builder builder, GraphQLObjectType fieldType) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (!fieldType.getFieldDefinitions().isEmpty())
            builder.quant(QuantType.all);

        fieldType.getFieldDefinitions().forEach(fldDef -> {
            GraphQLOutputType innerFieldType = fldDef.getType();

            if (innerFieldType instanceof GraphQLNonNull) {
                innerFieldType = (GraphQLOutputType) ((GraphQLNonNull) innerFieldType).getWrappedType();
            }

            if (innerFieldType instanceof GraphQLList) {
                innerFieldType = (GraphQLOutputType) ((GraphQLList) innerFieldType).getWrappedType();
            }

            map.put(fldDef.getName(), getObject(accessor, builder, fieldType, fldDef, innerFieldType));
        });
        return map;
    }

    private  Object getObject(Ontology.Accessor accessor, Query.Builder builder, GraphQLObjectType fieldType, GraphQLFieldDefinition fldDef, GraphQLOutputType innerFieldType) {
        if (innerFieldType instanceof GraphQLObjectType) {
            RelationshipType relType = accessor.relation$(fldDef.getName());
            builder.rel(relType.getrType(), Rel.Direction.R, fieldType.getName());
            return new Object();
        } else if (innerFieldType instanceof GraphQLInterfaceType) {
            RelationshipType relType = accessor.relation$(fldDef.getName());
            builder.rel(relType.getrType(), Rel.Direction.R, fieldType.getName());
            //select first implementing concrete class since ....
            return new Object();
        } else if (innerFieldType instanceof GraphQLScalarType) {
            populateGraphValue(accessor, builder, fldDef);
            return fakeScalarValue(fldDef.getName(), (GraphQLScalarType) innerFieldType);
        } else if (innerFieldType instanceof GraphQLEnumType) {
            populateGraphValue(accessor, builder, fldDef);
            return fakeEnumValue(fldDef.getName(), (GraphQLEnumType) innerFieldType);
        }
        return null;
    }

    private  void populateGraphValue(Ontology.Accessor accessor, Query.Builder builder, GraphQLFieldDefinition fldDef) {
        Property property = accessor.property$(fldDef.getName());
        builder.eProp(property.getpType());
    }


    private  Object fakeScalarValue(String fieldName, GraphQLScalarType scalarType) {
        if (scalarType.equals(Scalars.GraphQLString)) {
            return fieldName;
        } else if (scalarType.equals(Scalars.GraphQLBoolean)) {
            return true;
        } else if (scalarType.equals(Scalars.GraphQLInt)) {
            return 1;
        } else if (scalarType.equals(Scalars.GraphQLFloat)) {
            return 1.0;
        } else if (scalarType.equals(Scalars.GraphQLID)) {
            return "id_" + fieldName;
        } else if (scalarType.getName().equals("Long")) {
            return 1L;
        } else if (scalarType.getName().equals("BigDecimal")) {
            return new BigDecimal(1.0);
        } else if (scalarType.getName().equals("BigInteger")) {
            return new BigInteger("1");
        } else if (scalarType.getName().equals("Byte")) {
            return Byte.valueOf("1");
        } else if (scalarType.getName().equals("Short")) {
            return Short.valueOf("1");
        } else {
            return null;
        }
    }

    private  Object fakeEnumValue(String fieldName, GraphQLEnumType enumType) {
        return fieldName;
    }

    public  GraphQLScalarType fakeScalar(String name) {
        return GraphQLScalarType.newScalar().name(name).description(name).coercing(new Coercing() {
            @Override
            public Object serialize(Object dataFetcherResult) {
                return dataFetcherResult;
            }

            @Override
            public Object parseValue(Object input) {
                return input;
            }

            @Override
            public Object parseLiteral(Object input) {
                return input;
            }
        }).build();
    }
}


