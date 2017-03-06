package com.kayhut.fuse.neo4j.cypher;

import com.kayhut.fuse.model.process.AsgData;
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

    public static String compile(com.kayhut.fuse.model.query.Query query, Schema schema) throws CypherCompilerException {

        String cypher = buildStatement( new QueryElementsOrderedIterator(query), schema).compose();

        logger.info(String.format("(%s) -[:Compiled]-> ( %s)", query.getName(), cypher));

        return cypher.toString();
    }

    private static CypherStatement buildStatement(QueryElementsOrderedIterator itr, Schema schema) throws CypherCompilerException {

        CypherMatch match = new CypherMatch();
        CypherWhere where = new CypherWhere();
        CypherReturn ret = new CypherReturn();

        EBase prevElement = null;

        while(itr.hasNext()) {

            EBase next = itr.next();

            if (next instanceof ETyped) {
                match.appendNode(((ETyped) next).geteTag(),
                            schema.getEntityLabel(((ETyped) next).geteType()),
                            null);
                ret.append(((ETyped) next).geteTag(), null, null);
                prevElement = next;
            }

            if(next instanceof Rel) {
                match.appendRelationship(null,
                                        schema.getRelationLabel(((Rel)next).getrType()),
                                        null,
                                        ((Rel)next).getDir() == "R" ? CypherMatch.Direction.RIGHT : CypherMatch.Direction.LEFT);
            }

            if(next instanceof EProp) {
                EProp eprop = (EProp) next;
                Schema.Property prop = schema.getProperty(((ETyped)prevElement).geteType(), eprop.getpType());
                where.appendUnary(CypherWhere.ConditionType.AND,
                                  ((ETyped)prevElement).geteTag(),
                                    schema.getPropertyField(schema.getProperty(((ETyped)prevElement).geteType(), eprop.getpType()).name),
                                    null,
                                    eprop.getCond().getOp() == ConditionOp.eq ? CypherWhere.OpType.EQUALS :
                                                        CypherWhere.OpType.LARGER,
                                    prop.type.equals("string") ? String.format("'%s'", eprop.getCond().getValue()) :
                                                        String.valueOf(eprop.getCond().getValue()));

            }

            if(next instanceof Quant1) {
                Quant1 quant = (Quant1)next;
                //todo: 2 nexts ??
            }

        }

        return CypherStatement.build().with(match).with(where).with(ret);

    }
}
