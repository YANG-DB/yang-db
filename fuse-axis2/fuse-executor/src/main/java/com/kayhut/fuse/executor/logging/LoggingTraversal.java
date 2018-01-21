package com.kayhut.fuse.executor.logging;

import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.slf4j.Logger;

import java.util.List;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.LogType.finish;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.LogType.start;

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
            new LogMessage(this.logger, trace, start, "next", "start next").log();
            return this.traversal.next(amount);
        } catch (Exception ex) {
            thrownExcpetion = true;
            new LogMessage(this.logger, error, finish, "next", "failed next", ex).log();
            throw ex;
        } finally {
            if (!thrownExcpetion) {
                new LogMessage(this.logger, trace, finish, "next", "finish next").log();
            }
        }
    }

    @Override
    public List<E> toList() {
        boolean thrownExcpetion = false;

        try {
            new LogMessage(this.logger, trace, start, "toList", "start toList").log();
            return this.traversal.toList();
        } catch (Exception ex) {
            thrownExcpetion = true;
            new LogMessage(this.logger, error, finish, "toList", "failed toList", ex).log();
            throw ex;
        } finally {
            if (!thrownExcpetion) {
                new LogMessage(this.logger, trace, finish, "toList", "finish toList").log();
            }
        }
    }
    //endregion

    //region Fields
    private Traversal<S, E> traversal;
    private Logger logger;
    //endregion
}
