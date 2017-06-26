package com.kayhut.fuse.neo4j.cypher.types;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Created by Elad on 6/25/2017.
 */
public class Neo4jDateTimeTypeParser implements Neo4jTypeParser {

    public static String DATE_TIME_FORMAT = "MM/dd/YYYY HH:mm:SS";

    @Override
    public String getCypherExpression(Object expression) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        LocalDateTime dateTime = LocalDateTime.from(formatter.parse((CharSequence) expression));
        long millis = dateTime.toEpochSecond(ZoneOffset.UTC) * 1000;
        return String.valueOf(millis);
    }

    @Override
    public String getRealExpression(Object cypherResult) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        String dateTime = formatter.format(LocalDateTime.ofEpochSecond((Long)cypherResult / 1000, 0, ZoneOffset.UTC));
        return dateTime;
    }
}
