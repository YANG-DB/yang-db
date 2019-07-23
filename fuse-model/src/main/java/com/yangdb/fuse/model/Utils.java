package com.yangdb.fuse.model;

/*-
 * #%L
 * Utils.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.query.EBase;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by lior.perry on 19/02/2017.
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
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + name);
            if (stream != null) {
                result = loadJsonString(stream);
            } else {
                stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
                result = loadJsonString(stream);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //todo something clever
        } finally {
            return result;
        }
    }

    static Optional<String> match(String step, String... supportedPattern) {
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

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface SkipSerialisation {
    }

}
