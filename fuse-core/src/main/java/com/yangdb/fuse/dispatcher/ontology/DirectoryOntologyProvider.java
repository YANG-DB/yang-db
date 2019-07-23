package com.yangdb.fuse.dispatcher.ontology;

/*-
 * #%L
 * fuse-core
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.OntologyFinalizer;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class DirectoryOntologyProvider implements OntologyProvider {
    //region Constructors
    public DirectoryOntologyProvider(String dirName) throws URISyntaxException {
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
                    .filter(file -> !FilenameUtils.getBaseName(file.getName()).toLowerCase().contains("transformation"))
                    .toJavaMap(file -> {
                        try {
                            return new Tuple2<>(FilenameUtils.getBaseName(file.getName()),
                                    OntologyFinalizer.finalize(mapper.readValue(file, Ontology.class)));
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
    //endregion

    //region Fields
    private Map<String,Ontology> ontologies;
    //endregion
}
