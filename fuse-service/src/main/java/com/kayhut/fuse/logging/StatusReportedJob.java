package com.kayhut.fuse.logging;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.resource.store.NodeStatusResource;
import org.jooby.quartz.Scheduled;

public class StatusReportedJob {
        private NodeStatusResource statusResource;

        @Inject
        public StatusReportedJob(NodeStatusResource statusResource) {
            this.statusResource = statusResource;
        }

        @Scheduled("15s; delay=20s; repeat=*")
        public void report() {
            this.statusResource.report();
    }
}
