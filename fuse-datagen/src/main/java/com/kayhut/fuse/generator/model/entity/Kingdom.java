package com.kayhut.fuse.generator.model.entity;

import java.util.Date;

/**
 * Created by benishue on 15-May-17.
 */
public class Kingdom extends EntityBase {


    //region Ctrs
    public Kingdom(String id, String name) {
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

    public String getKing() {
        return king;
    }

    public void setKing(String king) {
        this.king = king;
    }

    public String getQueen() {
        return queen;
    }

    public void setQueen(String queen) {
        this.queen = queen;
    }

    public Date getIndependenceDay() {
        return independenceDay;
    }

    public void setIndependenceDay(Date independenceDay) {
        this.independenceDay = independenceDay;
    }

    public double getFunds() {
        return funds;
    }

    public void setFunds(double funds) {
        this.funds = funds;
    }
    //endregion

    @Override
    public String[] getRecord() {
        return new String[0];
    }

    //region Fields
    private String name;
    private String king;
    private String queen;
    private Date independenceDay;
    private double funds;
    //endregion
}
