package com.kayhut.fuse.generator.generator.graph;

import com.kayhut.fuse.generator.configuration.PersonConfiguration;
import com.kayhut.fuse.generator.generator.entity.PersonGeneratorBase;
import com.kayhut.fuse.generator.generator.model.ScaleFreeModel;
import com.kayhut.fuse.generator.generator.model.barbasi.albert.graphstream.GraphstreamHelper;
import com.kayhut.fuse.generator.generator.model.barbasi.albert.hadian.generators.BAGraphGenerator;
import com.kayhut.fuse.generator.model.entity.Person;
import com.kayhut.fuse.generator.model.relation.RelationBase;
import javaslang.Tuple2;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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
                new PersonGeneratorBase(personConfiguration)
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
    protected Person buildEntityNode(String id) {
        Person person = entityGenerator.generate();
        person.setId(id);
        return person;
    }

    @Override
    protected RelationBase buildEntityRelation(String sourceId, String targetId) {
        return null;
    }

    @Override
    protected void writeGraph(List<String> nodesList, List<Tuple2> edgesList) {

    }


    //endregion

    //region Private Methods

    //endregion


}
