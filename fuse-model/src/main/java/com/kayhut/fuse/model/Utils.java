package com.kayhut.fuse.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.StringJoiner;
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

    static String readJsonFile(String name) {
        String result = "{}";
        try {
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + name + ".json");
            if (stream != null) {
                result = loadJsonString(stream);
            } else {
                stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name + ".json");
                result = loadJsonString(stream);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //todo something clever
        } finally {
            return result;
        }
    }

    static String asString(Object value) throws JsonProcessingException {
        return mapper.writeValueAsString(value);
    }

    static <T> T asObject(String value, Class<T> clazz) throws IOException {
        return mapper.readValue(value, clazz);
    }

    static String loadJsonString(InputStream stream) throws IOException {
        String s = IOUtils.toString(stream);
        Object value = mapper.readValue(s, Object.class);
        return mapper.writeValueAsString(value);
    }

    static String baseUrl(String port) {
        return "http://" + getHostAddress() + ":" + port + "/fuse";
    }

    static String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    static String pattern(List<PlanOpBase> pattern) {
        StringJoiner sj = new StringJoiner(":", "", "");
        pattern.forEach(op -> sj.add(op.getClass().getSimpleName()));
        return sj.toString();
    }
}
