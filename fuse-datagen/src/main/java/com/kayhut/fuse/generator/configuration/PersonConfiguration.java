package com.kayhut.fuse.generator.configuration;

import org.apache.commons.configuration.Configuration;

import java.util.Date;

/**
 * Created by benishue on 19/05/2017.
 */
public class PersonConfiguration extends EntityConfigurationBase {

    //region Ctrs
    public PersonConfiguration(final Configuration configuration) {

        super(
                configuration.getInt("person.numberOfNodes"),
                configuration.getInt("person.edgesPerNode"),
                configuration.getString("resultsPath") + "//"
                        + configuration.getString("person.personsResultsCsvFileName"),
                configuration.getString("resultsPath") + "//"
                        + configuration.getString("person.personsRelationsCsvFileName")
        );

        this.heightMean = configuration.getDouble("person.heightMean");
        this.heightSD = configuration.getDouble("person.heightSD");
        this.maxChildren = configuration.getDouble("person.maxChildren");
        this.lifeExpectancyMean = configuration.getDouble("person.lifeExpectancyMean");
        this.lifeExpectancySD = configuration.getDouble("person.lifeExpectancySD");
        this.startDateOfStory = new Date(configuration.getLong("person.startDateOfStory"));
        this.endDateOfStory = new Date(configuration.getLong("person.endDateOfStory"));
    }
    //endregion

    //region Getters
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

    //endregion

    //region Fields
    private final double heightMean;
    private final double heightSD;
    private final double maxChildren;
    private final double lifeExpectancyMean;
    private final double lifeExpectancySD;
    private final Date startDateOfStory;
    private final Date endDateOfStory;
    //endregion
}
