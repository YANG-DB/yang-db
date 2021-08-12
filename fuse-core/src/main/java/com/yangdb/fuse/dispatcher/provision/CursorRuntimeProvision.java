package com.yangdb.fuse.dispatcher.provision;

import com.codahale.metrics.MetricRegistry;

import static com.yangdb.fuse.dispatcher.provision.ScrollProvisioning.SCROLLS_REGISTRY;

/**
 * add provisioning capability for query cursor runtime
 */
public interface CursorRuntimeProvision {
    int getActiveScrolls();

    boolean clearScrolls();

    class NoOpCursorRuntimeProvision implements CursorRuntimeProvision {

        public static final CursorRuntimeProvision INSTANCE = new NoOpCursorRuntimeProvision();

        @Override
        public int getActiveScrolls() {
            return 0;
        }

        @Override
        public boolean clearScrolls() {
            return false;
        }
    }

    class MetricRegistryCursorRuntimeProvision implements CursorRuntimeProvision {

        private String context;
        private MetricRegistry registry;

        public MetricRegistryCursorRuntimeProvision(String context,MetricRegistry registry) {
            this.context = context;
            this.registry = registry;
        }

        @Override
        public int getActiveScrolls() {
            String prefix = String.format("%s.%s", SCROLLS_REGISTRY, context);
            return this.registry.getCounters((s, metric) -> s.startsWith(prefix)).size();
        }

        @Override
        public boolean clearScrolls() {
            String prefix = String.format("%s.%s", SCROLLS_REGISTRY, context);
            this.registry.removeMatching((s, metric) -> s.startsWith(prefix));
            return true;
        }
    }
}
