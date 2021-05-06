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
import com.yangdb.fuse.dispatcher.utils.FileUtils;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.OntologyFinalizer;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class DirectoryOntologyProvider implements OntologyProvider {
    private static final ObjectMapper mapper = new ObjectMapper();
    private String dirName;

    //region Constructors
    public DirectoryOntologyProvider(String dirName) {
        this.dirName = dirName;
        this.ontologies = new HashMap<>();
        File dir = null;
        try {
            dir = FileUtils.getOrCreateFile(dirName, System.getProperty("user.dir"),true);
        } catch (IOException e) {
            throw new FuseError.FuseErrorException("Failed reading folder for new Ontology Provider ["+dirName + "] ", e.getCause());
        }
        if (dir.exists()) {
            this.ontologies =
                    Stream.of(dir.listFiles() == null ? new File[0] : dir.listFiles())
                    .filter(file -> FilenameUtils.getExtension(file.getName()).equals("json"))
                    .filter(file -> !FilenameUtils.getBaseName(file.getName()).toLowerCase().contains("transformation"))
                    .toJavaMap(file -> {
                        try {
                            Ontology ontology = OntologyFinalizer.finalize(mapper.readValue(file, Ontology.class));
                            return new Tuple2<>(ontology.getOnt(),ontology);
                        } catch (IOException e) {
                            return new Tuple2<>(FilenameUtils.getBaseName(file.getName()), new Ontology());
                        }
                    });
        }
    }
    //endregion

    //region OntologyProvider Implementation
    @Override
    public Optional<Ontology> get(String id) {
        return Optional.ofNullable(this.ontologies.get(id));
    }

    @Override
    public Collection<Ontology> getAll() {
        return ontologies.values();
    }

    @Override
    public synchronized Ontology add(Ontology ontology) {
        ontologies.put(ontology.getOnt(), OntologyFinalizer.finalize(ontology));
        //store locally
        File dir = null;
        try {
            dir = FileUtils.getOrCreateFile(dirName, System.getProperty("user.dir"),true);
        } catch (IOException e) {
            throw new FuseError.FuseErrorException("Failed reading folder for new Ontology Provider ["+dirName + "] ", e.getCause());
        }

        if (dir.exists()) {
            Path path = Paths.get(dir.getAbsolutePath()+"/"+ontology.getOnt()+".json");
            try {
                Files.write(path, mapper.writeValueAsBytes(ontology));
            } catch (IOException e) {
                throw new FuseError.FuseErrorException("Failed writing file for new Ontology ["+ontology.getOnt()+"] ",e.getCause());
            }
        }

        return ontology;
    }
    //endregion

    //region Fields
    private Map<String, Ontology> ontologies;
    //endregion
}
