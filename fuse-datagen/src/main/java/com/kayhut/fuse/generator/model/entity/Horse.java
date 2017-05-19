package com.kayhut.fuse.generator.model.entity;

import com.kayhut.fuse.generator.model.enums.Color;

/**
 * Created by benishue on 15-May-17.
 */
public class Horse extends EntityBase {

    //region Ctrs
    public Horse(String id, String name, Color color) {
        super(id);
        this.name = name;
        this.color = color;
    }
    //endregion

    //region Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    //endregion

    @Override
    public String[] getRecord() {
        return new String[0];
    }

    //region Fields
    private String name;
    private Color color;
    private int weight;
    private int maxSpeed;
    private int maxDistance;
    //endregion

}
