package com.kayhut.fuse.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.kayhut.fuse.model.process.ProcessElement;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.UUID;

/**
 * Created by lior on 19/02/2017.
 */
public interface Utils {
    ObjectMapper mapper = new ObjectMapper();
    static <T> T submit(EventBus eventBus, T data) {
        System.out.println("EventBus[" + data.toString() + "]");
        eventBus.post(data);
        return data;
    }

    static String getOrCreateId(String id) {
        return id != null ? id : UUID.randomUUID().toString();
    }

    static Object readJsonFile(String name) {
        String result = "{}";
        try {
            java.nio.file.Path path = Paths.get(ClassLoader.getSystemResource("result.json").toURI());
            result = new String(Files.readAllBytes(Paths.get(path.toString())));
            Object value = mapper.readValue(result, Object.class);
            result = mapper.writeValueAsString(value);
        } catch (Exception e) {
            //todo something clever
        } finally {
            return result;
        }
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

        public static FlowBuilder build(String name, EventBus eventBus) {
            return new FlowBuilder(name, eventBus);
        }

        public FlowBuilder consume(ProcessElement element) {
            eventBus.register(element);
            return this;
        }

    }
}
