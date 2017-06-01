package com.kayhut.fuse.generator.data.generation.graph;

import com.kayhut.fuse.generator.data.generation.entity.EntityGeneratorBase;
import com.kayhut.fuse.generator.data.generation.model.BaseModel;
import com.kayhut.fuse.generator.model.relation.RelationBase;
import javaslang.Tuple2;
import org.graphstream.graph.Graph;

import java.util.List;

/**
 * Created by benishue on 15-May-17.
 */

/**
 * @param <C> - Configuration
 * @param <E> - Entity (e.g., Dragon, Person)
 */
public abstract class GraphGeneratorBase<C,E> {

    protected static final int BUFFER = 1000;

    //region Ctrs
    public GraphGeneratorBase(C configuration, BaseModel model, EntityGeneratorBase entityGenerator) {
        this.configuration = configuration;
        this.entityGenerator = entityGenerator;
        this.model = model;
    }
    //endregion

    //region Abstract Methods

    /**
     * Intended for small-medium graphs, In-Memory graph.
     * We can do SNA on the graph, export graph, draw graph, etc...
     * @return Graph (Graphstream Barabasi- Albert Graph)
     */

    protected abstract Graph generateGraph();
    /**
     * Intended for massive graphs.
     * Only Edges list are built and not full graph.
     * Refernce:
     * author = {Hadian, Ali and Nobari, Sadegh and Minaei-Bidgoli, Behrooz and Qu, Qiang},
     * Title = {ROLL: Fast In-Memory Generation of Gigantic Scale-free Networks}
     * Source Code: https://github.com/alihadian/ROLL
     */
    protected abstract void generateMassiveGraph();

    /**
     * @param id Entity (Node) id
     * @return New Fake Entity (e.g., Dragon, Person)
     */
    protected abstract E buildEntityNode(String id);

    /**
     * @param sourceId Source Node Id
     * @param targetId Target Node Id
     * @return Relation between the source node (Entity X) and the target node (Entity Y)
     */
    protected abstract RelationBase buildEntityRelation(String sourceId, String targetId);

    /**
     * Write the graph to the file system
     * @param nodesList list of nodes ids
     * @param edgesList list of edges (source id, target id)
     */
    protected abstract void writeGraph(List<String> nodesList, List<Tuple2> edgesList);
    //endregion

    //region Getters
    protected C getConfiguration() {
        return configuration;
    }

    protected BaseModel getModel() {
        return model;
    }

    protected EntityGeneratorBase getEntityGenerator() {
        return entityGenerator;
    }
    //endregion

    //region Fields
    protected final C configuration;
    protected final BaseModel model;
    protected final EntityGeneratorBase<C,E> entityGenerator;
    //endregion

}