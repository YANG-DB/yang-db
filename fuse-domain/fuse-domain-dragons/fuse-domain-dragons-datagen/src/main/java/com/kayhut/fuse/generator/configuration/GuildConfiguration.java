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
public class GuildConfiguration extends EntityConfigurationBase {

    //region Ctrs
    public GuildConfiguration(final Configuration configuration) {
        super(configuration.getInt("guild.numOfGuilds"),
                0,
                System.getProperty("user.dir") + File.separator + configuration.getString("resultsPath") + File.separator
                        + configuration.getString("guild.guildsResultsCsvFileName"),
                System.getProperty("user.dir") + File.separator + configuration.getString("resultsPath") + File.separator
                        + configuration.getString("guild.guildsRelationsCsvFileName")
        );
        this.guilds = configuration.getStringArray("guild.guilds");
        this.startDateOfStory = new Date(configuration.getLong("guild.startDateOfStory"));
        this.endDateOfStory = new Date(configuration.getLong("guild.endDateOfStory"));
        this.idPrefix = configuration.getString("guild.idPrefix");

    }
    //endregion

    //region Getters
    public String[] getGuilds() {
        return guilds;
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
    //endregion

    //region Fields
    private String[] guilds;
    private Date startDateOfStory;
    private Date endDateOfStory;
    private final String idPrefix;

    //endregion

}
