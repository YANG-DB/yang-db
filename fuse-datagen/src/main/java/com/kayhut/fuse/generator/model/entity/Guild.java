package com.kayhut.fuse.generator.model.entity;

import java.util.Date;

/**
 * Created by benishue on 15-May-17.
 */
public class Guild extends EntityBase {

    //region Ctrs
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

    @Override
    public String[] getRecord() {
        return new String[0];
    }

    //region Fields
    String name;
    String description;
    String iconId;
    String url;
    Date establishDate;
   //endregion

}
