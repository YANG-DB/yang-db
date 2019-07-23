package com.yangdb.fuse.test.framework.providers;

/*-
 * #%L
 * fuse-test-framework
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.collection.Stream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/**
 * Created by moti on 3/12/2017.
 */
public class FileJsonDataProvider implements GenericDataProvider {
    private ObjectMapper mapper = new ObjectMapper();
    private String filePath;

    public FileJsonDataProvider(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Iterable<Map<String, Object>> getDocuments() throws IOException {
        return Stream.ofAll(() -> {
            try {
                return Files.lines(Paths.get(filePath)).iterator();
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.emptyIterator();
            }
        }).map(line -> {
            try {
                return mapper.readValue(line, new TypeReference<Map<String, Object>>(){});
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
