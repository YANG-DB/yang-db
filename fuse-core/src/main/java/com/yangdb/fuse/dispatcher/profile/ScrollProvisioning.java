package com.yangdb.fuse.dispatcher.profile;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * scroll provisioning API for a single traversal
 */
public interface ScrollProvisioning {

    String SCROLLS_REGISTRY = "Scrolls_Registry";

    boolean addScroll(String scrollId, long timeout);

    boolean clearScroll(String scrollId);

    Collection<String> getActiveScrolls();

    boolean clearScrolls();

    class NoOpScrollProvisioning implements ScrollProvisioning {
        private Logger logger = LoggerFactory.getLogger(NoOpScrollProvisioning.class);

        public static NoOpScrollProvisioning INSTANCE = new NoOpScrollProvisioning();

        @Override
        public boolean addScroll(String scrollId, long timeout) {
            logger.info(String.format("Adding scroll %s", scrollId));
            return false;
        }

        @Override
        public boolean clearScroll(String scrollId) {
            logger.info(String.format("remove scroll %s", scrollId));
            return false;
        }

        @Override
        public Collection<String> getActiveScrolls() {
            return Collections.emptyList();
        }

        @Override
        public boolean clearScrolls() {
            return false;
        }
    }

    class MetricRegistryScrollProvisioning implements ScrollProvisioning {
        private String context;
        private MetricRegistry scrollRegistry;
        private MetricRegistry metricRegistry;

        public MetricRegistryScrollProvisioning(MetricRegistry metricRegistry,String context) {
            this.metricRegistry = metricRegistry;
            this.context = context;
            try {
                String prefix = String.format("%s.%s", SCROLLS_REGISTRY, context);
                scrollRegistry = metricRegistry.register(prefix, new MetricRegistry());
            } catch (Throwable alreadyRegistered) {
                //ignored
            }
        }

        @Override
        public boolean addScroll(String scrollId, long timeout) {
            if (scrollRegistry.counter(scrollId).getCount() != 0)
                return true;

            scrollRegistry.counter(scrollId).inc(1);
            return true;
        }

        @Override
        public boolean clearScroll(String scrollId) {
            scrollRegistry.remove(scrollId);
            return true;
        }

        @Override
        public Collection<String> getActiveScrolls() {
            return scrollRegistry.getMetrics().keySet();
        }

        @Override
        public boolean clearScrolls() {
            scrollRegistry.removeMatching(MetricFilter.ALL);
            String prefix = String.format("%s.%s", SCROLLS_REGISTRY, context);
            return metricRegistry.remove(prefix);
        }
    }
}
