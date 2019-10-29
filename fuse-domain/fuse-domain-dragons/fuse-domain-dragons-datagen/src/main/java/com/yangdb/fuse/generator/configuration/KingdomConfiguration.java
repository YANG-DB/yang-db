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
 * Created by benishue on 20/05/2017.
 */
public class KingdomConfiguration extends EntityConfigurationBase {

    //region Ctrs
    public KingdomConfiguration(Configuration configuration) {
        super(configuration.getInt("kingdom.numOfKingdoms"),
                0,
                System.getProperty("user.dir") + File.separator +configuration.getString("resultsPath") + File.separator
                        + configuration.getString("kingdom.kingdomsResultsCsvFileName"),
                System.getProperty("user.dir") + File.separator +configuration.getString("resultsPath") + File.separator
                        + configuration.getString("kingdom.kingdomsRelationsCsvFileName")
        );
        this.kingdoms = configuration.getStringArray("kingdom.kingdoms");
        this.startDateOfStory = new Date(configuration.getLong("kingdom.startDateOfStory"));
        this.endDateOfStory = new Date(configuration.getLong("kingdom.endDateOfStory"));
        this.idPrefix = configuration.getString("kingdom.idPrefix");

    }
    //endregion

    //region Getters
    public String[] getKingdoms() {
        return kingdoms;
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
    private String[] kingdoms;
    private Date startDateOfStory;
    private Date endDateOfStory;
    private final String idPrefix;
    //endregion

}
