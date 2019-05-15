package com.kayhut.fuse.assembly.knowledge.parser;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.assembly.knowledge.parser.model.BusinessTypesProvider;
import javaslang.collection.Stream;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class FolderBasedTypeProvider implements BusinessTypesProvider {

    public FolderBasedTypeProvider(String dirName) throws URISyntaxException {
        String currentDir = System.getProperty("user.dir");
        ObjectMapper mapper = new ObjectMapper();

        File dir = new File(Paths.get(currentDir, dirName).toString());
        if (!dir.exists()) {
            dir = new File(Thread.currentThread().getContextClassLoader().getResource(dirName).toURI());
        }
        if (dir.exists()) {
            this.businessTypes = Stream.of(dir.listFiles() == null ? new File[0] : dir.listFiles())
                    .filter(file -> FilenameUtils.getExtension(file.getName()).equals("json"))
                    .filter(file -> FilenameUtils.getBaseName(file.getName()).toLowerCase().contains("businesstypes"))
                    .map(file -> {
                        try {
                            return mapper.readValue(file, BusinessTypes.class);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toJavaList();

        }
    }

    @Override
    public Optional<String> type(String fieldId) {
        return businessTypes
                .stream()
                .map(p -> p.get(fieldId))
                .filter(Objects::nonNull)
                .findAny();
    }

    private List<BusinessTypes> businessTypes;

    public static class BusinessTypes {
        private String namespace;
        private String version;
        private List<Map<String, String>> properties;

        public BusinessTypes() {
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<Map<String, String>> getProperties() {
            return properties;
        }

        public void setProperties(List<Map<String, String>> properties) {
            this.properties = properties;
        }

        public String get(String field) {
//            deal with non valid keys => should be namespace#type
            if (field.split("[#]").length != 2)
                return null;

            String namespace = field.split("[#]")[0];
            String logicalType = field.split("[#]")[1];

            if (!this.namespace.equals(namespace))
                return null;

            return this.properties.stream()
                    .filter(m -> m.get("name").equals(logicalType))
                    .findAny().orElse(Collections.singletonMap("type", null))
                    .get("type");
        }
    }
}
