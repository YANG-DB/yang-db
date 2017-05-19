package com.kayhut.fuse.generator.configuration;

import org.apache.commons.configuration.Configuration;

import java.util.Date;

/**
 * Created by benishue on 19/05/2017.
 */
public class PersonConfiguration  {

    //region Ctrs
    public PersonConfiguration(Configuration configuration) {
        this.numberOfNodes = configuration.getDouble("person.numberOfNodes");
        this.edgesPerNode = configuration.getDouble("person.edgesPerNode");
        this.heightMean = configuration.getDouble("person.heightMean");
        this.heightSD = configuration.getDouble("person.heightSD");
        this.maxChildren = configuration.getDouble("person.maxChildren");
        this.lifeExpectancyMean = configuration.getDouble("person.lifeExpectancyMean");
        this.lifeExpectancySD = configuration.getDouble("person.lifeExpectancySD");
        this.startDateOfStory = new Date(configuration.getLong("person.startDateOfStory"));
        this.endDateOfStory = new Date(configuration.getLong("person.endDateOfStory"));
        this.personsResultsCsvFileName = configuration.getString("resultsPath") + "//" + configuration.getString("person.personsResultsCsvFileName");
        this.personsRelationsCsvFileName = configuration.getString("resultsPath") + "//" + configuration.getString("person.personsRelationsCsvFileName");
    }
    //endregion

    //region Getters
    public double getNumberOfNodes() {
        return numberOfNodes;
    }

    public double getEdgesPerNode() {
        return edgesPerNode;
    }

    public double getHeightMean() {
        return heightMean;
    }

    public double getHeightSD() {
        return heightSD;
    }

    public double getMaxChildren() {
        return maxChildren;
    }

    public double getLifeExpectancyMean() {
        return lifeExpectancyMean;
    }

    public double getLifeExpectancySD() {
        return lifeExpectancySD;
    }

    public Date getStartDateOfStory() {
        return startDateOfStory;
    }

    public Date getEndDateOfStory() {
        return endDateOfStory;
    }

    public String getPersonsResultsCsvFileName() {
        return personsResultsCsvFileName;
    }

    public String getPersonsRelationsCsvFileName() {
        return personsRelationsCsvFileName;
    }

    //endregion

    //region Fields
    private final double numberOfNodes;
    private final double edgesPerNode;
    private final double heightMean;
    private final double heightSD;
    private final double maxChildren;
    private final double lifeExpectancyMean;
    private final double lifeExpectancySD;
    private final Date startDateOfStory;
    private final Date endDateOfStory;
    private final String personsResultsCsvFileName;
    private final String personsRelationsCsvFileName;
    //endregion
}
