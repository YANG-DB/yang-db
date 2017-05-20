package com.kayhut.fuse.generator.generator.dragon;

import com.kayhut.fuse.generator.configuration.DragonConfiguration;
import com.kayhut.fuse.generator.generator.graph.barbasi.albert.graphstream.BarabasiAlbertGenerator;
import com.kayhut.fuse.generator.model.enums.RelationType;
import com.kayhut.fuse.generator.model.graph.NodesEdges;
import com.kayhut.fuse.generator.model.relation.RelationBase;
import com.kayhut.fuse.generator.util.CSVUtil;
import com.kayhut.fuse.generator.util.RandomUtil;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by benishue on 15-May-17.
 */
public class DragonsGraphGeneratorV1 extends DragonsGraphGeneratorBase {

    //region Ctrs
    public DragonsGraphGeneratorV1(DragonConfiguration dragonConfiguration) {
        super(dragonConfiguration, "Dragons-Interactions-GraphV1");
    }
    //endregion

    //region Public Methods
    public Graph generateDragonsGraph() {
        List<NodesEdges> nodesEdgesList = new ArrayList<>();
        Graph graph = new SingleGraph(getModel().getModelName());
        BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator(getModel().getEdgesPerNode());
        gen.addSink(graph);

        NodesEdges firstNodesEdges = gen.init();
        nodesEdgesList.add(firstNodesEdges);

        int size = getModel().getNumOfNodes();
        while (size-- > 0) {
            NodesEdges nodesEdges = gen.evolve();
            nodesEdgesList.add(nodesEdges);
            if (nodesEdgesList.size() % 1000 == 0) { //BUFFER
                writeNodesEdgesData(nodesEdgesList);
                nodesEdgesList.clear();
            }
        }
        writeNodesEdgesData(nodesEdgesList);
        gen.end();

        return graph;
    }
    //endregion

    //region Private Methods
    private void writeNodesEdgesData(List<NodesEdges> nodesEdgesList) {
        List<String[]> dragonsRecords = new ArrayList<>();
        List<String[]> dragonsFiresRecords = new ArrayList<>();
        List<String[]> dragonsFreezeRecords = new ArrayList<>();

        for (NodesEdges nodesEdges : nodesEdgesList) {
            nodesEdges.getNodes().forEach(node -> dragonsRecords.add(buildDragonNode(node.getNodeId()).getRecord()));

            nodesEdges.getEdges().forEach(edge -> {
                int numOfInteractions = RandomUtil.randomInt(getDragonConfiguration().getMinUniqueInteractions(), getDragonConfiguration().getMaxUniqueInteractions());
                for (int i = 0; i < numOfInteractions; i++) {
                    RelationBase rel = buildDragonRelation(edge.getSource(), edge.getTarget());
                    if (rel.getRelationType() == RelationType.FIRES)
                        dragonsFiresRecords.add(rel.getRecord());
                    if (rel.getRelationType() == RelationType.FREEZES)
                        dragonsFreezeRecords.add(rel.getRecord());
                }
            });
        }
        CSVUtil.appendResults(dragonsRecords, getDragonConfiguration().getDragonsResultsFilePath());
        CSVUtil.appendResults(dragonsFiresRecords,
                getDragonConfiguration().getDragonsRelationsFilePath().replace(".csv", "") + "_" + RelationType.FIRES + ".csv");
        CSVUtil.appendResults(dragonsFreezeRecords,
                getDragonConfiguration().getDragonsRelationsFilePath().replace(".csv", "") + "_" + RelationType.FREEZES + ".csv");

    }
    //endregion

}
