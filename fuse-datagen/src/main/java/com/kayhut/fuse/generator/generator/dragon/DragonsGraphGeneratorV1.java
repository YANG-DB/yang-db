package com.kayhut.fuse.generator.generator.dragon;

import com.kayhut.fuse.generator.generator.graph.barbasi.albert.graphstream.BarabasiAlbertGenerator;
import com.kayhut.fuse.generator.model.entity.Dragon;
import com.kayhut.fuse.generator.model.graph.NodesEdges;
import com.kayhut.fuse.generator.model.relation.RelationBase;
import com.kayhut.fuse.generator.util.CSVUtil;
import com.kayhut.fuse.generator.util.RandomUtil;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;



/**
 * Created by benishue on 15-May-17.
 */
public class DragonsGraphGeneratorV1 extends DragonsGraphGeneratorBase{

    //region Ctrs
    public DragonsGraphGeneratorV1(DragonConfiguration dragonConfiguration) {
       super(dragonConfiguration, "Dragons-Interactions-GraphV1");
    }
    //endregion

    //region Public Methods
    public Graph generateDragonsGraph(){
        Graph graph = new SingleGraph(getModel().getModelName());
        BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator(getModel().getEdgesPerNode());
        gen.addSink(graph);

        NodesEdges firstNodesEdges = gen.init();
        buildNodesEdges(firstNodesEdges);

        int size = getModel().getNumOfNodes();
        while (size-- > 0) {
            NodesEdges nodesEdges = gen.evolve();
            buildNodesEdges(nodesEdges);
        }
        gen.end();

        return graph;
    }
    //endregion

    //region Private Methods
    private void buildNodesEdges(NodesEdges nodesEdges){
        nodesEdges.getNodes().forEach(node -> {
            Dragon dragon = buildDragonNode(node.getNodeId());
            CSVUtil.appendResult(dragon.getRecord(), getDragonConfiguration().getDragonsResultsFilePath());
        });
        nodesEdges.getEdges().forEach(edge -> {
            int numOfInteractions = RandomUtil.randomInt(getDragonConfiguration().getMinUniqueInteractions(), getDragonConfiguration().getMaxUniqueInteractions());
            for (int i = 0; i < numOfInteractions; i++) {
                RelationBase rel = buildDragonRelation(edge.getSource(), edge.getTarget());
                String relationsResultsFile = getDragonConfiguration().getDragonsRelationsFilePath().replace(".csv","") + "_" + rel.getRelationType() + ".csv";
                CSVUtil.appendResult(rel.getRecord(), relationsResultsFile);
            }
        });
    }
    //endregion

}
