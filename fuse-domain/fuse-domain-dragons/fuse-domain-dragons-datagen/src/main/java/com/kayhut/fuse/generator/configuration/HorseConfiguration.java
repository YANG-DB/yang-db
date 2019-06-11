package com.kayhut.fuse.generator.configuration;

/*-
 * #%L
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
 * Created by benishue on 20/05/2017.
 */
public class HorseConfiguration extends EntityConfigurationBase {

    //region Ctrs
    public HorseConfiguration(final Configuration configuration) {
        super(
                configuration.getInt("horse.numberOfNodes"),
                0,
                System.getProperty("user.dir") + File.separator +configuration.getString("resultsPath") + File.separator
                        + configuration.getString("horse.horsesResultsCsvFileName"),
                ""
        );

        //01/01/1900 00:00:00 GMT epoch time in milliseconds
        this.startDateOfStory = new Date(configuration.getLong("horse.startDateOfStory"));
        //01/01/2000 00:00:00 GMT epoch time in milliseconds
        this.endDateOfStory = new Date(configuration.getLong("horse.endDateOfStory"));
        this.weightMean = configuration.getInt("horse.weightMean");
        this.weightSD = configuration.getInt("horse.weightSD");
        this.maxDistance = configuration.getInt("horse.maxDistance");
        this.minSpeed = configuration.getInt("horse.minSpeed");
        this.maxSpeed = configuration.getInt("horse.maxSpeed");
        this.idPrefix = configuration.getString("horse.idPrefix");

    }
    //endregion

    //region Getters
    public Date getStartDateOfStory() {
        return startDateOfStory;
    }

    public Date getEndDateOfStory() {
        return endDateOfStory;
    }

    public int getMinSpeed() {
        return minSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public double getWeightMean() {
        return weightMean;
    }

    public double getWeightSD() {
        return weightSD;
    }

    public String getIdPrefix() {
        return idPrefix;
    }
    //endregion

    //region Fields
    private Date startDateOfStory;
    private Date endDateOfStory;
    private int minSpeed;
    private int maxSpeed;
    private int maxDistance;
    private double weightMean;
    private double weightSD;
    private final String idPrefix;
    //endregion
}
