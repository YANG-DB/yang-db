package com.kayhut.fuse.gta;

import com.google.inject.Inject;
import com.kayhut.fuse.executor.uniGraphProvider.UniGraphProvider;
import com.kayhut.fuse.gta.translation.SimplePlanOpTranslator;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.PhysicalIndexProvider;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;

/**
 * Created by moti on 3/7/2017.
 */
public class GremlinTranslationAppenderEngine implements GremlinTranslator {

    @Inject
    public GremlinTranslationAppenderEngine(
            UniGraphProvider uniGraphProvider) {
        this.uniGraphProvider = uniGraphProvider;
    }

    @Override
    public Traversal<Element, Path> translate(Ontology ontology, Plan plan){
        // Create initial traversal
        GraphTraversal graphTraversal = __.start();
        try {
            return new SimplePlanOpTranslator(this.uniGraphProvider.getGraph(ontology)).translate(plan, graphTraversal, ontology);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //region Fields
    private UniGraphProvider uniGraphProvider;
    //endregion
}
