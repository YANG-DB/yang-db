package com.yangdb.fuse.graphql.translator;

/*-
 * #%L
 * fuse-extension-graphql
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

import com.yangdb.fuse.model.ontology.*;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Generates the GraphQL schema (SDL) that queries run against from an ontology, whatever its source (JSON or SDL):
 * <ul>
 *     <li>a root {@code Query} type with one field per entity type, named like the entity with a lower-case first
 *     letter ({@code dragon} for {@code Dragon}, as {@code human} for {@code Human} in SDL schemas), with an optional
 *     {@code where: WhereClause} argument;</li>
 *     <li>one object type per entity type: a field per property (a GraphQL scalar, or an enum for enumerated types) and
 *     a list field per relationship type the entity is side A of, typed by the pair's side B;</li>
 *     <li>when a relationship leads from an entity type to several side-B types (Dragons: {@code Person -Own-> Dragon,
 *     Horse}), its field is typed by an abstract type named {@code <sideA>_<relationship>} ({@code Person_Own}) that
 *     the side-B types belong to: an interface with the fields they all have (same name and type), so they can be
 *     queried without naming a type ({@code Own { name }}), or a union when they have none in common. Inline fragments
 *     ({@code Own { ... on Dragon { color } }}) narrow the field to some of the types;</li>
 *     <li>the enumerated types, and the {@code WhereClause}, {@code Constraint} and {@code WhereOperator} input types.</li>
 * </ul>
 * Names that aren't valid GraphQL names are left out, as are entity types with no fields.
 */
public final class OntologyToGraphQLSdl {
    public static final String QUERY = "Query";

    /** the where-clause types every schema shares (the content of whereSchema.graphql) */
    public static final String WHERE_SCHEMA =
            "enum WhereOperator {\n    AND\n    OR\n}\n\n" +
            "input Constraint {\n    operand: String!\n    operator: String!\n    expression: String\n}\n\n" +
            "input WhereClause {\n    operator: WhereOperator\n    constraints: [Constraint]\n}\n";

    private static final Pattern NAME = Pattern.compile("[_A-Za-z][_0-9A-Za-z]*");
    private static final Set<String> RESERVED = new HashSet<>(Arrays.asList(
            QUERY, "WhereOperator", "Constraint", "WhereClause", "String", "Int", "Float", "Boolean", "ID"));

    private OntologyToGraphQLSdl() {
    }

    /** the root query field of an entity type: its name with a lower-case first letter */
    public static String queryField(String entityName) {
        return Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
    }

    /** the parsed schema of an ontology */
    public static TypeDefinitionRegistry registry(Ontology ontology) {
        return new SchemaParser().parse(sdl(ontology));
    }

    /** the SDL of an ontology */
    public static String sdl(Ontology ontology) {
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);
        Map<String, Property> byPType = new HashMap<>();
        if (ontology.getProperties() != null) {
            ontology.getProperties().forEach(p -> byPType.putIfAbsent(p.getpType(), p));
        }

        // enums
        Map<String, List<String>> enums = new LinkedHashMap<>();
        if (ontology.getEnumeratedTypes() != null) {
            for (EnumeratedType type : ontology.getEnumeratedTypes()) {
                List<String> values = type.getValues() == null ? Collections.emptyList() : type.getValues().stream()
                        .map(Value::getName).filter(OntologyToGraphQLSdl::valid).distinct().collect(Collectors.toList());
                if (valid(type.geteType()) && !values.isEmpty()) {
                    enums.put(type.geteType(), values);
                }
            }
        }

