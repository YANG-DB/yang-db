package com.kayhut.fuse.generator.data.generation;

import com.google.common.base.Stopwatch;
import com.kayhut.fuse.generator.configuration.PersonConfiguration;
import com.kayhut.fuse.generator.data.generation.entity.PersonGenerator;
import com.kayhut.fuse.generator.data.generation.graph.GraphGeneratorBase;
import com.kayhut.fuse.generator.data.generation.scale.free.ScaleFreeModel;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.graphstream.GraphstreamHelper;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.generators.BAGraphGenerator;
import com.kayhut.fuse.generator.model.entity.Person;
import com.kayhut.fuse.generator.model.enums.RelationType;
import com.kayhut.fuse.generator.model.relation.Knows;
import com.kayhut.fuse.generator.model.relation.RelationBase;
import com.kayhut.fuse.generator.util.RandomUtil;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.configuration.Configuration;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.kayhut.fuse.generator.util.CsvUtil.appendResults;


/**
 * Created by benishue on 15-May-17.
 */
public class PersonsGraphGenerator extends GraphGeneratorBase<PersonConfiguration, Person> {

    private  final Logger logger = LoggerFactory.getLogger(PersonsGraphGenerator.class);

    //region Ctrs
    public PersonsGraphGenerator(final PersonConfiguration personConfiguration) {
        super(
                personConfiguration,
                new ScaleFreeModel(
                        "Persons-Interactions-Graph",
                        personConfiguration.getEdgesPerNode(),
                        personConfiguration.getNumberOfNodes()),
                new PersonGenerator(personConfiguration)
        );
        personConf = personConfiguration;
    }
    //endregion

    //region Public Methods
    public List<String> generatePersonsGraph() {
        List<String> nodesIds = new ArrayList<>();
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            PersonsGraphGenerator personsGraphGenerator = new PersonsGraphGenerator(personConf);
            nodesIds = personsGraphGenerator.generateMassiveGraph();
            stopwatch.stop();
            long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
            logger.info("Persons massive graph generation took (seconds): %d", elapsed);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return nodesIds;
    }

    public Map<String, List<String>> attachDragonsToPersons(List<String> dragonsIdList,
                                                              List<String> personsIdList,
                                                              double meanDragonsPerPerson,
                                                              double sdDragonsPerPerson) {


        return attachAnimalToPersons(dragonsIdList, personsIdList, meanDragonsPerPerson, sdDragonsPerPerson);
    }

    public Map<String, List<String>> attachHorsesToPersons(List<String> horsesIdList,
                                                             List<String> personsIdList,
                                                             double meanHorsesPerPerson,
                                                             double sdHorsesPerPerson) {

        return attachAnimalToPersons(horsesIdList, personsIdList, meanHorsesPerPerson, sdHorsesPerPerson);
    }

    //endregion

    //region Overridden Methods
    @Override
    protected Graph generateGraph() {
        Graph graph = GraphstreamHelper.generateGraph(getModel().getModelName() + "-Persons"
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
    protected Person buildEntityNode(String id) {
        Person person = entityGenerator.generate();
        person.setId(id);
        return person;
    }

    @Override
    protected RelationBase buildEntityRelation(String sourceId, String targetId, String edgeId) {
        Date since = RandomUtil.randomDate(configuration.getStartDateOfStory(), configuration.getEndDateOfStory());
        return new Knows(edgeId, sourceId, targetId, since);
    }

    @Override
    protected void writeGraph(List<String> nodesList, List<Tuple2> edgesList) {
        List<String[]> personsRecords = new ArrayList<>();
        List<String[]> personsKnowsRecords = new ArrayList<>();

        String knowsRelationsFile = configuration.getRelationsFilePath().replace(".csv", "") + "_" + RelationType.KNOWS + ".csv";
        String entitiesFile = configuration.getEntitiesFilePath();

        for (String nodeId : nodesList) {
            personsRecords.add(buildEntityNode(nodeId).getRecord());
            if (personsRecords.size() % BUFFER == 0) { //BUFFER
                appendResults(personsRecords, entitiesFile);
                personsRecords.clear();
            }
        }

        for (Tuple2 edge : edgesList) {
            String sourceId = edge._1.toString();
            String targetId = edge._2.toString();
            String edgeId = sourceId + "_" + targetId;
            RelationBase rel = buildEntityRelation(sourceId, targetId, edgeId);
            personsKnowsRecords.add(rel.getRecord());
            if ((personsKnowsRecords.size()) % BUFFER == 0) { //BUFFER
                appendResults(personsKnowsRecords, knowsRelationsFile);
                personsKnowsRecords.clear();
            }
        }

        appendResults(personsRecords, entitiesFile);
        appendResults(personsKnowsRecords, knowsRelationsFile);
    }
    //endregion

    //region Private Methods
    private Map<String, List<String>> attachAnimalToPersons(List<String> animalsIdList,
                                                              List<String> personsIdList,
                                                              double meanAnimalsPerPerson,
                                                              double sdAnimalsPerPerson) {
        Map<String, List<String>> animalsToPersonsSet = new HashMap<>();

        //Deep Copy
        List<String> animalsIdsClone = new ArrayList<>();
        for (String d : animalsIdList) {
            animalsIdsClone.add(d);
        }

        //Generate Gaussian of numbers that will represent the ownership of dragons by person
        List<Double> gaussianDist = RandomUtil.randomGaussianNumbers(meanAnimalsPerPerson, sdAnimalsPerPerson, personsIdList.size());

        //Shuffle the clone list to make it random assignment
        Collections.shuffle(animalsIdsClone);

        //This will tell us if the animal is already assigned to someone
        Map<String, Boolean> assignedAnimals = new HashMap<>();

        int selectionStartIndex = 0;
        //The gaussian Dist list is the same size of the persons ids
        for (int i = 0; i < gaussianDist.size(); i++) {
            int randNumOfAnimalsPerPerson = (int) Math.floor(gaussianDist.get(i));
            String personId = personsIdList.get(i);

            //if the ending index is larger than list size select the Min
            int selectionEndIndex = Math.min(selectionStartIndex + randNumOfAnimalsPerPerson, animalsIdsClone.size() - 1);
            if (selectionStartIndex > selectionEndIndex)
                selectionEndIndex = selectionStartIndex;
            List<String> selectedAnimalsIds = animalsIdsClone.subList(selectionStartIndex, selectionEndIndex);
            selectionStartIndex = selectionEndIndex;

            for (String dragonId : selectedAnimalsIds) {
                if (animalsToPersonsSet.get(personId) == null) {
                    animalsToPersonsSet.put(personId, new ArrayList<>());
                    if (!assignedAnimals.containsKey(dragonId)) {
                        animalsToPersonsSet.get(personId).add(dragonId);
                        assignedAnimals.put(dragonId, true);
                    }
                } else {
                    List<String> animalsPerPerson = animalsToPersonsSet.get(personId);
                    if (!animalsPerPerson.contains(dragonId) && !assignedAnimals.containsKey(dragonId)) {
                        animalsToPersonsSet.get(personId).add(dragonId);
                        assignedAnimals.put(dragonId, true);
                    }
                }
            }
        }
        return animalsToPersonsSet;
    }


    //endregion

    //region Fields
    private final PersonConfiguration personConf;
    //endregion

}
