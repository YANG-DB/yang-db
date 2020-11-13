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
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.IndexProvider;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class DirectoryIndexProvider implements IndexProviderFactory {
    //region Constructors
    public DirectoryIndexProvider(String dirName) throws URISyntaxException {
        this.map = new HashMap<>();
        String currentDir = System.getProperty("user.dir");
        ObjectMapper mapper = new ObjectMapper();

        File dir = new File(Paths.get(currentDir, dirName).toString());
        if(!dir.exists()) {
            URL resource = Thread.currentThread().getContextClassLoader().getResource(dirName);
            if(Objects.isNull(resource))
                throw new FuseError.FuseErrorException("No file resource was found for "+dirName+" during creation of IndexProvider factory...",new IllegalStateException("No file resource found ["+dirName+"]"));

            dir = new File(resource.toURI());
        }
        if (dir.exists()) {
            this.map =
                    Stream.of(dir.listFiles() == null ? new File[0] : dir.listFiles())
                    .filter(file -> FilenameUtils.getExtension(file.getName()).equals("conf"))
                    .map(file -> {
                        try {
                            return mapper.readValue(file, IndexProvider.class);
                        } catch (IOException e) {
                            throw new FuseError.FuseErrorException( "Error reading index provider ",e,new FuseError("Error reading index provider ",file.getName()));
                        }
                    }).toJavaMap(provider -> new Tuple2<>(provider.getOntology(), provider));
        }
    }
    //endregion

    //region OntologyProvider Implementation
    @Override
    public Optional<IndexProvider> get(String id) {
        return Optional.ofNullable(this.map.get(id));
    }

    @Override
    public Collection<IndexProvider> getAll() {
        return map.values();
    }

    @Override
    public IndexProvider add(IndexProvider indexProvider) {
        map.put(indexProvider.getOntology(),indexProvider);
        return indexProvider;
    }
    //endregion

    //region Fields
    private Map<String, IndexProvider> map;
    //endregion
}
