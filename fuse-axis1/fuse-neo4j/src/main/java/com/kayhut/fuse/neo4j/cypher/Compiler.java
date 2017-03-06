package com.kayhut.fuse.neo4j.cypher;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by EladW on 23/02/2017.
 */
public class Compiler {

    Logger logger = LoggerFactory.getLogger("com.kayhut.fuse.neo4j.cypher.Compiler");

    public static class Condition {
        public Condition() {

        }

        public String op;
        public String value;
    }

    public static class Element {
        public Element() {

        }

        public int eNum;
        public String type;
        public String eTag;
        public int eType;
        public int rType;
        public int pType;
        public String eName;
        public int next;
        public String dir;
        public Condition cond;
    }

    public static class Query {

        public Query() {

        }

        public String ont;
        public String name;
        public List<Element> elements;

        public Iterator<Element> iterator() {

            Iterator<Element> itr = new Iterator<Element>() {

                private Element next;

                @Override
                public boolean hasNext() {
                    setNext();
                    return next != null;
                }

                @Override
                public Element next() {
                    return next;
                }

                private void setNext() {

                    if(next == null) {
                        //search for start element
                        for (Element e : elements) {
                            if(e.type.equals("Start")) {
                                next = e;
                                return;
                            }
                        }
                    }
                    else {
                        //search for next element number
                        for (Element e : elements) {
                            if(e.eNum == next.next && !e.type.equals("Start")) {
                                next = e;
                                return;
                            }
                        }
                    }

                    next = null;

                }
            };

            return itr;

        }

    }

    public String compile(String query, Schema schema) {

        StringBuilder cypherMatch = new StringBuilder();

        cypherMatch.append("MATCH ");

        StringBuilder cypherWhere = new StringBuilder();

        cypherWhere.append(" WHERE ");

        StringBuilder cypherReturn = new StringBuilder();

        cypherReturn.append(" RETURN ");

        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // At first stage we will have a collection of v1 queries written in cypher,
        // and we only support querying ine of these queries, identified by query-id.

        try {

            Query q = mapper.readValue(query, Query.class);

            Iterator<Element> itr = q.iterator();

            boolean hasWhere = false;

            Element prev = null;

            while(itr.hasNext()) {

                Element next = itr.next();

                switch(next.type) {
                    case "Start":
                        break;
                    case "ETyped":
                        cypherMatch.append(String.format("(%s:%s)",next.eTag,schema.getEntityLabel(next.eType)));
                        cypherReturn.append(String.format("%s ,",next.eTag));
                        break;
                    case "Rel":
                        if(next.dir == "R") {
                            cypherMatch.append(String.format("-[:%s]->",schema.getRelationLabel(next.rType)));
                        }
                        else {
                            cypherMatch.append(String.format("<-[:%s]-",schema.getRelationLabel(next.rType)));
                        }
                        break;
                    case "EProp":
                        if(next.cond.op.equals("eq")) {
                            hasWhere = true;
                            Schema.Property prop = schema.getProperty(prev.eType, next.pType);
                            if (prop.type.equals("string")) {
                                cypherWhere.append(String.format("%s.%s = '%s'",prev.eTag,schema.getPropertyField(prop.name),next.cond.value));
                            } else {
                                cypherWhere.append(String.format("%s.%s = %s",prev.eTag,schema.getPropertyField(prop.name),next.cond.value));
                            }
                        }
                        break;
                }

                prev = next;
            }

            StringBuilder cypher = new StringBuilder();

            cypher.append(cypherMatch);

            if(hasWhere) {
                cypher.append(cypherWhere);
            }

            cypher.append(cypherReturn.substring(0,cypherReturn.length()-1));

            return cypher.toString();

        } catch (IOException e) {
            logger.error("JSON parsing failed.");
        }

        return null;
    }

}
