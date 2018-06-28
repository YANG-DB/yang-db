package com.kayhut.fuse.generator.model.entity;

/**
 * Created by benishue on 15-May-17.
 */
public abstract class EntityBase {

    //region Ctrs
    public EntityBase() {
    }

    public EntityBase(String id) {
        this.id = id;
    }
    //endregion

    //region Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    //endregion

    //region Abstract Methods
    public abstract String[] getRecord();
    //endregion

    //region Fields
    private String id;
    //endregion
}
