package com.kayhut.fuse.generator.model.entity;

import java.util.Date;

/**
 * Created by benishue on 15-May-17.
 */
public class Guild extends EntityBase {

    //region Ctrs
    public Guild() {
    }

    public Guild(String id, String name) {
        super(id);
        this.name = name;
    }
    //endregion

    //region Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconId() {
        return iconId;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getEstablishDate() {
        return establishDate;
    }

    public void setEstablishDate(Date establishDate) {
        this.establishDate = establishDate;
    }
    //endregion

    //region Public Methods
    @Override
    public String[] getRecord() {
        return new String[0];
    }
    //endregion

    //region Fields
    private String name;
    private String description;
    private String iconId;
    private String url;
    private Date establishDate;
    //endregion

    //region Builder
    public static final class GuildBuilder {
        String name;
        String description;
        String iconId;
        String url;
        Date establishDate;
        private String id;

        private GuildBuilder() {
        }

        public static GuildBuilder aGuild() {
            return new GuildBuilder();
        }

        public GuildBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public GuildBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public GuildBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public GuildBuilder withIconId(String iconId) {
            this.iconId = iconId;
            return this;
        }

        public GuildBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public GuildBuilder withEstablishDate(Date establishDate) {
            this.establishDate = establishDate;
            return this;
        }

        public Guild build() {
            Guild guild = new Guild();
            guild.setId(id);
            guild.setName(name);
            guild.setDescription(description);
            guild.setIconId(iconId);
            guild.setUrl(url);
            guild.setEstablishDate(establishDate);
            return guild;
        }
    }
    //endregion

}
