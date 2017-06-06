package com.kayhut.fuse.generator.configuration;

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
