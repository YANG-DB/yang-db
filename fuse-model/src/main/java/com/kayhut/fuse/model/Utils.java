package com.kayhut.fuse.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.kayhut.fuse.model.process.ProcessElement;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/result.json");
            if(stream!=null ) {
                System.out.println("Loading - /result.json");
                result = loadJsonString(stream);
            } else {
                stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("result.json");
                System.out.println("Loading - result.json");
                result = loadJsonString(stream);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //todo something clever
        } finally {
            return result;
        }
    }

    static String loadJsonString(InputStream stream) throws IOException {
        String s = IOUtils.toString(stream);
        Object value = mapper.readValue(s, Object.class);
        return mapper.writeValueAsString(value);
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
