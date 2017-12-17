package com.kayhut.fuse.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.execution.plan.AsgEBaseContainer;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;
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
}
