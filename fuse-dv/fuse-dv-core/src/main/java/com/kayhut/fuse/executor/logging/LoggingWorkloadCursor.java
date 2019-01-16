package com.kayhut.fuse.executor.logging;

import com.codahale.metrics.MetricRegistry;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.dispatcher.logging.MethodName;
import com.kayhut.fuse.dispatcher.logging.Sequence;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import java.util.List;

public class LoggingWorkloadCursor<S, E> implements Traversal<S, E> {
    //region Constructors
    public LoggingWorkloadCursor(
            Traversal<S, E> traversal,
            MetricRegistry metricRegistry) {
        this.traversal = traversal;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region GraphTraversal Implementation
    @Override
    public Traversal.Admin<S, E> asAdmin() {
        return this.traversal.asAdmin();
    }

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
        try {
            this.metricRegistry.counter(cursor.getMethodName()).inc(amount);
            return this.traversal.next(amount);
        } catch (Exception ex) {
            throw ex;
        } finally {
            this.metricRegistry.counter(cursor.getMethodName()).dec(amount);
        }
    }

    @Override
    public List<E> toList() {
        boolean thrownExcpetion = false;
        try {

            return this.traversal.toList();
        } catch (Exception ex) {
            thrownExcpetion = true;
            throw ex;
        } finally {
            if (!thrownExcpetion) {
            }
        }
    }
    //endregion

    //region Fields
    private Traversal<S, E> traversal;
    private MetricRegistry metricRegistry;

    private static MethodName.MDCWriter cursor = MethodName.of("cursor");
    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
