package com.yangdb.fuse.generator.configuration;

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



import org.apache.commons.configuration.Configuration;

import java.io.File;
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
                System.getProperty("user.dir") + File.separator + configuration.getString("resultsPath") + File.separator
                        + configuration.getString("person.personsResultsCsvFileName"),
                System.getProperty("user.dir") + File.separator +
                        configuration.getString("resultsPath") + File.separator
                        + configuration.getString("person.personsRelationsCsvFileName")
        );

        this.heightMean = configuration.getDouble("person.heightMean");
        this.heightSD = configuration.getDouble("person.heightSD");
        this.maxChildren = configuration.getDouble("person.maxChildren");
        this.lifeExpectancyMean = configuration.getDouble("person.lifeExpectancyMean");
        this.lifeExpectancySD = configuration.getDouble("person.lifeExpectancySD");
        this.startDateOfStory = new Date(configuration.getLong("person.startDateOfStory"));
        this.endDateOfStory = new Date(configuration.getLong("person.endDateOfStory"));
        this.idPrefix = configuration.getString("person.idPrefix");
        this.maxGuildMembership = configuration.getInt("person.maxGuildMembership");
        this.meanDragonsPerPerson = configuration.getDouble("person.meanDragonsPerPerson");
        this.sdDragonsPerPerson = configuration.getDouble("person.sdDragonsPerPerson");
        this.meanHorsesPerPerson = configuration.getDouble("person.meanHorsesPerPerson");
        this.sdHorsesPerPerson = configuration.getDouble("person.sdHorsesPerPerson");
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

    public String getIdPrefix() {
        return idPrefix;
    }

    public int getMaxGuildMembership() {
        return maxGuildMembership;
    }

    public double getMeanDragonsPerPerson() {
        return meanDragonsPerPerson;
    }

    public double getSdDragonsPerPerson() {
        return sdDragonsPerPerson;
    }

    public double getMeanHorsesPerPerson() {
        return meanHorsesPerPerson;
    }

    public double getSdHorsesPerPerson() {
        return sdHorsesPerPerson;
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
    private final int maxGuildMembership;
    private final double meanDragonsPerPerson;
    private final double sdDragonsPerPerson;
    private final double meanHorsesPerPerson;
    private final double sdHorsesPerPerson;
    private final String idPrefix;
    //endregion
}
