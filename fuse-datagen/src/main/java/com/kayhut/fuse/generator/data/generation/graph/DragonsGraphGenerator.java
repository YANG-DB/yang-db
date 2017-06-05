package com.kayhut.fuse.generator.data.generation.graph;

import com.kayhut.fuse.generator.configuration.DragonConfiguration;
import com.kayhut.fuse.generator.data.generation.entity.DragonGenerator;
import com.kayhut.fuse.generator.data.generation.scale.free.ScaleFreeModel;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.graphstream.GraphstreamHelper;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.generators.BAGraphGenerator;
import com.kayhut.fuse.generator.model.entity.Dragon;
import com.kayhut.fuse.generator.model.enums.RelationType;
import com.kayhut.fuse.generator.model.relation.Fires;
import com.kayhut.fuse.generator.model.relation.Freezes;
import com.kayhut.fuse.generator.model.relation.RelationBase;
import com.kayhut.fuse.generator.util.DateUtil;
import com.kayhut.fuse.generator.util.RandomUtil;
import javaslang.Tuple2;
import javaslang.collection.Stream;
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
    public List<String> generateMassiveGraph() {
        List<Tuple2> edgesList = BAGraphGenerator.generateMassiveBAgraph(
                model.getNumOfNodes(),
                ((ScaleFreeModel) model).getEdgesPerNode(),
                BAGraphGenerator.SamplingMode.ROLL_TREE,
                configuration.getRelationsFilePath());


        Set<Long> tempSet = new LinkedHashSet<>(Stream.ofAll(edgesList).map(tuple2 -> (Long)tuple2._1).toJavaList());
        tempSet.addAll(Stream.ofAll(edgesList).map(tuple2 -> (Long)tuple2._2).toJavaList());
        List<Long> nodeNumericIds = new ArrayList<>(tempSet);
        Collections.sort(nodeNumericIds);
        List<String> nodesList = nodeNumericIds.stream().map(Object::toString).collect(Collectors.toList());

        writeGraph(nodesList, edgesList);

        return nodesList;
    }

    @Override
    protected Dragon buildEntityNode(String id) {
        Dragon dragon = entityGenerator.generate();
        dragon.setId(id);
        return dragon;
    }

    @Override
    protected RelationBase buildEntityRelation(String sourceId, String targetId, String edgeId) {
        ArrayList<Pair<RelationType, Double>> probs = new ArrayList<>(Arrays.asList(new Pair<>(RelationType.FIRES,
                        configuration.getFireProbability()),
                new Pair<>(RelationType.FREEZES, configuration.getFreezProbability())));
        RelationType relationType = RandomUtil.enumeratedDistribution(probs);
        Date date = RandomUtil.randomDate(configuration.getStartDateOfStory(), configuration.getEndDateOfStory());
        int temperature = RandomUtil.randomInt(configuration.getFireMinTemperature(), configuration.getFireMaxTemperature());
        RelationBase relationBase = null;

        if (relationType == RelationType.FIRES) {
            relationBase = new Fires(edgeId, sourceId, targetId, date, temperature);
        }

        if (relationType == RelationType.FREEZES) {
            Date dateTill = DateUtil.addMinutesToDate(date, RandomUtil.randomInt(1, configuration.getFreezMaxDuraution()));
            relationBase = new Freezes(edgeId, sourceId, targetId, date, dateTill);
        }
        return relationBase;
    }

    @Override
    public void writeGraph(List<String> nodesList, List<Tuple2> edgesList) {
        List<String[]> dragonsRecords = new ArrayList<>();
        List<String[]> dragonsFiresRecords = new ArrayList<>();
        List<String[]> dragonsFreezeRecords = new ArrayList<>();
        String fireRelationsFile = configuration.getRelationsFilePath().replace(".csv", "") + "_" + RelationType.FIRES + ".csv";
        String freezeRelationsFile = configuration.getRelationsFilePath().replace(".csv", "") + "_" + RelationType.FREEZES + ".csv";
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
                String sourceId = edge._1.toString();
                String targetId = edge._2.toString();
                String edgeId = sourceId + "_" + targetId + "_" + Integer.toString(i);
                RelationBase rel = buildEntityRelation(sourceId, targetId, edgeId);
                if (rel.getRelationType() == RelationType.FIRES) {
                    dragonsFiresRecords.add(rel.getRecord());
                }
                if (rel.getRelationType() == RelationType.FREEZES) {
                    dragonsFreezeRecords.add(rel.getRecord());
                }

                if ((dragonsFiresRecords.size() + dragonsFreezeRecords.size()) % BUFFER == 0) { //BUFFER
                    appendResults(dragonsFiresRecords, fireRelationsFile);
                    appendResults(dragonsFreezeRecords, freezeRelationsFile);
                    dragonsFreezeRecords.clear();
                    dragonsFiresRecords.clear();
                }
            }
        }

        appendResults(dragonsRecords, entitiesFile);
        appendResults(dragonsFiresRecords, fireRelationsFile);
        appendResults(dragonsFreezeRecords, freezeRelationsFile);

    }
    //endregion

    //region Private Methods

    //endregion
}
