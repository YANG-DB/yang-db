package com.kayhut.fuse.neo4j.cypher.types;

import com.kayhut.fuse.model.ontology.EnumeratedType;
import com.kayhut.fuse.model.ontology.Ontology;
import javaslang.collection.Stream;
import javaslang.control.Option;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Elad on 6/25/2017.
 */
public final class CypherTypeParsersFactory {

    static Map<String, Neo4jTypeParser> parsers = null;
    static Neo4jTypeParser defaultParser = new Neo4jDefaultTypeParser();
    static Neo4jTypeParser enumParser = new Neo4jEnumTypeParser();

    private CypherTypeParsersFactory() {

    }

    private static void initParsers() {
        parsers = new HashMap<>();
        parsers.put("date", new Neo4jDateTypeParser());
        parsers.put("datetime", new Neo4jDateTimeTypeParser());
        parsers.put("string", new Neo4jStringTypeParser());
    }

    public static String toCypherValue(Ontology ont, String type, Object expression) {
        if(parsers == null) {
            initParsers();
        }
        if(parsers.containsKey(type)) {
            return parsers.get(type).getCypherExpression(expression);
        } else {
            //check if it is an enumerated type
            if (ont.getEnumeratedTypes() != null) {
                Option<EnumeratedType> enumType = Stream.ofAll(ont.getEnumeratedTypes()).find(et -> et.geteType().equals(type));
                if (!enumType.isEmpty()) {
                    return enumParser.getCypherExpression(expression);
                }
            }
            return defaultParser.getCypherExpression(expression);
        }
    }

    public static Object toPropValue(Ontology ont, String type, Object cypherValue) {
        if(parsers == null) {
            initParsers();
        }
        if(parsers.containsKey(type)) {
            return parsers.get(type).getRealExpression(cypherValue);
        } else {
            //check if it is an enumerated type
            if (ont.getEnumeratedTypes() != null) {
                Option<EnumeratedType> enumType = Stream.ofAll(ont.getEnumeratedTypes()).find(et -> et.geteType().equals(type));
                if (!enumType.isEmpty()) {
                    return enumParser.getRealExpression(cypherValue);
                }
            }
            return defaultParser.getRealExpression(cypherValue);
        }
    }

}
