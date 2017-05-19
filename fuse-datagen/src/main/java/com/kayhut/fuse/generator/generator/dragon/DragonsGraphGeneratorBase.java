package com.kayhut.fuse.generator.generator.dragon;

import com.kayhut.fuse.generator.generator.graph.ScaleFreeModel;
import com.kayhut.fuse.generator.model.entity.Dragon;
import com.kayhut.fuse.generator.model.enums.RelationType;
import com.kayhut.fuse.generator.model.relation.Fires;
import com.kayhut.fuse.generator.model.relation.Freezes;
import com.kayhut.fuse.generator.model.relation.RelationBase;
import com.kayhut.fuse.generator.util.DateUtil;
import com.kayhut.fuse.generator.util.RandomUtil;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by benishue on 15-May-17.
 */
public abstract class DragonsGraphGeneratorBase {

    //region Ctrs
    public DragonsGraphGeneratorBase(DragonConfiguration dragonConfiguration, String graphId) {
       this.dragonConfiguration = dragonConfiguration;
       this.dragonGenerator = new DragonGenerator(dragonConfiguration);
       this.model = new ScaleFreeModel(graphId,
                dragonConfiguration.getEdgesPerNode(),
                dragonConfiguration.getNumberOfNodes());
    }
    //endregion

    //region Private Methods
    protected Dragon buildDragonNode(String id){
        Dragon dragon = dragonGenerator.generateDragon();
        dragon.setId(id);
        return dragon;
    }

    protected RelationBase buildDragonRelation(String sourceId, String targetId){
        ArrayList<Pair<RelationType, Double>> probs = new ArrayList<>(Arrays.asList(new Pair<>(RelationType.FIRES,
                        dragonConfiguration.getFireProbability())
                , new Pair<>(RelationType.FREEZES, dragonConfiguration.getFreezProbability())));
        RelationType relationType = RandomUtil.enumeratedDistribution(probs);
        String edgeId = sourceId + "_" +targetId;
        Date date = RandomUtil.randomDate(dragonConfiguration.getStartDateOfStory(), dragonConfiguration.getEndDateOfStory());
        RelationBase relationBase = null;

        if (relationType == RelationType.FIRES){
            relationBase = new Fires(edgeId, sourceId, targetId, date);
        }

        if (relationType == RelationType.FREEZES){
            Date dateTill = DateUtil.addMinutesToDate(date, RandomUtil.randomInt(1,dragonConfiguration.getFreezMaxDuraution()));
            relationBase = new Freezes(edgeId, sourceId, targetId, date, dateTill);
        }
        return relationBase;
    }
    //endregion

    //region Getters
    protected DragonConfiguration getDragonConfiguration() {
        return dragonConfiguration;
    }

    protected ScaleFreeModel getModel() {
        return model;
    }

    protected DragonGenerator getDragonGenerator() {
        return dragonGenerator;
    }
    //endregion

    //region Fields
    private final DragonConfiguration dragonConfiguration;
    private final ScaleFreeModel model;
    private final DragonGenerator dragonGenerator;

    //endregion

}
