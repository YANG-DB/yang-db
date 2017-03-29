package com.kayhut.fuse.neo4j;

import javaslang.Tuple2;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

/**
 * Created by liorp on 3/16/2017.
 */
public interface GraphProvider {
    Tuple2<Transaction,StatementResult> run(String cypher);
}
