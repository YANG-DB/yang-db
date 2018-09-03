package com.kayhut.fuse.generator.model.entity;

import com.kayhut.fuse.generator.model.enums.Color;
import com.kayhut.fuse.generator.model.enums.Gender;

/**
 * Created by benishue on 15-May-17.
 */
public class Dragon extends EntityBase {

    //region Ctrs
    private Dragon() {
        //Used by the Builder
    }

    public Dragon(String id, String name) {
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

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public Gender getGender() {
        return gender;
    }

    public Color getColor() {
        return color;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    //endregion

    //region Public Methods
    @Override
    public String toString() {
        return "Dragon{" +
                "name='" + name + '\'' +
                ", power=" + power +
                ", gender=" + gender +
                ", color=" + color +
                '}';
    }

    public String[] getRecord(){
        return new String[] { this.getId(),
                this.name,
                Integer.toString(this.getPower()),
                this.getGender().toString(),
                this.getColor().toString()};
    }
    //endregion

    //region Fields
    private String name;
    private int power;
    private Gender gender;
    private Color color;
    //endregion

    //region Builder
    public static final class Builder {
        private String id;
        private String name;
        private int power;
        private Gender gender;
        private Color color;

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

        public Builder withPower(int power) {
            this.power = power;
            return this;
        }

        public Builder withGender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public Builder withColor(Color color) {
            this.color = color;
            return this;
        }

        public Dragon build() {
            Dragon dragon = new Dragon();
            dragon.setId(id);
            dragon.setName(name);
            dragon.setPower(power);
            dragon.setGender(gender);
            dragon.setColor(color);
            return dragon;
        }
    }
    //endregion

}
