package com.kayhut.fuse.generator.configuration;

import org.apache.commons.configuration.Configuration;

import java.util.Date;

/**
 * Created by benishue on 20/05/2017.
 */
public class KingdomConfiguration extends EntityConfigurationBase {

    //region Ctrs
    public KingdomConfiguration(Configuration configuration) {
        super(configuration.getInt("kingdom.numOfKingdoms"),
                0,
                configuration.getString("resultsPath") + "//"
                        + configuration.getString("kingdom.kingdomsResultsCsvFileName"),
                ""
        );
        this.kingdoms = configuration.getStringArray("kingdom.kingdoms");
        this.startDateOfStory = new Date(configuration.getLong("kingdom.startDateOfStory"));
    }
    //endregion

    //region Getters
    public String[] getKingdoms() {
        return kingdoms;
    }
    public Date getStartDateOfStory() {
        return startDateOfStory;
    }
    //endregion

    //region Fields
    private String[] kingdoms;
    private Date startDateOfStory;
    //endregion

}
