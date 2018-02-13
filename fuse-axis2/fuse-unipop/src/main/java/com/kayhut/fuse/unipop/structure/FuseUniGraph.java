package com.kayhut.fuse.unipop.structure;

import com.kayhut.fuse.unipop.process.traversal.dsl.graph.FuseGraphTraversalSource;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.unipop.process.strategyregistrar.StrategyProvider;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.ControllerManagerFactory;
import org.unipop.structure.UniGraph;

/**
 * Created by roman.margolis on 12/02/2018.
 */
public class FuseUniGraph extends UniGraph {
    //region Constructors
    public FuseUniGraph(Configuration configuration, ControllerManagerFactory controllerManagerFactory, StrategyProvider strategyProvider) throws Exception {
        super(configuration, controllerManagerFactory, strategyProvider);
    }
    //endregion

    //region Override Methods
    @Override
    public GraphTraversalSource traversal() {
        return new FuseGraphTraversalSource(this, this.strategies);
        //return new GraphTraversalSource(this, strategies);
    }
    //endregion
}
