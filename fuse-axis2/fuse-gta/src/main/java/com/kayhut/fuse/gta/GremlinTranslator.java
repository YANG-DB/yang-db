package com.kayhut.fuse.gta;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.ontology.Ontology;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Element;

/**
 * Created by Roman on 06/04/2017.
 */
public interface GremlinTranslator {
    Traversal<Element, Path> translate(Ontology ontology, Plan plan);
}
