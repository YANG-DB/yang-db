package com.kayhut.fuse.generator.model.entity;

import com.kayhut.fuse.generator.model.enums.Color;

/**
 * Created by benishue on 15-May-17.
 */
public class Horse extends EntityBase {

    //region Ctrs

    public Horse() {
    }

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

    //region Public Methods
    @Override
    public String[] getRecord() {
        return new String[]{
                this.getId(),
                this.name,
                Integer.toString(this.weight),
                Integer.toString(this.maxSpeed),
                Integer.toString(this.maxDistance)
        };
    }

    @Override
    public String toString() {
        return "Horse{" +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", color=" + color +
                ", weight=" + weight +
                ", maxSpeed=" + maxSpeed +
                ", maxDistance=" + maxDistance +
                '}';
    }

    //endregion

    //region Fields
    private String name;
    private Color color;
    private int weight;
    private int maxSpeed;
    private int maxDistance;
    //endregion

    //region Builder
    public static final class Builder {
        private String id;
        private String name;
        private Color color;
        private int weight;
        private int maxSpeed;
        private int maxDistance;

        private Builder() {
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withColor(Color color) {
            this.color = color;
            return this;
        }

        public Builder withWeight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder withMaxSpeed(int maxSpeed) {
            this.maxSpeed = maxSpeed;
            return this;
        }

        public Builder withMaxDistance(int maxDistance) {
            this.maxDistance = maxDistance;
            return this;
        }

        public Horse build() {
            Horse horse = new Horse();
            horse.setId(id);
            horse.setName(name);
            horse.setColor(color);
            horse.setWeight(weight);
            horse.setMaxSpeed(maxSpeed);
            horse.setMaxDistance(maxDistance);
            return horse;
        }
    }
    //endregion

}
