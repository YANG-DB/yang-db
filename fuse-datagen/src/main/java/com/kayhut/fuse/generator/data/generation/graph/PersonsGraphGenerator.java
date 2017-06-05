package com.kayhut.fuse.generator.data.generation.graph;

import com.kayhut.fuse.generator.configuration.PersonConfiguration;
import com.kayhut.fuse.generator.data.generation.entity.PersonGenerator;
import com.kayhut.fuse.generator.data.generation.scale.free.ScaleFreeModel;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.graphstream.GraphstreamHelper;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.hadian.generators.BAGraphGenerator;
import com.kayhut.fuse.generator.model.entity.Person;
import com.kayhut.fuse.generator.model.enums.RelationType;
import com.kayhut.fuse.generator.model.relation.Knows;
import com.kayhut.fuse.generator.model.relation.RelationBase;
import com.kayhut.fuse.generator.util.DateUtil;
import com.kayhut.fuse.generator.util.RandomUtil;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.math3.util.Pair;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.kayhut.fuse.generator.util.CSVUtil.appendResults;


/**
 * Created by benishue on 15-May-17.
 */
public class PersonsGraphGenerator extends GraphGeneratorBase<PersonConfiguration, Person> {

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
    }
    //endregion

    //region Public Methods

    @Override
    public Graph generateGraph() {
        Graph graph = GraphstreamHelper.generateGraph(getModel().getModelName() + "-Persons"
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
    public void writeGraph(List<String> nodesList, List<Tuple2> edgesList) {
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

    //endregion


}
