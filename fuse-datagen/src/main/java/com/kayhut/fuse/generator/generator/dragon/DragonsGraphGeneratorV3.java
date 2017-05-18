package com.kayhut.fuse.generator.generator.dragon;

import com.kayhut.fuse.generator.generator.graph.barbasi.albert.hadian.generators.BAGraphGenerator;
import com.kayhut.fuse.generator.model.entity.Dragon;

import com.kayhut.fuse.generator.model.relation.RelationBase;
import com.kayhut.fuse.generator.util.CSVUtil;
import com.kayhut.fuse.generator.util.RandomGenerator;
import javaslang.Tuple2;

import java.util.ArrayList;


/**
 * Created by benishue on 18-May-17.
 */
public class DragonsGraphGeneratorV3 extends DragonsGraphGeneratorBase {

    //region Ctrs
    public DragonsGraphGeneratorV3(DragonConfiguration dragonConfiguration) {
        super(dragonConfiguration, "Dragons-Interactions-GraphV3");
    }
    //endregion

    //region Public Methods
    public void generateDragonsGraph(){
        ArrayList<Tuple2> edgesSet = BAGraphGenerator.generateMassiveBAgraph(
                getModel().getNumOfNodes(),
                getModel().getEdgesPerNode(),
                BAGraphGenerator.SamplingMode.ROLL_TREE,
                "sheker.csv");

        for (int i = 0; i < getModel().getNumOfNodes(); i++) {
            Dragon dragon = buildDragonNode(i + "");
            CSVUtil.appendResult(dragon.getRecord(), getDragonConfiguration().getDragonsResultsFilePath());
        }
        edgesSet.forEach(tuple -> {
            int numOfInteractions = RandomGenerator.randomInt(getDragonConfiguration().getMinUniqueInteractions(), getDragonConfiguration().getMaxUniqueInteractions());
            for (int i = 0; i < numOfInteractions; i++) {
                RelationBase rel = buildDragonRelation(tuple._1.toString(), tuple._2.toString());
                String relationsResultsFile = getDragonConfiguration().getDragonsRelationsFilePath().replace(".csv","") + "_" + rel.getRelationType() + ".csv";
                CSVUtil.appendResult(rel.getRecord(), relationsResultsFile);
            }
        });

    }
    //endregion


}
