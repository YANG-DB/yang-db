package com.yangdb.fuse.executor.ontology;

import com.yangdb.fuse.dispatcher.ontology.InMemoryOntologyProvider;
import com.yangdb.fuse.model.ontology.Ontology;

import java.io.IOException;
import java.util.HashMap;

import static com.yangdb.fuse.model.Utils.asObject;
import static com.yangdb.fuse.model.Utils.readJsonFile;

public class DragonsOntologyProvider extends InMemoryOntologyProvider {
    public static final String DRAGONS = "Dragons";

    public DragonsOntologyProvider(Ontology... ontology) throws IOException {
        super(ontology);
    }

    public DragonsOntologyProvider() throws IOException {
        ontologyMap = new HashMap<>();
        ontologyMap.put(DRAGONS, asObject(readJsonFile(ONTOLOGY + "/" + DRAGONS + ".json"), Ontology.class));
    }
}
