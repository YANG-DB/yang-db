package com.kayhut.fuse.dispatcher.driver;

import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.model.Utils.asObject;
import static com.kayhut.fuse.model.Utils.readJsonFile;

/**
 * Created by liorp on 3/16/2017.
 */
public class SimpleOntologyProvider implements OntologyProvider {
    public static final String DRAGONS = "Dragons";
    public static final String ONTOLOGY = "ontology";

    private Map<String,Ontology> ontologyMap;

    public SimpleOntologyProvider() throws IOException {
        ontologyMap = new HashMap<>();
        Ontology ontology = asObject(readJsonFile(ONTOLOGY + "/" +DRAGONS+".json"), Ontology.class);
        ontology = OntologyFinalizer.finalize(ontology);

        ontologyMap.put(DRAGONS, ontology);
    }

    @Override
    public Optional<Ontology> get(String id) {
        return Optional.of(ontologyMap.get(id));
    }
}
