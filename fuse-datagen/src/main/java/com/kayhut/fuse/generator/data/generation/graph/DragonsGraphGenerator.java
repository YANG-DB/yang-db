package com.kayhut.fuse.generator.data.generation.graph;

import com.kayhut.fuse.generator.configuration.DragonConfiguration;
import com.kayhut.fuse.generator.data.generation.entity.DragonGenerator;
import com.kayhut.fuse.generator.data.generation.model.ScaleFreeModel;
import com.kayhut.fuse.generator.data.generation.model.barbasi.albert.graphstream.GraphstreamHelper;
import com.kayhut.fuse.generator.data.generation.model.barbasi.albert.hadian.generators.BAGraphGenerator;
import com.kayhut.fuse.generator.model.entity.Dragon;
import com.kayhut.fuse.generator.model.enums.RelationType;
import com.kayhut.fuse.generator.model.relation.Fires;
import com.kayhut.fuse.generator.model.relation.Freezes;
import com.kayhut.fuse.generator.model.relation.RelationBase;
import com.kayhut.fuse.generator.util.DateUtil;
import com.kayhut.fuse.generator.util.RandomUtil;
import javaslang.Tuple2;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.kayhut.fuse.generator.util.CSVUtil.appendResults;


/**
 * Created by benishue on 15-May-17.
 */
public class DragonsGraphGenerator extends GraphGeneratorBase<DragonConfiguration, Dragon> {

    //region Ctrs
    public DragonsGraphGenerator(final DragonConfiguration dragonConfiguration) {
        super(
                dragonConfiguration,
                new ScaleFreeModel(
                        "Dragons-Interactions-Graph",
                        dragonConfiguration.getEdgesPerNode(),
                        dragonConfiguration.getNumberOfNodes()
                ),
                new DragonGenerator(dragonConfiguration)
        );
    }
    //endregion

    //region Public Methods

    @Override
    public Graph generateGraph() {
        Graph graph = GraphstreamHelper.generateGraph(getModel().getModelName() + "_Dragons"
                , getModel().getNumOfNodes(), ((ScaleFreeModel) getModel()).getEdgesPerNode());

        List<String> nodesList = graph.getNodeSet().stream().map(Node::getId).collect(Collectors.toList());
        List<Tuple2> edgesList = graph.getEdgeSet().stream().map(edge ->
                new Tuple2<>(edge.getSourceNode().getId(), edge.getTargetNode().getId())).collect(Collectors.toList());

        writeGraph(nodesList, edgesList);
        return graph;
    }


    @Override
    public void generateMassiveGraph() {
        ArrayList<Tuple2> edgesList = BAGraphGenerator.generateMassiveBAgraph(
                model.getNumOfNodes(),
                ((ScaleFreeModel) model).getEdgesPerNode(),
                BAGraphGenerator.SamplingMode.ROLL_TREE,
                configuration.getRelationsFilePath());

        List<String> nodesList = IntStream.rangeClosed(0, model.getNumOfNodes())
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());

        writeGraph(nodesList, edgesList);
    }

    @Override
    protected Dragon buildEntityNode(String id) {
        Dragon dragon = entityGenerator.generate();
        dragon.setId(id);
        return dragon;
    }

    @Override
    protected RelationBase buildEntityRelation(String sourceId, String targetId) {
        ArrayList<Pair<RelationType, Double>> probs = new ArrayList<>(Arrays.asList(new Pair<>(RelationType.FIRES,
                        configuration.getFireProbability()),
                new Pair<>(RelationType.FREEZES, configuration.getFreezProbability())));
        RelationType relationType = RandomUtil.enumeratedDistribution(probs);
        String edgeId = sourceId + "_" + targetId;
        Date date = RandomUtil.randomDate(configuration.getStartDateOfStory(), configuration.getEndDateOfStory());
        RelationBase relationBase = null;

        if (relationType == RelationType.FIRES) {
            relationBase = new Fires(edgeId, sourceId, targetId, date);
        }

        if (relationType == RelationType.FREEZES) {
            Date dateTill = DateUtil.addMinutesToDate(date, RandomUtil.randomInt(1, configuration.getFreezMaxDuraution()));
            relationBase = new Freezes(edgeId, sourceId, targetId, date, dateTill);
        }
        return relationBase;
    }
    //endregion

    //region Private Methods
    @Override
    public void writeGraph(List<String> nodesList, List<Tuple2> edgesList) {
        List<String[]> dragonsRecords = new ArrayList<>();
        List<String[]> dragonsFiresRecords = new ArrayList<>();
        List<String[]> dragonsFreezeRecords = new ArrayList<>();
        String fireRelationsFile = configuration.getRelationsFilePath().replace(".csv", "") + "_" + RelationType.FIRES + ".csv";
        String freezRelationsFile = configuration.getRelationsFilePath().replace(".csv", "") + "_" + RelationType.FREEZES + ".csv";
        String entitiesFile = configuration.getEntitiesFilePath();

        for (String nodeId : nodesList) {
            dragonsRecords.add(buildEntityNode(nodeId).getRecord());
            if (dragonsRecords.size() % BUFFER == 0) { //BUFFER
                appendResults(dragonsRecords, entitiesFile);
                dragonsRecords.clear();
            }
        }

        for (Tuple2 edge : edgesList) {
            int numOfInteractions = RandomUtil.randomInt(configuration.getMinUniqueInteractions(), configuration.getMaxUniqueInteractions());
            for (int i = 0; i < numOfInteractions; i++) {
                RelationBase rel = buildEntityRelation(edge._1.toString(), edge._2.toString());
                if (rel.getRelationType() == RelationType.FIRES) {
                    dragonsFiresRecords.add(rel.getRecord());
                }
                if (rel.getRelationType() == RelationType.FREEZES) {
                    dragonsFreezeRecords.add(rel.getRecord());
                }

                if ((dragonsFiresRecords.size() + dragonsFreezeRecords.size()) % BUFFER == 0) { //BUFFER
                    appendResults(dragonsFiresRecords, fireRelationsFile);
                    appendResults(dragonsFreezeRecords, freezRelationsFile);
                    dragonsFreezeRecords.clear();
                    dragonsFiresRecords.clear();
                }
            }
        }

        appendResults(dragonsRecords, entitiesFile);
        appendResults(dragonsFiresRecords, fireRelationsFile);
        appendResults(dragonsFreezeRecords, freezRelationsFile);

    }
    //endregion
}
