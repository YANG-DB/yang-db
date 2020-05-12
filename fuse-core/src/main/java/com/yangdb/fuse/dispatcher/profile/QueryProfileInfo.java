package com.yangdb.fuse.dispatcher.profile;

import org.apache.tinkerpop.gremlin.process.traversal.util.Metrics;

import java.util.List;

/**
 * profiling info for
 */
public interface QueryProfileInfo {

    Metrics measurements();

    class QueryProfileInfoImpl implements QueryProfileInfo {
        private Metrics measurements;

        public QueryProfileInfoImpl(Metrics measurements ) {
            this.measurements = measurements;
        }

        @Override
        public Metrics measurements() {
            return measurements;
        }
    }

}
