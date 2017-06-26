package com.kayhut.fuse.neo4j.cypher.types;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Elad on 6/25/2017.
 */
public class Neo4jDateTypeParser implements Neo4jTypeParser {

    public static String DATE_FORMAT = "MM/dd/YYYY";

    @Override
    public String getCypherExpression(Object expression) {
        String cypherExpr;
        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        try {
            Date date = formatter.parse((String) expression);
            cypherExpr = String.valueOf(date.getTime());
        } catch (ParseException e) {
            throw new RuntimeException("Unknown date format: " + expression + ". Expected format: " + DATE_FORMAT);
        }
        return cypherExpr;
    }

    @Override
    public String getRealExpression(Object cypherExpression) {
        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        String date = formatter.format(new Date((Long) cypherExpression));
        return date;
    }
}
