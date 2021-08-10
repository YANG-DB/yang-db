package com.yangdb.fuse.dispatcher.profile;

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * scroll provisioning API for a single traversal
 */
public interface ScrollProvisioning {
    boolean addScroll(String scrollId,long timeout);
    boolean clearScroll(String scrollId);

    List<String> getActiveScrolls();
    int clearScrolls();

    class NoOpScrollProvisioning implements ScrollProvisioning {
        private Logger logger = LoggerFactory.getLogger(NoOpScrollProvisioning.class);

        public static NoOpScrollProvisioning INSTANCE = new NoOpScrollProvisioning();

        @Override
        public boolean addScroll(String scrollId, long timeout) {
            logger.info(String.format("Adding scroll %s",scrollId));
            return false;
        }

        @Override
        public boolean clearScroll(String scrollId) {
            logger.info(String.format("remove scroll %s",scrollId));
            return false;
        }

        @Override
        public List<String> getActiveScrolls() {
            return Collections.emptyList();
        }

        @Override
        public int clearScrolls() {
            return 0;
        }
    }

    class MetricRegistryScrollProvisioning implements ScrollProvisioning {
        private MetricRegistry metricRegistry;

        public MetricRegistryScrollProvisioning(MetricRegistry metricRegistry) {
            this.metricRegistry = metricRegistry;
        }

        @Override
        public boolean addScroll(String scrollId, long timeout) {
            return false;
        }

        @Override
        public boolean clearScroll(String scrollId) {
            return false;
        }

        @Override
        public List<String> getActiveScrolls() {
            return null;
        }

        @Override
        public int clearScrolls() {
            return 0;
        }
    }
}
