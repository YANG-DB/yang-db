package com.kayhut.fuse.model;

import com.google.common.eventbus.EventBus;
import com.kayhut.fuse.model.process.ProcessElement;

import java.util.UUID;

/**
 * Created by lior on 19/02/2017.
 */
public interface Utils {

    static<T> T submit(EventBus eventBus, T data) {
        System.out.println("EventBus["+data.toString()+"]");
        eventBus.post(data);
        return data;
    }

    static String getOrCreateId(String id) {
        return id!=null ? id : UUID.randomUUID().toString();
    }

    /**
     * @Deprecated
     */
    class FlowBuilder {
        private String name;
        private EventBus eventBus;

        public FlowBuilder(String name, EventBus eventBus) {
            this.name = name;
            this.eventBus = eventBus;
        }

        public static FlowBuilder build(String name,EventBus eventBus) {
            return new FlowBuilder(name,eventBus);
        }

        public FlowBuilder consume(ProcessElement element) {
            eventBus.register(element);
            return this;
        }

    }
}
