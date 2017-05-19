package com.kayhut.fuse.generator.configuration;


import org.apache.commons.configuration.Configuration;

import java.util.Date;

/**
 * Created by benishue on 18-May-17.
 */
public class DragonConfiguration {

    //region Ctrs
    public DragonConfiguration(Configuration configuration) {
        this.numberOfNodes = configuration.getInt("dragon.numberOfNodes");
        this.edgesPerNode = configuration.getInt("dragon.edgesPerNode");
        this.dragonsResultsFilePath = configuration.getString("resultsPath") + "//" + configuration.getString("dragon.dragonsResultsCsvFileName");
        this.dragonsRelationsFilePath = configuration.getString("resultsPath") + "//" + configuration.getString("dragon.dragonsRelationsCsvFileName");
        this.startDateOfStory = new Date(configuration.getLong("dragon.startDateOfStory")); //01/01/1900 00:00:00 GMT epoch time in milliseconds
        this.endDateOfStory = new Date(configuration.getLong("dragon.endDateOfStory")); //01/01/2000 00:00:00 GMT epoch time in milliseconds
        this.fireProbability = configuration.getDouble("dragon.fireProbability");
        this.freezProbability = configuration.getDouble("dragon.freezProbability");
        this.minUniqueInteractions = configuration.getInt("dragon.minUniqueInteractions");
        this.maxUniqueInteractions = configuration.getInt("dragon.maxUniqueInteractions");
        this.freezMaxDuraution = configuration.getInt("dragon.freezMaxDuraution");
        this.maxPower = configuration.getInt("dragon.maxPower");
        this.minPower = configuration.getInt("dragon.minPower");
    }
    //endregion

    //region Getters
    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public int getEdgesPerNode() {
        return edgesPerNode;
    }

    public Date getStartDateOfStory() {
        return startDateOfStory;
    }

    public Date getEndDateOfStory() {
        return endDateOfStory;
    }

    public int getMinPower() {
        return minPower;
    }

    public int getMaxPower() {
        return maxPower;
    }

    public int getMaxUniqueInteractions() {
        return maxUniqueInteractions;
    }

    public int getMinUniqueInteractions() {
        return minUniqueInteractions;
    }

    public double getFreezProbability() {
        return freezProbability;
    }

    public double getFireProbability() {
        return fireProbability;
    }

    public int getFreezMaxDuraution() {
        return freezMaxDuraution;
    }

    public String getDragonsResultsFilePath() {
        return dragonsResultsFilePath;
    }

    public String getDragonsRelationsFilePath() {
        return dragonsRelationsFilePath;
    }
    //endregion

    //region Fields
    private final int numberOfNodes;
    private final int edgesPerNode;
    private final Date startDateOfStory;
    private final Date endDateOfStory;
    private final int minPower;
    private final int maxPower;
    private final int maxUniqueInteractions;
    private final int minUniqueInteractions;
    private final double freezProbability;
    private final double fireProbability;
    private final int freezMaxDuraution;
    private final String dragonsResultsFilePath;
    private final String dragonsRelationsFilePath;
    //endregion
}