        // entity types
        Map<String, List<String>> types = new LinkedHashMap<>();
        for (EntityType entity : accessor.entities()) {
            String name = entity.getName();
            if (!valid(name) || enums.containsKey(name)) {
                continue;
            }
            Map<String, String> fields = new LinkedHashMap<>();
            for (String pType : entity.getProperties()) {
                Property property = byPType.get(pType);
                if (property != null && valid(property.getName())) {
                    fields.putIfAbsent(property.getName(), scalar(property.getType(), enums.keySet()));
                }
            }
            types.put(name, entityFields(entity, fields));
        }
        // relationship fields, once the entity type names are known
        Map<String, List<String>> out = new LinkedHashMap<>();
        // abstract types of the relationships leading to several types: name -> side-B types
        Map<String, List<String>> abstracts = new LinkedHashMap<>();
        for (EntityType entity : accessor.entities()) {
            String name = entity.getName();
            if (!types.containsKey(name)) {
                continue;
            }
            List<String> fields = new ArrayList<>(types.get(name));
            Set<String> fieldNames = fields.stream().map(f -> f.substring(0, f.indexOf(':'))).collect(Collectors.toSet());
            for (RelationshipType rel : accessor.relations()) {
                if (!valid(rel.getName()) || fieldNames.contains(rel.getName()) || rel.getePairs() == null) {
                    continue;
                }
                List<String> sidesB = rel.getePairs().stream()
                        .filter(pair -> entity.geteType().equals(pair.geteTypeA()))
                        .map(pair -> accessor.$entity(pair.geteTypeB()).map(EntityType::getName)
                                .orElse(accessor.entity(pair.geteTypeB()).map(EntityType::getName).orElse(null)))
                        .filter(Objects::nonNull)
                        .filter(types::containsKey)
                        .distinct()
                        .collect(Collectors.toList());
                if (sidesB.isEmpty()) {
                    continue;
                }
                String fieldType = sidesB.get(0);
                if (sidesB.size() > 1) {
                    fieldType = name + "_" + rel.getName();
                    while (types.containsKey(fieldType) || enums.containsKey(fieldType)
                            || abstracts.containsKey(fieldType) || RESERVED.contains(fieldType)) {
                        fieldType = fieldType + "_";
                    }
                    abstracts.put(fieldType, sidesB);
                }
                fields.add(rel.getName() + ": [" + fieldType + "]");
                fieldNames.add(rel.getName());
            }
            if (!fields.isEmpty()) {
                out.put(name, fields);
            }
        }

        // an interface with the fields the side-B types have in common, or a union when they have none
        Map<String, List<String>> interfaces = new LinkedHashMap<>();
        Map<String, List<String>> unions = new LinkedHashMap<>();
        Map<String, List<String>> implemented = new LinkedHashMap<>();
        abstracts.forEach((name, sidesB) -> {
            List<String> common = new ArrayList<>(out.getOrDefault(sidesB.get(0), Collections.emptyList()));
            sidesB.forEach(sideB -> common.retainAll(out.getOrDefault(sideB, Collections.emptyList())));
            if (common.isEmpty()) {
                unions.put(name, sidesB);
            } else {
                interfaces.put(name, common);
                sidesB.forEach(sideB -> implemented.computeIfAbsent(sideB, k -> new ArrayList<>()).add(name));
            }
        });

        StringBuilder sdl = new StringBuilder("schema {\n    query: Query\n}\n\n");
        sdl.append("type Query {\n");
        out.keySet().forEach(name -> sdl.append("    ").append(queryField(name)).append("(where: WhereClause): ").append(name).append('\n'));
        sdl.append("}\n\n");
        enums.forEach((name, values) -> sdl.append("enum ").append(name).append(" {\n")
                .append(values.stream().map(v -> "    " + v + "\n").collect(Collectors.joining())).append("}\n\n"));
        interfaces.forEach((name, fields) -> sdl.append("interface ").append(name).append(" {\n")
                .append(fields.stream().map(f -> "    " + f + "\n").collect(Collectors.joining())).append("}\n\n"));
        unions.forEach((name, members) -> sdl.append("union ").append(name).append(" = ")
                .append(String.join(" | ", members)).append("\n\n"));
        out.forEach((name, fields) -> sdl.append("type ").append(name)
                .append(implemented.containsKey(name) ? " implements " + String.join(" & ", implemented.get(name)) : "")
                .append(" {\n")
                .append(fields.stream().map(f -> "    " + f + "\n").collect(Collectors.joining())).append("}\n\n"));
        sdl.append(WHERE_SCHEMA);
        return sdl.toString();
    }

    private static List<String> entityFields(EntityType entity, Map<String, String> fields) {
        return fields.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.toList());
    }

    private static boolean valid(String name) {
        return name != null && NAME.matcher(name).matches() && !name.startsWith("__") && !RESERVED.contains(name);
    }

    /** a GraphQL type for an ontology property type */
    private static String scalar(String type, Set<String> enums) {
        if (type == null) {
            return "String";
        }
        if (enums.contains(type)) {
            return type;
        }
        switch (type.toLowerCase(Locale.ROOT)) {
            case "id":
                return "ID";
            case "int":
            case "integer":
            case "long":
            case "short":
            case "byte":
                return "Int";
            case "float":
            case "double":
                return "Float";
            case "boolean":
            case "bool":
                return "Boolean";
            default:
                return "String";
        }
    }
}
