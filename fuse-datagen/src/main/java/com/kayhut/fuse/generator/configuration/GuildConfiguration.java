package com.kayhut.fuse.generator.configuration;

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
                System.getProperty("user.dir") + File.separator +configuration.getString("resultsPath") + File.separator
                        + configuration.getString("guild.guildsResultsCsvFileName"),
                ""
        );
        this.guilds = configuration.getStringArray("guild.guilds");
        this.startDateOfStory = new Date(configuration.getLong("guild.startDateOfStory"));
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

    public String getIdPrefix() {
        return idPrefix;
    }
    //endregion

    //region Fields
    private String[] guilds;
    private Date startDateOfStory;
    private final String idPrefix;

    //endregion

}
