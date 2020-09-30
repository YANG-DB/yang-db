package com.yangdb.fuse.dispatcher.ontology;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.OntologyFinalizer;
import com.yangdb.fuse.model.ontology.mapping.MappingOntologies;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class DirectoryOntologyMappingProvider implements OntologyMappingProvider {
    //region Constructors
    public DirectoryOntologyMappingProvider(String dirName) throws URISyntaxException {
        this.ontologies = new HashMap<>();
        String currentDir = System.getProperty("user.dir");
        ObjectMapper mapper = new ObjectMapper();

        File dir = new File(Paths.get(currentDir, dirName).toString());
        if(!dir.exists()) {
            dir = new File(Thread.currentThread().getContextClassLoader().getResource(dirName).toURI());
        }
        if (dir.exists()) {
            this.ontologies =
                    Stream.of(dir.listFiles() == null ? new File[0] : dir.listFiles())
                    .filter(file -> FilenameUtils.getExtension(file.getName()).equals("json"))
                    .filter(file -> FilenameUtils.getBaseName(file.getName()).toLowerCase().contains("mapping"))
                    .toJavaMap(file -> {
                        try {
                            MappingOntologies mappingOntologies = mapper.readValue(file, MappingOntologies.class);
                            return new Tuple2<>(mappingOntologies.getSourceOntology(),mappingOntologies);
                        } catch (IOException e) {
                            return new Tuple2<>(FilenameUtils.getBaseName(file.getName()), new MappingOntologies());
                        }
                    });
        }
    }
    //endregion

    //region OntologyProvider Implementation
    @Override
    public Optional<MappingOntologies> get(String id) {
        return Optional.ofNullable(this.ontologies.get(id));
    }

    @Override
    public Collection<MappingOntologies> getAll() {
        return ontologies.values();
    }

    @Override
    public MappingOntologies add(MappingOntologies ontology) {
        ontologies.put(ontology.getSourceOntology(),ontology);
        return ontology;
    }
    //endregion

    //region Fields
    private Map<String,MappingOntologies> ontologies;
    //endregion
}
