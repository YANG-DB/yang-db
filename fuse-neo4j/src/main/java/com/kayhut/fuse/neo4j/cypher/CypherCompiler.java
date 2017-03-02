package com.kayhut.fuse.neo4j.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Created by EladW on 23/02/2017.
 */
public class CypherCompiler {

    static Logger logger = LoggerFactory.getLogger("com.kayhut.fuse.neo4j.cypher.CypherCompiler");

    public static class QueryElementsOrderedIterator implements Iterator<EBase> {

        List<EBase> elements;
        EBase next = null;

        public QueryElementsOrderedIterator(Query q) {
            elements = q.getElements();
        }

        @Override
        public boolean hasNext() {
            setNext();
            return next != null;
        }

        @Override
        public EBase next() {
            return next;
        }

        private void setNext() {

            if (next == null) {
                //search for start element
                for (EBase e : elements) {
                    if (e instanceof Start) {
                        next = e;
                        return;
                    }
                }
            } else {
                //search for next element number
                for (EBase e : elements) {

                    //TODO: Every element type must have a next pointer!!!
                    int nextNext = 0;
                    if (next instanceof Start) {
                        nextNext = ((Start) next).getNext();
                    } else if (next instanceof ETyped) {
                        nextNext = ((ETyped) next).getNext();
                    } else if (next instanceof EConcrete) {
                        nextNext = ((EConcrete) next).getNext();
                    } else if (next instanceof EUntyped) {
                        nextNext = ((EUntyped) next).getNext();
                    } else if (next instanceof Rel) {
                        nextNext = ((Rel)next).getNext();
                    }

                    if (e.geteNum() == nextNext && !(e instanceof Start)) {
                        next = e;
                        return;
                    }
                }
            }

            next = null;

        }
    }

    public static String compile(com.kayhut.fuse.model.query.Query query, Schema schema) {

        StringBuilder cypherMatch = new StringBuilder();

        cypherMatch.append("MATCH ");

        StringBuilder cypherWhere = new StringBuilder();

        cypherWhere.append(" WHERE ");

        StringBuilder cypherReturn = new StringBuilder();

        cypherReturn.append(" RETURN ");

        ObjectMapper mapper = new ObjectMapper();

        Iterator<EBase> itr = new QueryElementsOrderedIterator(query);

        boolean hasWhere = false;

        int prevEtype = 0;
        String prevTag = null;

        while (itr.hasNext()) {

            EBase next = itr.next();

            if (next instanceof ETyped) {
                cypherMatch.append(String.format("(%s:%s)", ((ETyped) next).geteTag(), schema.getEntityLabel(((ETyped) next).geteType())));
                cypherReturn.append(String.format("%s ,", ((ETyped) next).geteTag()));
                prevEtype = ((ETyped) next).geteType();
                prevTag = ((ETyped) next).geteTag();
            } else if (next instanceof Rel) {
                Rel r = (Rel) next;
                if (r.getDir() == "R") {
                    cypherMatch.append(String.format("-[:%s]->", schema.getRelationLabel(r.getrType())));
                } else {
                    cypherMatch.append(String.format("<-[:%s]-", schema.getRelationLabel(r.getrType())));
                }
                prevEtype = r.getrType();
            } else if (next instanceof EProp) {
                EProp eprop = (EProp) next;
                if (eprop.getCond().getOp() == ConditionOp.eq) {
                    hasWhere = true;
                    Schema.Property prop = schema.getProperty(prevEtype, eprop.getpType());
                    if (prop.type.equals("string")) {
                        cypherWhere.append(String.format("%s.%s = '%s'", prevTag, schema.getPropertyField(prop.name), eprop.getCond().getValue()));
                    } else {
                        cypherWhere.append(String.format("%s.%s = %s", prevTag, schema.getPropertyField(prop.name), eprop.getCond().getValue()));
                    }
                }
            }

        }

        StringBuilder cypher = new StringBuilder();

        cypher.append(cypherMatch);

        if (hasWhere) {
            cypher.append(cypherWhere);
        }

        cypher.append(cypherReturn.substring(0, cypherReturn.length() - 1));

        logger.info(String.format("(%s) -[:Compiled]-> ( %s)", query.getName(), cypher));

        return cypher.toString();
    }

}
