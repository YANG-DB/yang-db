package com.kayhut.fuse.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.query.EBase;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by lior on 19/02/2017.
 */
public interface Utils {
    ObjectMapper mapper = new ObjectMapper();

    static List<EBase> from(String elements) {
        return Arrays.stream(elements.split("\\n")).map(e -> {
            try {
                return mapper.readValue(e, EBase.class);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    static <T> T submit(EventBus eventBus, T data) {
        eventBus.post(data);
        return data;
    }

    static String getOrCreateId(String id) {
        return id != null ? id : UUID.randomUUID().toString();
    }

    static String readJsonFile(String name) {
        String result = "{}";
        try {
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + name );
                if (stream != null) {
                result = loadJsonString(stream);
            } else {
                stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name );
                result = loadJsonString(stream);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //todo something clever
        } finally {
            return result;
        }
    }

    static Optional<String> match(String step,String ... supportedPattern) {
        for (String pattern : supportedPattern) {
            Pattern compile = Pattern.compile(pattern);
            Matcher matcher = compile.matcher(step);
            if (matcher.find()) {
                return Optional.of(matcher.group());
            }
        }
        return Optional.empty();
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

    static String fullPattern(List<PlanOpBase> pattern) {
        StringJoiner sj = new StringJoiner(":", "[", "]");
        pattern.forEach(op -> sj.add(op.toString()));
        return sj.toString();
    }

    static String pattern(List<PlanOpBase> pattern) {
        StringJoiner sj = new StringJoiner(":", "", "");
        pattern.forEach(op -> sj.add(op.getClass().getSimpleName()));
        return sj.toString();
    }

    static String simplePattern(List<PlanOpBase> pattern) {
        StringJoiner sj = new StringJoiner(":", "[", "]");
        pattern.forEach(op -> sj.add(Integer.toString(op.geteNum())));
        return sj.toString();
    }

    static List<Class<? extends PlanOpBase>> fromPattern(String pattern) {
        if(pattern.split("\\:").length ==0)
            return Collections.emptyList();

        return Arrays.asList(pattern.split("\\:")).stream().map(element -> {
            try {
                return (Class<? extends PlanOpBase>)Class.forName(element);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Class.forName(element) failed for "+element);
            }
        }).collect(Collectors.toList());
    }
}
