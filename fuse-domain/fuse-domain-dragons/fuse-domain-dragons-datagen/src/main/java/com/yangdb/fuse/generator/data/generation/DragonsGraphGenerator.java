package com.yangdb.fuse.generator.data.generation;

/*-
 * #%L
 * fuse-domain-dragons-datagen
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import com.google.common.base.Stopwatch;
import com.yangdb.fuse.generator.configuration.DragonConfiguration;
import com.yangdb.fuse.generator.data.generation.entity.DragonGenerator;
import com.yangdb.fuse.generator.data.generation.graph.GraphGeneratorBase;
import com.yangdb.fuse.generator.data.generation.scale.free.ScaleFreeModel;
import com.yangdb.fuse.generator.data.generation.scale.free.barbasi.albert.graphstream.GraphstreamHelper;
import com.yangdb.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.generators.BAGraphGenerator;
import com.yangdb.fuse.generator.model.entity.Dragon;
import com.yangdb.fuse.generator.model.enums.RelationType;
import com.yangdb.fuse.generator.model.relation.Fires;
import com.yangdb.fuse.generator.model.relation.Freezes;
import com.yangdb.fuse.generator.model.relation.RelationBase;
import com.yangdb.fuse.generator.util.DateUtil;
import com.yangdb.fuse.generator.util.RandomUtil;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.math3.util.Pair;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yangdb.fuse.generator.util.CsvUtil.appendResults;


/**
 * Created by benishue on 15-May-17.
 */
public class DragonsGraphGenerator extends GraphGeneratorBase<DragonConfiguration, Dragon> {

