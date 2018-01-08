package com.kayhut.fuse.executor.logging;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;

import java.util.List;

/**
 * Created by roman.margolis on 07/01/2018.
 */
public class LoggingTraversal<S, E> implements Traversal<S, E> {
    //region Constructors
    public LoggingTraversal(
            Traversal<S, E> traversal,
            Logger logger) {
        this.traversal = traversal;
        this.logger = logger;
    }
    //endregion

    //region GraphTraversal Implementation
    @Override
    public boolean hasNext() {
        return this.traversal.hasNext();
    }

    @Override
    public E next() {
        return this.traversal.next();
    }

    @Override
    public List<E> next(int amount) {
        boolean thrownExcpetion = false;

        try {
            this.logger.trace("start next");
            return this.traversal.next(amount);
        } catch (Exception ex) {
            thrownExcpetion = true;
            this.logger.trace("failed next");
            throw ex;
        } finally {
            if (!thrownExcpetion) {
                this.logger.trace("finish next");
            }
        }
    }

    @Override
    public List<E> toList() {
        boolean thrownExcpetion = false;

        try {
            this.logger.trace("start toList");
            return this.traversal.toList();
        } catch (Exception ex) {
            thrownExcpetion = true;
            this.logger.trace("failed toList");
            throw ex;
        } finally {
            if (!thrownExcpetion) {
                this.logger.trace("finish toList");
            }
        }
    }
    //endregion

    //region Fields
    private Traversal<S, E> traversal;
    private Logger logger;
    //endregion
}
