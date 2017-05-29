package com.kayhut.fuse.executor.ontology;

import com.kayhut.fuse.model.ontology.Ontology;
import org.unipop.structure.UniGraph;

/**
 * Created by Roman on 06/04/2017.
 */
public interface UniGraphProvider {
    UniGraph getGraph(Ontology ontology) throws Exception;
}