    public static final String[] DRAGON_HEADER = {"id", "name", "birthDate", "power", "gender", "color"};
    public static final String[] DRAGONS_FIRE_HEADER = {"id", "entityA.id", "entityA.type", "entityB.id", "entityB.type", "date", "temp"};
    public static final String[] DRAGON_FREEZE_HEADER = {"id", "entityA.id", "entityA.type", "entityB.id", "entityB.type", "from", "to"};
    private final Logger logger = LoggerFactory.getLogger(DragonsGraphGenerator.class);

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
        this.dragonConf = dragonConfiguration;
    }
    //endregion

    //region Public Methods


    public void generateSmallDragonsGraph(Optional<String> resultsPath, boolean drawGraph) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            DragonsGraphGenerator dragonsGraphGenerator = new DragonsGraphGenerator(dragonConf);
            Graph dragonsInteractionGraph = dragonsGraphGenerator.generateGraph();
            stopwatch.stop();
            long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
            logger.info("Dragons model V1 generation took (seconds): {}", elapsed);

            GraphstreamHelper.printScaleFreeDataSummary(dragonsInteractionGraph, resultsPath);
            if (drawGraph) {
                GraphstreamHelper.drawGraph(dragonsInteractionGraph, logger);
                System.in.read();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public List<String> generateMassiveDragonsGraph() {
        List<String> nodesIds = new ArrayList<>();
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            DragonsGraphGenerator dragonsGraphGenerator = new DragonsGraphGenerator(dragonConf);
            nodesIds = dragonsGraphGenerator.generateMassiveGraph();
            stopwatch.stop();
            long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
            logger.info("Dragons massive graph generation took (seconds): {}", elapsed);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return nodesIds;
    }

    //endregion

    //region Overridden Methods
    @Override
    protected Graph generateGraph() {
        Graph graph = GraphstreamHelper.generateGraph(getModel().getModelName() + "_Dragons"
                , getModel().getNumOfNodes(), ((ScaleFreeModel) getModel()).getEdgesPerNode());

        List<String> nodesList = graph.getNodeSet().stream().map(Node::getId).collect(Collectors.toList());
        List<Tuple2> edgesList = graph.getEdgeSet().stream().map(edge ->
                new Tuple2<>(edge.getSourceNode().getId(), edge.getTargetNode().getId())).collect(Collectors.toList());

        writeGraph(nodesList, edgesList);
        return graph;
    }


    @Override
    protected List<String> generateMassiveGraph() {
        List<Tuple2> edgesList = BAGraphGenerator.generateMassiveBAgraph(
                model.getNumOfNodes(),
                ((ScaleFreeModel) model).getEdgesPerNode(),
                BAGraphGenerator.SamplingMode.ROLL_TREE,
                configuration.getRelationsFilePath());


        Set<Long> tempSet = new LinkedHashSet<>(Stream.ofAll(edgesList).map(tuple2 -> (Long) tuple2._1).toJavaList());
        tempSet.addAll(Stream.ofAll(edgesList).map(tuple2 -> (Long) tuple2._2).toJavaList());
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
        ArrayList<Pair<RelationType, Double>> probs = new ArrayList<>(
                Arrays.asList(new Pair<>(RelationType.FIRES,configuration.getFireProbability()),
                              new Pair<>(RelationType.FREEZES, configuration.getFreezProbability())
                        ));
        RelationType relationType = RandomUtil.enumeratedDistribution(probs);
        Date date = RandomUtil.randomDate(configuration.getStartDateOfStory(), configuration.getEndDateOfStory());
        int temperature = RandomUtil.randomInt(configuration.getFireMinTemperature(), configuration.getFireMaxTemperature());
        RelationBase relationBase = null;

        if (relationType == RelationType.FIRES) {
            relationBase = new Fires(edgeId, sourceId, targetId, date, temperature);
        }

        if (relationType == RelationType.FREEZES) {
            Date dateTill = DateUtil.addMinutesToDate(date, RandomUtil.randomInt(1, configuration.getFreezMaxDuraution()));
            relationBase = new Freezes(edgeId, sourceId, targetId, date, dateTill,temperature);
        }
        return relationBase;
    }

    @Override
    protected void writeGraph(List<String> nodesList, List<Tuple2> edgesList) {
        List<String[]> peopleRecords = new ArrayList<>();
        List<String[]> dragonsRecords = new ArrayList<>();

        List<String[]> dragonsFiresRecords = new ArrayList<>();
        List<String[]> dragonsFreezeRecords = new ArrayList<>();

        //add headers
        peopleRecords.add(0, DRAGON_HEADER);
        dragonsRecords.add(0,DRAGON_HEADER);
        dragonsFiresRecords.add(0, DRAGONS_FIRE_HEADER);
        dragonsFreezeRecords.add(0, DRAGON_FREEZE_HEADER);

        String fireRelationsFile = configuration.getRelationsFilePath().replace(".csv", "") + "_" + RelationType.FIRES + ".csv";
        String freezeRelationsFile = configuration.getRelationsFilePath().replace(".csv", "") + "_" + RelationType.FREEZES + ".csv";
        String entitiesFile = configuration.getEntitiesFilePath();

        for (String nodeId : nodesList) {
            dragonsRecords.add(buildEntityNode(nodeId).getRecord());
            if (dragonsRecords.size() % BUFFER == 0) { //BUFFER
                System.out.println(".");
                appendResults(dragonsRecords, entitiesFile);
                dragonsRecords.clear();
            }
        }

        for (Tuple2 edge : edgesList) {
            int numOfInteractions = RandomUtil.randomInt(configuration.getMinUniqueInteractions(), configuration.getMaxUniqueInteractions());
            for (int i = 0; i < numOfInteractions; i++) {
                String sourceId = edge._1.toString();
                String targetId = edge._2.toString();
                String edgeId = sourceId + "_" + targetId + "_" + i;
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

        appendResults(dragonsFiresRecords, fireRelationsFile);
        appendResults(dragonsRecords, entitiesFile);
        appendResults(dragonsFreezeRecords, freezeRelationsFile);

    }

    @Override
    protected void writeCSVs(List<Dragon> elements) {
        //todo
    }
    //endregion

    //region Private Methods

    //endregion

    //region Fields
    private final DragonConfiguration dragonConf;
    //endregion
}
