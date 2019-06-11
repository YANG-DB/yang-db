package com.kayhut.fuse.dispatcher.ontology;

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
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;
import com.kayhut.fuse.model.ontology.transformer.OntologyTransformer;
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
public class DirectoryOntologyTransformerProvider implements OntologyTransformerProvider {
    //region Constructors
    public DirectoryOntologyTransformerProvider(String dirName) throws URISyntaxException {
        this.transformations = new HashMap<>();
        String currentDir = System.getProperty("user.dir");
        ObjectMapper mapper = new ObjectMapper();

        File dir = new File(Paths.get(currentDir, dirName).toString());
        if (!dir.exists()) {
            dir = new File(Thread.currentThread().getContextClassLoader().getResource(dirName).toURI());
        }
        if (dir.exists()) {
            this.transformations =
                    Stream.of(Objects.requireNonNull(dir.listFiles() == null ? new File[0] : dir.listFiles()))
                            .filter(file -> FilenameUtils.getExtension(file.getName()).equals("json"))
                            .filter(file -> FilenameUtils.getBaseName(file.getName()).toLowerCase().contains("transformation"))
                            .toJavaMap(file -> {
                                try {
                                    OntologyTransformer ontologyTransformer = mapper.readValue(file, OntologyTransformer.class);
                                    return new Tuple2<>(ontologyTransformer.getOnt(),ontologyTransformer);
                                } catch (IOException e) {
                                    return new Tuple2<>(FilenameUtils.getBaseName(file.getName()), new OntologyTransformer());
                                }
                            });
        }
    }
    //endregion

    //region Fields
    private Map<String, OntologyTransformer> transformations;

    @Override
    public Optional<OntologyTransformer> transformer(String id) {
        return Optional.ofNullable(this.transformations.get(id));

    }

    @Override
    public Collection<OntologyTransformer> transformation() {
        return Collections.unmodifiableCollection(transformations.values());
    }
//endregion
}
