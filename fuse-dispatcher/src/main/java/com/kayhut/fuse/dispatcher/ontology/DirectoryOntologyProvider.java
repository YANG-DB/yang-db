package com.kayhut.fuse.dispatcher.ontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by roman.margolis on 02/10/2017.
 */
public class DirectoryOntologyProvider implements OntologyProvider {
    //region Constructors
    public DirectoryOntologyProvider(String dirName) {
        this.ontologies = new HashMap<>();
        String currentDir = System.getProperty("user.dir");
        ObjectMapper mapper = new ObjectMapper();

        File dir = new File(Paths.get(currentDir, dirName).toString());
        if (dir.exists()) {
            this.ontologies =
                    Stream.of(dir.listFiles() == null ? new File[0] : dir.listFiles())
                    .filter(file -> FilenameUtils.getExtension(file.getName()).equals("json"))
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
