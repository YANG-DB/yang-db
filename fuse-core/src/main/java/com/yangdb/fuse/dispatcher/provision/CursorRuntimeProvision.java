package com.yangdb.fuse.dispatcher.provision;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
