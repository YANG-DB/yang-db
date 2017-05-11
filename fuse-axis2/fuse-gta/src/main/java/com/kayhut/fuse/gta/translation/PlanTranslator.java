package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.ontology.Ontology;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Element;

/**
 * Created by Roman on 10/05/2017.
 */
public interface PlanTranslator {
    Traversal<Element, Path> translate(Plan plan, Ontology ontology) throws Exception;
}
