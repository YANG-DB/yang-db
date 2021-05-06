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
import com.yangdb.fuse.model.ontology.transformer.OntologyTransformer;
import com.yangdb.fuse.model.resourceInfo.FuseError;
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
    private final static ObjectMapper mapper = new ObjectMapper();
    private String dirName;

    //region Constructors
    public DirectoryOntologyTransformerProvider(String dirName) {
        this.dirName = dirName;
        this.transformations = new HashMap<>();
        File dir = null;
        try {
            dir = FileUtils.getOrCreateFile(dirName, System.getProperty("user.dir"),true);
        } catch (IOException e) {
            throw new FuseError.FuseErrorException("Failed reading folder for new Ontology ["+dirName + "] ", e.getCause());
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
